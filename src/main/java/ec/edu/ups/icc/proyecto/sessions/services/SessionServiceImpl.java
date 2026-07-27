package ec.edu.ups.icc.proyecto.sessions.services;

import java.util.Set;

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
import ec.edu.ups.icc.proyecto.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.UpdateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.entities.SessionEntity;
import ec.edu.ups.icc.proyecto.sessions.mappers.SessionMapper;
import ec.edu.ups.icc.proyecto.sessions.repositories.SessionRepository;

@Service
public class SessionServiceImpl implements SessionService {

    // Lista blanca de campos permitidos para ordenar.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "startAt", "endAt", "createdAt", "updatedAt"
    );

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final SessionMapper sessionMapper;

    public SessionServiceImpl(SessionRepository sessionRepository, EventRepository eventRepository,
            SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.sessionMapper = sessionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponseDto> findByEvent(Long eventId, PaginationDto pagination) {

        findActiveEventOrThrow(eventId);

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        return sessionRepository.findByEventId(eventId, pageable)
                .map(sessionMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponseDto findOne(Long eventId, Long sessionId) {

        findActiveEventOrThrow(eventId);

        return sessionMapper.toResponseDto(findSessionOrThrow(eventId, sessionId));
    }

    // POST: Solo el organizador del evento puede agregar sesiones.
    @Override
    @Transactional
    public SessionResponseDto create(Long eventId, CreateSessionDto dto, Long currentUserId) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateOwnership(event, currentUserId);
        validateEventIsEditable(event);
        validateDates(dto, event);

        String title = dto.getTitle().trim();

        if (sessionRepository.existsByEventIdAndTitleIgnoreCaseAndStartAt(eventId, title, dto.getStartAt())) {
            throw new ConflictException("Ya existe una sesión con ese título y horario en el evento");
        }

        SessionEntity entity = sessionMapper.toEntity(dto);
        entity.setEvent(event);

        return sessionMapper.toResponseDto(sessionRepository.save(entity));
    }

    // PUT
    @Override
    @Transactional
    public SessionResponseDto update(Long eventId, Long sessionId, UpdateSessionDto dto, Long currentUserId) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateOwnership(event, currentUserId);
        validateEventIsEditable(event);

        SessionEntity entity = findSessionOrThrow(eventId, sessionId);

        validateDates(dto, event);

        String title = dto.getTitle().trim();

        if (sessionRepository.existsByEventIdAndTitleIgnoreCaseAndStartAtAndIdNot(
                eventId, title, dto.getStartAt(), sessionId)) {
            throw new ConflictException("Ya existe una sesión con ese título y horario en el evento");
        }

        sessionMapper.updateEntity(entity, dto);

        return sessionMapper.toResponseDto(sessionRepository.save(entity));
    }

    // DELETE: Borrado físico: la tabla sessions no tiene eliminación lógica.
    @Override
    @Transactional
    public void delete(Long eventId, Long sessionId, Long currentUserId) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateOwnership(event, currentUserId);
        validateEventIsEditable(event);

        SessionEntity entity = findSessionOrThrow(eventId, sessionId);

        sessionRepository.delete(entity);
    }

    private EventEntity findActiveEventOrThrow(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    // Busca la sesión exigiendo que pertenezca al evento de la ruta.
    private SessionEntity findSessionOrThrow(Long eventId, Long sessionId) {
        return sessionRepository.findByIdAndEventId(sessionId, eventId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    /*
     * La propiedad de la sesión se hereda del evento (Punto 3).
     *
     * TODO E2: incorporar el bypass de ADMIN.
     */
    private void validateOwnership(EventEntity event, Long currentUserId) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (event.getOrganizer() == null || event.getOrganizer().getId() == null) {
            throw new AccessDeniedException("El evento no tiene organizador válido");
        }

        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes modificar sesiones de otro organizador");
        }
    }

    private void validateEventIsEditable(EventEntity event) {
        if (event.getStatus() == EventStatus.FINISHED || event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("No se pueden modificar las sesiones de un evento finalizado o cancelado");
        }
    }

    /*
     * Replica el CHECK chk_sessions_date_order y añade la regla
     * de negocio de que la sesión debe caber dentro del evento.
     */
    private void validateDates(CreateSessionDto dto, EventEntity event) {

        if (!dto.getStartAt().isBefore(dto.getEndAt())) {
            throw new BadRequestException("El inicio de la sesión debe ser anterior a su fin");
        }

        if (dto.getStartAt().isBefore(event.getStartAt())) {
            throw new BadRequestException("La sesión no puede empezar antes que el evento");
        }

        if (dto.getEndAt().isAfter(event.getEndAt())) {
            throw new BadRequestException("La sesión no puede terminar después que el evento");
        }
    }
}