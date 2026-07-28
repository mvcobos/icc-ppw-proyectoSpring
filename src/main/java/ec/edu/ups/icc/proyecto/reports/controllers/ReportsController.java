package ec.edu.ups.icc.proyecto.reports.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.reports.dtos.ReportFilterDto;
import ec.edu.ups.icc.proyecto.reports.services.ReportService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Reportes",
    description = "Archivos descargables generados bajo demanda"
)
@RestController
@RequestMapping("/reports")
public class ReportsController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
        summary = "Listado de inscritos en PDF",
        description = "Descarga el listado de inscritos de un evento. Propietario o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Archivo generado exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado")
    })
    @GetMapping("/events/{eventId}/registrations.pdf")
    public ResponseEntity<byte[]> eventRegistrationsPdf(
            @PathVariable("eventId") Long eventId,
            @Valid @ModelAttribute ReportFilterDto filters,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        byte[] file = reportService.generateEventRegistrationsPdf(eventId, filters, currentUser);

        return buildFileResponse(
                file,
                MediaType.APPLICATION_PDF_VALUE,
                "registrations-event-" + eventId + ".pdf");
    }

    @Operation(
        summary = "Listado de inscritos en Excel",
        description = "Descarga el listado de inscritos de un evento. Propietario o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Archivo generado exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "El evento pertenece a otro organizador"),
        @ApiResponse(
            responseCode = "404",
            description = "Evento no encontrado")
    })
    @GetMapping("/events/{eventId}/registrations.xlsx")
    public ResponseEntity<byte[]> eventRegistrationsExcel(
            @PathVariable("eventId") Long eventId,
            @Valid @ModelAttribute ReportFilterDto filters,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        byte[] file = reportService.generateEventRegistrationsExcel(eventId, filters, currentUser);

        return buildFileResponse(
                file,
                EXCEL_CONTENT_TYPE,
                "registrations-event-" + eventId + ".xlsx");
    }

    /*
     * Encabezados exigidos por el Punto 13: el tipo de archivo y el
     * nombre con el que el navegador debe descargarlo.
     */
    private ResponseEntity<byte[]> buildFileResponse(byte[] content, String contentType,
            String filename) {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}