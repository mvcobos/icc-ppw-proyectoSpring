package ec.edu.ups.icc.proyecto.reports.services;

import java.time.ZoneId;
import ec.edu.ups.icc.proyecto.core.config.AppProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyecto.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.events.repositories.EventRepository;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.proyecto.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyecto.reports.dtos.ReportFilterDto;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import ec.edu.ups.icc.proyecto.security.utils.SecurityUtils;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

@Service
public class ReportServiceImpl implements ReportService {

    /*
     * Límites amplios usados cuando el filtro de fechas no viene.
     * La consulta no puede usar IS NULL con parámetros temporales.
     */
    private static final OffsetDateTime MIN_DATE = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final OffsetDateTime MAX_DATE = OffsetDateTime.of(2999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

    // La conversión a America/Guayaquil corresponde al Punto 14.
    // Los instantes se guardan en UTC y se muestran en la zona de negocio.
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] REGISTRATION_COLUMNS = {
            "Código", "Participante", "Correo", "Estado", "Fecha de inscripción"
    };

    
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    // Zona de negocio en la que se muestran los instantes (Punto 14).
    private final ZoneId businessZone;

    public ReportServiceImpl(EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            AppProperties appProperties) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.businessZone = ZoneId.of(appProperties.getTimezone());
    }

    // Listado de inscritos en PDF. Propietario o ADMIN.
    @Override
    @Transactional(readOnly = true)
    public byte[] generateEventRegistrationsPdf(Long eventId, ReportFilterDto filters,
            UserDetailsImpl currentUser) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateEventOwnership(event, currentUser);

        List<RegistrationEntity> registrations = findRegistrations(eventId, filters);

        return buildRegistrationsPdf(event, registrations);
    }

    // Listado de inscritos en Excel. Propietario o ADMIN.
    @Override
    @Transactional(readOnly = true)
    public byte[] generateEventRegistrationsExcel(Long eventId, ReportFilterDto filters,
            UserDetailsImpl currentUser) {

        EventEntity event = findActiveEventOrThrow(eventId);

        validateEventOwnership(event, currentUser);

        List<RegistrationEntity> registrations = findRegistrations(eventId, filters);

        return buildRegistrationsExcel(event, registrations);
    }

    // Comprobante de una inscripción confirmada. Solo el participante propietario.
    @Override
    @Transactional(readOnly = true)
    public byte[] generateRegistrationCertificatePdf(Long registrationId,
            UserDetailsImpl currentUser) {

        RegistrationEntity registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException("Inscripción no encontrada"));

        validateParticipantOwnership(registration, currentUser);

        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new BadRequestException("La inscripción no está confirmada");
        }

        return buildCertificatePdf(registration);
    }

    /*
     * Busca un evento activo.
     * Si no existe o está eliminado, devuelve 404.
     */
    private EventEntity findActiveEventOrThrow(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado"));
    }

    /*
     * El organizador solo consulta reportes de sus propios eventos.
     * ADMIN puede consultar cualquiera.
     */
    private void validateEventOwnership(EventEntity event, UserDetailsImpl currentUser) {

        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (SecurityUtils.isCurrentUserAdmin()) {
            return;
        }

        if (event.getOrganizer() == null || event.getOrganizer().getId() == null) {
            throw new AccessDeniedException("El evento no tiene organizador válido");
        }

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes consultar reportes de eventos de otro organizador");
        }
    }

    /*
     * El comprobante solo lo descarga el participante dueño de la
     * inscripción: el enunciado no contempla bypass de ADMIN aquí.
     */
    private void validateParticipantOwnership(RegistrationEntity registration,
            UserDetailsImpl currentUser) {

        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (registration.getParticipant() == null
                || registration.getParticipant().getId() == null) {
            throw new AccessDeniedException("La inscripción no tiene participante válido");
        }

        if (!registration.getParticipant().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes descargar comprobantes de otro participante");
        }
    }

    /*
     * Aplica el rango de fechas. Cuando un extremo no viene, se envía
     * un límite amplio para que la condición no descarte nada.
     */
    private List<RegistrationEntity> findRegistrations(Long eventId, ReportFilterDto filters) {

        if (filters != null && !filters.hasValidRange()) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial");
        }

        OffsetDateTime from = MIN_DATE;
        OffsetDateTime to = MAX_DATE;

        if (filters != null && filters.getFrom() != null) {
            from = filters.getFrom();
        }

        if (filters != null && filters.getTo() != null) {
            to = filters.getTo();
        }

        return registrationRepository.findForReport(eventId, from, to);
    }

    /*
     * Arma el PDF del listado de inscritos en memoria.
     */
    private byte[] buildRegistrationsPdf(EventEntity event,
            List<RegistrationEntity> registrations) {

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);

        PdfWriter.getInstance(document, output);

        document.open();

        Paragraph title = new Paragraph("Listado de inscritos", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(event.getTitle(), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15f);
        document.add(subtitle);

        PdfPTable table = new PdfPTable(REGISTRATION_COLUMNS.length);
        table.setWidthPercentage(100);

        for (String column : REGISTRATION_COLUMNS) {
            table.addCell(new PdfPCell(new Phrase(column, headerFont)));
        }

        for (RegistrationEntity registration : registrations) {
            table.addCell(new PdfPCell(new Phrase(
                    registration.getRegistrationCode().toString(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(
                    registration.getParticipant().getFullName(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(
                    registration.getParticipant().getEmail(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(
                    registration.getStatus().name(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(
                    formatDate(registration.getRegisteredAt()), bodyFont)));
        }

        document.add(table);

        Paragraph total = new Paragraph("Total de registros: " + registrations.size(), bodyFont);
        total.setSpacingBefore(15f);
        document.add(total);

        document.close();

        return output.toByteArray();
    }

    /*
     * Arma el Excel del listado de inscritos en memoria.
     */
    private byte[] buildRegistrationsExcel(EventEntity event,
            List<RegistrationEntity> registrations) {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inscritos");

            // Font de POI, no de OpenPDF: ambas clases se llaman igual.
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < REGISTRATION_COLUMNS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(REGISTRATION_COLUMNS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;

            for (RegistrationEntity registration : registrations) {
                Row row = sheet.createRow(rowIndex);

                row.createCell(0).setCellValue(registration.getRegistrationCode().toString());
                row.createCell(1).setCellValue(registration.getParticipant().getFullName());
                row.createCell(2).setCellValue(registration.getParticipant().getEmail());
                row.createCell(3).setCellValue(registration.getStatus().name());
                row.createCell(4).setCellValue(formatDate(registration.getRegisteredAt()));

                rowIndex++;
            }

            for (int i = 0; i < REGISTRATION_COLUMNS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);

            return output.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", ex);
        }
    }

    /*
     * Arma el comprobante de una inscripción confirmada.
     */
    private byte[] buildCertificatePdf(RegistrationEntity registration) {

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);

        PdfWriter.getInstance(document, output);

        document.open();

        Paragraph title = new Paragraph("Comprobante de inscripción", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(25f);
        document.add(title);

        document.add(buildCertificateLine("Código: ",
                registration.getRegistrationCode().toString(), labelFont, bodyFont));
        document.add(buildCertificateLine("Participante: ",
                registration.getParticipant().getFullName(), labelFont, bodyFont));
        document.add(buildCertificateLine("Correo: ",
                registration.getParticipant().getEmail(), labelFont, bodyFont));
        document.add(buildCertificateLine("Evento: ",
                registration.getEvent().getTitle(), labelFont, bodyFont));
        document.add(buildCertificateLine("Fecha del evento: ",
                formatDate(registration.getEvent().getStartAt()), labelFont, bodyFont));
        document.add(buildCertificateLine("Estado: ",
                registration.getStatus().name(), labelFont, bodyFont));
        document.add(buildCertificateLine("Confirmada el: ",
                formatDate(registration.getConfirmedAt()), labelFont, bodyFont));

        document.close();

        return output.toByteArray();
    }

    // Une etiqueta y valor en un mismo párrafo con fuentes distintas.
    private Paragraph buildCertificateLine(String label, String value,
            Font labelFont, Font bodyFont) {

        Paragraph line = new Paragraph();
        line.add(new Phrase(label, labelFont));
        line.add(new Phrase(value, bodyFont));
        line.setSpacingAfter(8f);

        return line;
    }

    // Evita imprimir "null" cuando la fecha no está asignada.
    private String formatDate(OffsetDateTime date) {

        if (date == null) {
            return "";
        }

        return date.format(DATE_FORMATTER);
    }

}