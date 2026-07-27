package ec.edu.ups.icc.proyecto.registrations.services;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.core.utils.PageableBuilder;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.events.entities.EventStatus;
import ec.edu.ups.icc.proyecto.events.repositories.EventRepository;
import ec.edu.ups.icc.proyecto.registrations.dtos.ChangeRegistrationStatusDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.proyecto.registrations.mappers.RegistrationMapper;
import ec.edu.ups.icc.proyecto.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    // Lista blanca de campos permitidos para ordenar.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "status", "registeredAt", "statusUpdatedAt"
    );

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper registrationMapper;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, EventRepository eventRepository,
            UserRepository userRepository, RegistrationMapper registrationMapper) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationMapper = registrationMapper;
    }

    /*
     * POST
     *
     * Reglas del Punto 9:
     *   - no dos inscripciones del mismo participante en un evento
     *   - no inscripciones en eventos finalizados o sin cupos
     *
     * Toda la operación va dentro de una transacción.
     */
    @Override
    @Transactional
    public RegistrationResponseDto create(Long eventId, Long currentUserId) {

        EventEntity event = findActiveEventOrThrow(eventId);

        UserEntity participant = findActiveUserOrThrow(currentUserId);

        OffsetDateTime now = OffsetDateTime.now();

        validateEventAcceptsRegistrations(event, now);

        /*
         * Si ya existe una inscripción para este par no se crea otra:
         * la restricción uq_registrations_event_participant lo impide.
         *
         * Si estaba cancelada o rechazada se reactiva como PENDING
         * para que el participante no quede vetado del evento.
         */
        RegistrationEntity existing = registrationRepository
                .findByEventIdAndParticipantId(eventId, currentUserId)
                .orElse(null);

        if (existing != null) {

            if (existing.getStatus() == RegistrationStatus.PENDING
                    || existing.getStatus() == RegistrationStatus.CONFIRMED) {
                throw new ConflictException("Ya tienes una inscripción en este evento");
            }

            existing.setStatus(RegistrationStatus.PENDING);
            existing.setStatusUpdatedAt(now);
            existing.setConfirmedAt(null);
            existing.setCancelledAt(null);

            return registrationMapper.toResponseDto(registrationRepository.save(existing));
        }

        RegistrationEntity entity = new RegistrationEntity(UUID.randomUUID(), event, participant);
        entity.setStatus(RegistrationStatus.PENDING);
        entity.setStatusUpdatedAt(now);

        return registrationMapper.toResponseDto(registrationRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponseDto> findByEvent(Long eventId, RegistrationStatus status,
            PaginationDto pagination, Long currentUserId) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateEventOwnership(event, currentUserId);

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        Page<RegistrationEntity> page = status != null
                ? registrationRepository.findByEventIdAndStatus(eventId, status, pageable)
                : registrationRepository.findByEventId(eventId, pageable);

        return page.map(registrationMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponseDto> findMine(Long currentUserId, PaginationDto pagination) {

        findActiveUserOrThrow(currentUserId);

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        return registrationRepository.findByParticipantId(currentUserId, pageable)
                .map(registrationMapper::toResponseDto);
    }

    // Puede consultarla el participante dueño o el organizador del evento.
    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDto findOne(Long registrationId, Long currentUserId) {

        RegistrationEntity entity = findRegistrationOrThrow(registrationId);

        if (currentUserId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        boolean isParticipant = entity.getParticipant().getId().equals(currentUserId);
        boolean isOrganizer = entity.getEvent().getOrganizer().getId().equals(currentUserId);

        if (!isParticipant && !isOrganizer) {
            throw new AccessDeniedException("No puedes consultar inscripciones ajenas");
        }

        return registrationMapper.toResponseDto(entity);
    }

    /*
     * PATCH
     * Confirmar descuenta un cupo del evento dentro de la misma
     * transacción que actualiza la inscripción.
     */
    @Override
    @Transactional
    public RegistrationResponseDto changeStatus(Long registrationId, ChangeRegistrationStatusDto dto,
            Long currentUserId) {

        RegistrationEntity entity = findRegistrationOrThrow(registrationId);

        EventEntity event = entity.getEvent();

        validateEventOwnership(event, currentUserId);

        RegistrationStatus target = dto.getStatus();

        if (target != RegistrationStatus.CONFIRMED && target != RegistrationStatus.REJECTED) {
            throw new BadRequestException("El organizador solo puede confirmar o rechazar una inscripción");
        }

        if (entity.getStatus() != RegistrationStatus.PENDING) {
            throw new ConflictException("Solo se pueden resolver inscripciones pendientes");
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (target == RegistrationStatus.CONFIRMED) {

            if (event.getAvailableCapacity() == null || event.getAvailableCapacity() <= 0) {
                throw new ConflictException("El evento no tiene cupos disponibles");
            }

            event.setAvailableCapacity(event.getAvailableCapacity() - 1);
            eventRepository.save(event);

            entity.setConfirmedAt(now);
        }

        entity.setStatus(target);
        entity.setStatusUpdatedAt(now);

        return registrationMapper.toResponseDto(registrationRepository.save(entity));
    }

    /*
     * El participante cancela su propia inscripción.
     * Si estaba confirmada, el cupo vuelve a quedar disponible.
     */
    @Override
    @Transactional
    public RegistrationResponseDto cancel(Long registrationId, Long currentUserId) {

        RegistrationEntity entity = findRegistrationOrThrow(registrationId);

        if (currentUserId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (!entity.getParticipant().getId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes cancelar inscripciones ajenas");
        }

        if (entity.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ConflictException("La inscripción ya está cancelada");
        }

        if (entity.getStatus() == RegistrationStatus.REJECTED) {
            throw new ConflictException("No se puede cancelar una inscripción rechazada");
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (entity.consumesCapacity()) {

            EventEntity event = entity.getEvent();

            int released = event.getAvailableCapacity() + 1;

            // Nunca por encima del cupo total: lo exige chk_events_available_capacity.
            event.setAvailableCapacity(Math.min(released, event.getCapacity()));
            eventRepository.save(event);
        }

        entity.setStatus(RegistrationStatus.CANCELLED);
        entity.setCancelledAt(now);
        entity.setStatusUpdatedAt(now);

        return registrationMapper.toResponseDto(registrationRepository.save(entity));
    }

    private RegistrationEntity findRegistrationOrThrow(Long registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException("Registration not found"));
    }

    private EventEntity findActiveEventOrThrow(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    private UserEntity findActiveUserOrThrow(Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));
    }

    /*
     * Solo el organizador del evento resuelve sus inscripciones (Punto 3).
     *
     * TODO E2: incorporar el bypass de ADMIN.
     */
    private void validateEventOwnership(EventEntity event, Long currentUserId) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (event.getOrganizer() == null || event.getOrganizer().getId() == null) {
            throw new AccessDeniedException("El evento no tiene organizador válido");
        }

        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes gestionar inscripciones de otro organizador");
        }
    }

    /*
     * Un evento admite inscripciones si está publicado, dentro del
     * periodo de inscripción y con cupos disponibles.
     */
    private void validateEventAcceptsRegistrations(EventEntity event, OffsetDateTime now) {

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ConflictException("El evento no admite inscripciones en su estado actual");
        }

        if (now.isBefore(event.getRegistrationStartAt())) {
            throw new ConflictException("El periodo de inscripciones aún no ha iniciado");
        }

        if (now.isAfter(event.getRegistrationEndAt())) {
            throw new ConflictException("El periodo de inscripciones ya finalizó");
        }

        if (event.getAvailableCapacity() == null || event.getAvailableCapacity() <= 0) {
            throw new ConflictException("El evento no tiene cupos disponibles");
        }
    }
}