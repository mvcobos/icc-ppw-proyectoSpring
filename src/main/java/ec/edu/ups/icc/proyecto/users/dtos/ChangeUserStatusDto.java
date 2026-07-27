package ec.edu.ups.icc.proyecto.users.dtos;

import ec.edu.ups.icc.proyecto.users.entities.UserStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeUserStatusDto {

    @NotNull(message = "El estado es obligatorio")
    private UserStatus status;

    // Constructor vacío
    public ChangeUserStatusDto() {
    }

    // Getters y setters
    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}