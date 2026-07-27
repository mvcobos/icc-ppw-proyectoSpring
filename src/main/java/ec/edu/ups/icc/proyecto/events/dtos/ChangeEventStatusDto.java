package ec.edu.ups.icc.proyecto.events.dtos;

import ec.edu.ups.icc.proyecto.events.entities.EventStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para cambiar el estado de un evento (PATCH).
 */
public class ChangeEventStatusDto {

    @NotNull(message = "El estado es obligatorio")
    private EventStatus status;

    // Constructor vacío
    public ChangeEventStatusDto() {
    }

    // Constructor lleno
    public ChangeEventStatusDto(EventStatus status) {
        this.status = status;
    }

    // Getters y setters
    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}