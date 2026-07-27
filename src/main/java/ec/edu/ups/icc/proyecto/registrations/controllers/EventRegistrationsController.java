package ec.edu.ups.icc.proyecto.registrations.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.proyecto.registrations.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Inscripciones por evento",
    description = "Inscripción de participantes y listado de inscritos de un evento"
)
@RestController
@RequestMapping("/events/{eventId}/registrations")
public class EventRegistrationsController {

    // TODO E2: reemplazar por @AuthenticationPrincipal.
    private static final Long TEMP_PARTICIPANT_ID = 5L;
    private static final Long TEMP_ORGANIZER_ID = 2L;

    private final RegistrationService registrationService;

    public EventRegistrationsController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /*
     * POST /events/{eventId}/registrations: El participante sale del usuario autenticado, no del body.
     */
    // TODO E2: @PreAuthorize("hasRole('PARTICIPANT')")
    @Operation(
        summary = "Inscribirse en un evento",
        description = "Crea una inscripción PENDING para el participante autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Inscripción creada exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado"),
        @ApiResponse(
            responseCode = "409",
            description = "Inscripción duplicada, evento cerrado o sin cupos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponseDto create(@PathVariable("eventId") Long eventId) {
        return registrationService.create(eventId, TEMP_PARTICIPANT_ID);
    }

    // GET /events/{eventId}/registrations?status=CONFIRMED
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Listar inscritos de un evento",
        description = "Devuelve una página de inscripciones. Solo para el organizador del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de inscripciones devuelta exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado")
    })
    @GetMapping
    public Page<RegistrationResponseDto> findByEvent(
            @PathVariable("eventId") Long eventId,
            @RequestParam(value = "status", required = false) RegistrationStatus status,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return registrationService.findByEvent(eventId, status, pagination, TEMP_ORGANIZER_ID);
    }
}