package ec.edu.ups.icc.proyecto.users.dtos;

import ec.edu.ups.icc.proyecto.roles.entities.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class AssignRolesDto {

    @NotEmpty(message = "Debe asignar al menos un rol")
    private Set<RoleName> roles;

    // Constructor vacío
    public AssignRolesDto() {
    }

    // Getters y setters
    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }
}