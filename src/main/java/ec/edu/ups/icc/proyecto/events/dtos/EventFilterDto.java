package ec.edu.ups.icc.proyecto.events.dtos;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import ec.edu.ups.icc.proyecto.events.entities.EventModality;
import ec.edu.ups.icc.proyecto.events.entities.EventStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/*
 * DTO utilizado para recibir filtros opcionales
 * /api/events?search=spring&categoryId=1&modality=VIRTUAL&startFrom=2026-08-01T00:00:00-05:00
 */
public class EventFilterDto {

    @Size(min = 2, max = 160, message = "La búsqueda debe tener entre 2 y 160 caracteres")
    private String search;

    @Min(value = 1, message = "El ID de categoría debe ser mayor a 0")
    private Long categoryId;

    @Min(value = 1, message = "El ID de organizador debe ser mayor a 0")
    private Long organizerId;

    private EventModality modality;

    private EventStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startTo;

    public boolean hasValidDateRange() {
        if (startFrom != null && startTo != null) {
            return !startTo.isBefore(startFrom);
        }

        return true;
    }

    // Constructor vacío
    public EventFilterDto() {
    }

    // Getters y setters
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public OffsetDateTime getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(OffsetDateTime startFrom) {
        this.startFrom = startFrom;
    }

    public OffsetDateTime getStartTo() {
        return startTo;
    }

    public void setStartTo(OffsetDateTime startTo) {
        this.startTo = startTo;
    }
}