package ec.edu.ups.icc.proyecto.reports.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.reports.services.ReportService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * El enunciado ubica esta ruta bajo /registrations, pero el archivo
 * lo genera el módulo de reportes.
 */
@Tag(
    name = "Reportes",
    description = "Archivos descargables generados bajo demanda"
)
@RestController
@RequestMapping("/registrations")
public class RegistrationCertificateController {

    private final ReportService reportService;

    public RegistrationCertificateController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
        summary = "Comprobante de inscripción en PDF",
        description = "Descarga el comprobante de una inscripción confirmada. Solo el participante propietario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comprobante generado exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "La inscripción no está confirmada"),
        @ApiResponse(
            responseCode = "403",
            description = "La inscripción pertenece a otro participante"),
        @ApiResponse(
            responseCode = "404",
            description = "Inscripción no encontrada")
    })
    @GetMapping("/{id}/certificate.pdf")
    public ResponseEntity<byte[]> registrationCertificate(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        byte[] file = reportService.generateRegistrationCertificatePdf(id, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"certificate-registration-" + id + ".pdf\"")
                .body(file);
    }
}