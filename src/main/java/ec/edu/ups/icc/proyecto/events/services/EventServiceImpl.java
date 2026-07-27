package ec.edu.ups.icc.proyecto.events.services;

import java.util.Set;
import java.util.List;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.proyecto.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyecto.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyecto.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.core.utils.PageableBuilder;
import ec.edu.ups.icc.proyecto.events.dtos.ChangeEventStatusDto;
import ec.edu.ups.icc.proyecto.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventFilterDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyecto.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.events.entities.EventModality;
import ec.edu.ups.icc.proyecto.events.entities.EventStatus;
import ec.edu.ups.icc.proyecto.events.mappers.EventMapper;
import ec.edu.ups.icc.proyecto.events.repositories.EventRepository;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;


@Service
public class EventServiceImpl implements EventService {

    // Lista blanca de campos permitidos para ordenar.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "startAt", "endAt", "capacity", "availableCapacity", "createdAt", "updatedAt"
    );

    /*
     * Límites usados cuando el filtro por rango de fechas no viene.
     *
     * PostgreSQL no puede deducir el tipo de un parámetro temporal
     * nulo comparado con IS NULL, así que en lugar de anular el filtro
     * se envía un rango que abarca todos los registros.
     */
    private static final OffsetDateTime MIN_START_AT = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final OffsetDateTime MAX_START_AT = OffsetDateTime.of(2999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RegistrationRepository registrationRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, RegistrationRepository registrationRepository,
            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.registrationRepository = registrationRepository;
        this.eventMapper = eventMapper;
    }

    // Listado con paginación, filtros, búsqueda y ordenamiento
    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDto> findAll(EventFilterDto filters, PaginationDto pagination) {

        validateFilters(filters);

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        return eventRepository.findWithFilters(
                        normalizeSearch(filters.getSearch()),
                        filters.getCategoryId(),
                        filters.getOrganizerId(),
                        filters.getModality(),
                        filters.getStatus(),
                        orDefault(filters.getStartFrom(), MIN_START_AT),
                        orDefault(filters.getStartTo(), MAX_START_AT),
                        pageable
                )
                .map(eventMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto findOne(Long id) {
        return eventMapper.toResponseDto(findActiveEventOrThrow(id));
    }

    // POST: organizador es el usuario autenticado, nunca viene del body.
    @Override
    @Transactional
    public EventResponseDto create(CreateEventDto dto, Long currentUserId) {

        UserEntity organizer = findActiveUserOrThrow(currentUserId);

        CategoryEntity category = findActiveCategoryOrThrow(dto.getCategoryId());

        validateDates(dto);
        validateModalityData(dto);

        EventEntity entity = eventMapper.toEntity(dto);
        entity.setOrganizer(organizer);
        entity.setCategory(category);
        entity.setStatus(EventStatus.DRAFT);

        return eventMapper.toResponseDto(eventRepository.save(entity));
    }

    // PUT: Solo el organizador propietario puede modificar su evento
    @Override
    @Transactional
    public EventResponseDto update(Long id, UpdateEventDto dto, Long currentUserId) {

        EventEntity entity = findActiveEventOrThrow(id);

        validateOwnership(entity, currentUserId);

        if (entity.getStatus() == EventStatus.FINISHED || entity.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("No se puede modificar un evento finalizado o cancelado");
        }

        CategoryEntity category = findActiveCategoryOrThrow(dto.getCategoryId());

        validateDates(dto);
        validateModalityData(dto);

        /*
         * Al cambiar el cupo hay que conservar las plazas ya ocupadas:
         * el nuevo cupo no puede ser menor que las inscripciones confirmadas.
         */
        int used = entity.getCapacity() - entity.getAvailableCapacity();

        if (dto.getCapacity() < used) {
            throw new ConflictException(
                    "El cupo no puede ser menor a las " + used + " inscripciones ya confirmadas");
        }

        eventMapper.updateEntity(entity, dto);
        entity.setCategory(category);
        entity.setAvailableCapacity(dto.getCapacity() - used);

        return eventMapper.toResponseDto(eventRepository.save(entity));
    }

    // PATCH /events/{id}/status
    @Override
    @Transactional
    public EventResponseDto changeStatus(Long id, ChangeEventStatusDto dto, Long currentUserId) {

        EventEntity entity = findActiveEventOrThrow(id);

        validateOwnership(entity, currentUserId);

        validateStatusTransition(entity.getStatus(), dto.getStatus());

        entity.setStatus(dto.getStatus());

        return eventMapper.toResponseDto(eventRepository.save(entity));
    }

    // DELETE: Eliminación lógica: nunca se borra físicamente (Punto 9).
    @Override
    @Transactional
    public void delete(Long id, Long currentUserId) {

        EventEntity entity = findActiveEventOrThrow(id);

        validateOwnership(entity, currentUserId);

        /*
         * No se elimina un evento publicado con inscripciones (Punto 9).
         * Se consideran activas las pendientes y las confirmadas.
         */
        boolean hasRegistrations = registrationRepository.existsByEventIdAndStatusIn(
                id, List.of(RegistrationStatus.PENDING, RegistrationStatus.CONFIRMED));

        if (entity.getStatus() == EventStatus.PUBLISHED && hasRegistrations) {
            throw new ConflictException("No se puede eliminar un evento publicado con inscripciones");
        }
        if (entity.getStatus() == EventStatus.PUBLISHED
                && entity.getAvailableCapacity() < entity.getCapacity()) {
            throw new ConflictException("No se puede eliminar un evento publicado con inscripciones");
        }

        entity.setDeleted(true);
        eventRepository.save(entity);
    }

    /*
     * Busca un evento activo.
     * Si no existe o está eliminado lógicamente, devuelve 404.
     */
    private EventEntity findActiveEventOrThrow(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    private CategoryEntity findActiveCategoryOrThrow(Long categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private UserEntity findActiveUserOrThrow(Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));
    }

    /*
     * Valida la propiedad del recurso
     *
     * TODO E2: incorporar el bypass de ADMIN leyendo los roles
     * del usuario autenticado.
     */
    private void validateOwnership(EventEntity event, Long currentUserId) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (event.getOrganizer() == null || event.getOrganizer().getId() == null) {
            throw new AccessDeniedException("El evento no tiene organizador válido");
        }

        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes modificar eventos de otro organizador");
        }
    }

    /*
     * Replica en Java el CHECK chk_events_date_order,
     * para responder 400 en lugar de un error de base de datos.
     */
    private void validateDates(CreateEventDto dto) {

        if (!dto.getRegistrationStartAt().isBefore(dto.getRegistrationEndAt())) {
            throw new BadRequestException(
                    "El inicio de inscripciones debe ser anterior al fin de inscripciones");
        }

        if (dto.getRegistrationEndAt().isAfter(dto.getStartAt())) {
            throw new BadRequestException(
                    "Las inscripciones deben cerrar antes o cuando inicia el evento");
        }

        if (!dto.getStartAt().isBefore(dto.getEndAt())) {
            throw new BadRequestException("El inicio del evento debe ser anterior a su fin");
        }
    }

    // Replica en Java el CHECK chk_events_modality_data.
    private void validateModalityData(CreateEventDto dto) {

        boolean hasLocation = dto.getLocation() != null && !dto.getLocation().isBlank();
        boolean hasVirtualUrl = dto.getVirtualUrl() != null && !dto.getVirtualUrl().isBlank();

        if (dto.getModality() == EventModality.PRESENTIAL && (!hasLocation || hasVirtualUrl)) {
            throw new BadRequestException(
                    "Un evento presencial requiere lugar y no admite enlace virtual");
        }

        if (dto.getModality() == EventModality.VIRTUAL && (hasLocation || !hasVirtualUrl)) {
            throw new BadRequestException(
                    "Un evento virtual requiere enlace y no admite lugar físico");
        }

        if (dto.getModality() == EventModality.HYBRID && (!hasLocation || !hasVirtualUrl)) {
            throw new BadRequestException("Un evento híbrido requiere lugar y enlace virtual");
        }
    }

    // Transiciones permitidas del ciclo de vida del evento.
    private void validateStatusTransition(EventStatus current, EventStatus target) {

        if (current == target) {
            throw new ConflictException("El evento ya se encuentra en estado " + target);
        }

        boolean allowed = switch (current) {
            case DRAFT -> target == EventStatus.PUBLISHED || target == EventStatus.CANCELLED;
            case PUBLISHED -> target == EventStatus.FINISHED || target == EventStatus.CANCELLED;
            case FINISHED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new ConflictException("Transición no permitida: " + current + " -> " + target);
        }
    }

    private void validateFilters(EventFilterDto filters) {

        if (filters == null) {
            return;
        }

        if (!filters.hasValidDateRange()) {
            throw new BadRequestException("La fecha final debe ser posterior a la inicial");
        }

        if (filters.getCategoryId() != null
                && !categoryRepository.existsByIdAndActiveTrue(filters.getCategoryId())) {
            throw new NotFoundException("Category not found");
        }
    }

    /*
     * Convierte un texto vacío en null para que el filtro se ignore.
     */
    private String normalizeSearch(String search) {

        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    // Devuelve el valor recibido o el límite por defecto si es nulo.
    private OffsetDateTime orDefault(OffsetDateTime value, OffsetDateTime fallback) {
        return value != null ? value : fallback;
    }
}