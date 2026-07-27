package ec.edu.ups.icc.proyecto.events.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.events.dtos.ChangeEventStatusDto;
import ec.edu.ups.icc.proyecto.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventFilterDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyecto.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyecto.events.services.EventService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Eventos",
    description = "Gestión de eventos académicos: creación, actualización, estados y consulta"
)
@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventService eventService;

    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(
        summary = "Listar eventos",
        description = "Devuelve una página de eventos con filtros, búsqueda y ordenamiento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de eventos devuelta exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de filtro o paginación inválidos")
    })
    @GetMapping
    public Page<EventResponseDto> findAll(
            @Valid @ModelAttribute EventFilterDto filters,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return eventService.findAll(filters, pagination);
    }

    @Operation(
        summary = "Buscar evento por ID",
        description = "Devuelve un evento según su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evento encontrado exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado")
    })
    @GetMapping("/{id}")
    public EventResponseDto findOne(@PathVariable("id") Long id) {
        return eventService.findOne(id);
    }

    // POST /events
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @Operation(
        summary = "Crear nuevo evento",
        description = "Crea un evento en estado DRAFT a nombre del organizador autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Evento creado exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del evento inválidos"),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto create(@Valid @RequestBody CreateEventDto dto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventService.create(dto, userDetails.getId());
    }

    // PUT /events/{id}
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Actualizar evento",
        description = "Actualiza completamente un evento. Solo su organizador puede hacerlo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evento actualizado exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado"),
        @ApiResponse(
            responseCode = "409",
            description = "El evento no admite modificaciones")
    })
    @PutMapping("/{id}")
    public EventResponseDto update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateEventDto dto, @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return eventService.update(id, dto, userDetails.getId());
    }

    // PATCH /events/{id}/status
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Cambiar estado del evento",
        description = "Aplica una transición de estado: DRAFT, PUBLISHED, FINISHED o CANCELLED."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estado actualizado exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado"),
        @ApiResponse(
            responseCode = "409",
            description = "Transición de estado no permitida")
    })
    @PatchMapping("/{id}/status")
    public EventResponseDto changeStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeEventStatusDto dto, @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return eventService.changeStatus(id, dto, userDetails.getId());
    }

    // DELETE /events/{id}: Eliminación lógica: deleted = true.
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Eliminar evento",
        description = "Elimina lógicamente un evento sin inscripciones confirmadas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Evento eliminado exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado"),
        @ApiResponse(
            responseCode = "409",
            description = "El evento tiene inscripciones confirmadas")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        eventService.delete(id, userDetails.getId());
    }
}