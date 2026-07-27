package ec.edu.ups.icc.proyecto.sessions.dtos;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSessionDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 160, message = "El título debe tener entre 5 y 160 caracteres")
    private String title;

    private String description;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private OffsetDateTime startAt;

    @NotNull(message = "La fecha de fin es obligatoria")
    private OffsetDateTime endAt;

    @Size(max = 200, message = "El lugar no debe superar 200 caracteres")
    private String location;

    @Size(max = 500, message = "El enlace virtual no debe superar 500 caracteres")
    private String virtualUrl;

    // Constructor vacío
    public CreateSessionDto() {
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
}