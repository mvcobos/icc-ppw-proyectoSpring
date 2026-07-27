package ec.edu.ups.icc.proyecto.registrations.dtos;

import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

/*
 * La cancelación no se hace por aquí: es una acción
 * del participante con su propio endpoint.
 */
public class ChangeRegistrationStatusDto {

    @NotNull(message = "El estado es obligatorio")
    private RegistrationStatus status;

    // Constructor vacío
    public ChangeRegistrationStatusDto() {
    }

    // Constructor lleno
    public ChangeRegistrationStatusDto(RegistrationStatus status) {
        this.status = status;
    }

    // Getters y setters
    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}