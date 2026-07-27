package ec.edu.ups.icc.proyecto.events.dtos;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyecto.events.entities.EventModality;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * El organizador no viaja en el body: se obtiene del
 * usuario autenticado, igual que el owner de productos.
 */
public class CreateEventDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 160, message = "El título debe tener entre 5 y 160 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "La modalidad es obligatoria")
    private EventModality modality;

    @Size(max = 200, message = "El lugar no debe superar 200 caracteres")
    private String location;

    @Size(max = 500, message = "El enlace virtual no debe superar 500 caracteres")
    private String virtualUrl;

    @NotNull(message = "El cupo es obligatorio")
    @Min(value = 1, message = "El cupo debe ser mayor a 0")
    private Integer capacity;

    @NotNull(message = "La fecha de inicio de inscripciones es obligatoria")
    private OffsetDateTime registrationStartAt;

    @NotNull(message = "La fecha de fin de inscripciones es obligatoria")
    private OffsetDateTime registrationEndAt;

    @NotNull(message = "La fecha de inicio del evento es obligatoria")
    private OffsetDateTime startAt;

    @NotNull(message = "La fecha de fin del evento es obligatoria")
    private OffsetDateTime endAt;

    @NotNull(message = "La categoría es obligatoria")
    @Min(value = 1, message = "El ID de categoría debe ser mayor a 0")
    private Long categoryId;

    // Constructor vacío
    public CreateEventDto() {
    }

    // Getters y setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVirtualUrl() {
        return virtualUrl;
    }

    public void setVirtualUrl(String virtualUrl) {
        this.virtualUrl = virtualUrl;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public OffsetDateTime getRegistrationStartAt() {
        return registrationStartAt;
    }

    public void setRegistrationStartAt(OffsetDateTime registrationStartAt) {
        this.registrationStartAt = registrationStartAt;
    }

    public OffsetDateTime getRegistrationEndAt() {
        return registrationEndAt;
    }

    public void setRegistrationEndAt(OffsetDateTime registrationEndAt) {
        this.registrationEndAt = registrationEndAt;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}