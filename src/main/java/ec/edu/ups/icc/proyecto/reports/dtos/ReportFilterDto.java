package ec.edu.ups.icc.proyecto.reports.dtos;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

/*
 * Filtros por rango de fechas de los reportes (Punto 13).
 *
 * Sus campos llegan desde query params en formato ISO 8601 y se
 * aplican sobre la fecha de inscripción.
 */
public class ReportFilterDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime to;

    /*
     * Valida que el rango sea coherente.
     * Si ambos valores existen, "to" no puede ser anterior a "from".
     */
    public boolean hasValidRange() {
        if (from != null && to != null) {
            return !to.isBefore(from);
        }

        return true;
    }

    // Constructor vacío
    public ReportFilterDto() {
    }

    // Constructor lleno
    public ReportFilterDto(OffsetDateTime from, OffsetDateTime to) {
        this.from = from;
        this.to = to;
    }

    // Getters y setters
    public OffsetDateTime getFrom() {
        return from;
    }

    public void setFrom(OffsetDateTime from) {
        this.from = from;
    }

    public OffsetDateTime getTo() {
        return to;
    }

    public void setTo(OffsetDateTime to) {
        this.to = to;
    }
}