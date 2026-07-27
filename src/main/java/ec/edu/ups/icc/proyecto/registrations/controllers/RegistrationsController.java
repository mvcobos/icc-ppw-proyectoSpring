package ec.edu.ups.icc.proyecto.registrations.controllers;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.ChangeRegistrationStatusDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyecto.registrations.services.RegistrationService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/*
 * Estas acciones no llevan el evento en la ruta:
 * la inscripción ya sabe a cuál pertenece.
 */
@Tag(
    name = "Inscripciones",
    description = "Consulta, resolución y cancelación de inscripciones"
)
@RestController
@RequestMapping("/registrations")
public class RegistrationsController {

    private final RegistrationService registrationService;

    public RegistrationsController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // GET /registrations/me
    // TODO E2: @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Listar mis inscripciones",
        description = "Devuelve las inscripciones del participante autenticado."
    )
    @GetMapping("/me")
    public Page<RegistrationResponseDto> findMine(
            @Valid @ModelAttribute PaginationDto pagination, @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return registrationService.findMine(userDetails.getId(), pagination);
    }

    // GET /registrations/{id}
    // TODO E2: @PreAuthorize("isAuthenticated()") + propiedad
    @Operation(
        summary = "Buscar inscripción por ID",
        description = "Accesible para el participante dueño o el organizador del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inscripción encontrada exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "La inscripción pertenece a otro usuario"),
        @ApiResponse(
            responseCode = "404",
            description = "Inscripción no encontrada")
    })
    @GetMapping("/{id}")
    public RegistrationResponseDto findOne(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return registrationService.findOne(id, userDetails.getId());
    }

    // PATCH /registrations/{id}/status
    // Confirmar descuenta un cupo dentro de la transacción.
    // TODO E2: @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") + propiedad
    @Operation(
        summary = "Confirmar o rechazar inscripción",
        description = "Acción del organizador. Confirmar descuenta un cupo del evento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inscripción resuelta exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Estado no permitido para esta operación"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "409",
            description = "La inscripción no está pendiente o no hay cupos")
    })
    @PatchMapping("/{id}/status")
    public RegistrationResponseDto changeStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeRegistrationStatusDto dto, @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return registrationService.changeStatus(id, dto, userDetails.getId());
    }

    // PATCH /registrations/{id}/cancel
    // Si la inscripción estaba confirmada, el cupo se libera.
    // TODO E2: @PreAuthorize("hasRole('PARTICIPANT')") + propiedad
    @Operation(
        summary = "Cancelar mi inscripción",
        description = "Acción del participante. Libera el cupo si la inscripción estaba confirmada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inscripción cancelada exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "La inscripción pertenece a otro participante"),
        @ApiResponse(
            responseCode = "409",
            description = "La inscripción no admite cancelación")
    })
    @PatchMapping("/{id}/cancel")
    public RegistrationResponseDto cancel(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return registrationService.cancel(id, userDetails.getId());
    }
}