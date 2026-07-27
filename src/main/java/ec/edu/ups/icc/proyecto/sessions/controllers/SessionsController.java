package ec.edu.ups.icc.proyecto.sessions.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.UpdateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.services.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/*
 * Controlador REST del módulo sessions.
 *
 * Las sesiones cuelgan siempre de su evento:
 * no existen fuera de él.
 */
@Tag(
    name = "Sesiones",
    description = "Gestión de las sesiones y horarios de cada evento"
)
@RestController
@RequestMapping("/events/{eventId}/sessions")
public class SessionsController {

    /*
     * TODO E2: reemplazar por @AuthenticationPrincipal.
     */
    private static final Long TEMP_CURRENT_USER_ID = 2L;

    private final SessionService sessionService;

    public SessionsController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /*
     * GET /events/{eventId}/sessions?sortBy=startAt
     */
    @Operation(
        summary = "Listar sesiones de un evento",
        description = "Devuelve una página con las sesiones del evento indicado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de sesiones devuelta exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado")
    })
    @GetMapping
    public Page<SessionResponseDto> findByEvent(
            @PathVariable("eventId") Long eventId,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return sessionService.findByEvent(eventId, pagination);
    }

    /*
     * GET /events/{eventId}/sessions/{sessionId}
     */
    @Operation(
        summary = "Buscar sesión por ID",
        description = "Devuelve una sesión siempre que pertenezca al evento indicado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sesión encontrada exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento o sesión no encontrados")
    })
    @GetMapping("/{sessionId}")
    public SessionResponseDto findOne(
            @PathVariable("eventId") Long eventId,
            @PathVariable("sessionId") Long sessionId
    ) {
        return sessionService.findOne(eventId, sessionId);
    }

    /*
     * POST /events/{eventId}/sessions
     */
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Crear sesión",
        description = "Agrega una sesión al evento. Su horario debe caber dentro del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sesión creada exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Horario de la sesión inválido"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una sesión con ese título y horario")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponseDto create(
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody CreateSessionDto dto
    ) {
        return sessionService.create(eventId, dto, TEMP_CURRENT_USER_ID);
    }

    /*
     * PUT /events/{eventId}/sessions/{sessionId}
     */
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Actualizar sesión",
        description = "Actualiza completamente una sesión del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sesión actualizada exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento o sesión no encontrados")
    })
    @PutMapping("/{sessionId}")
    public SessionResponseDto update(
            @PathVariable("eventId") Long eventId,
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody UpdateSessionDto dto
    ) {
        return sessionService.update(eventId, sessionId, dto, TEMP_CURRENT_USER_ID);
    }

    /*
     * DELETE /events/{eventId}/sessions/{sessionId}
     * Borrado físico: la tabla no tiene eliminación lógica.
     */
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Eliminar sesión",
        description = "Elimina una sesión del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Sesión eliminada exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento o sesión no encontrados")
    })
    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("eventId") Long eventId,
            @PathVariable("sessionId") Long sessionId
    ) {
        sessionService.delete(eventId, sessionId, TEMP_CURRENT_USER_ID);
    }
}