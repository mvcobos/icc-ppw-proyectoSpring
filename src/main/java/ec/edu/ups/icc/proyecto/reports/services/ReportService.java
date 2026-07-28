package ec.edu.ups.icc.proyecto.reports.services;

import ec.edu.ups.icc.proyecto.reports.dtos.ReportFilterDto;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;

/*
 * Servicio que genera los archivos descargables del Punto 13.
 *
 * Los archivos se construyen en memoria y se devuelven como byte[]:
 * el contenedor no debe almacenar archivos permanentes (Punto 15).
 */
public interface ReportService {

    /*
     * Listado de inscritos de un evento en PDF.
     * Solo el organizador propietario o un ADMIN.
     */
    byte[] generateEventRegistrationsPdf(
            Long eventId,
            ReportFilterDto filters,
            UserDetailsImpl currentUser
    );

    /*
     * Listado de inscritos de un evento en Excel.
     * Solo el organizador propietario o un ADMIN.
     */
    byte[] generateEventRegistrationsExcel(
            Long eventId,
            ReportFilterDto filters,
            UserDetailsImpl currentUser
    );

    /*
     * Comprobante de una inscripción confirmada.
     * Solo el participante propietario.
     */
    byte[] generateRegistrationCertificatePdf(
            Long registrationId,
            UserDetailsImpl currentUser
    );
}