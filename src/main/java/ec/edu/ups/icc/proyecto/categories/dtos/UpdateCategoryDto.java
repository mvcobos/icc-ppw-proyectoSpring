package ec.edu.ups.icc.proyecto.categories.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCategoryDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no debe superar 255 caracteres")
    private String description;

    // Constructor vacío
    public UpdateCategoryDto() {
    }

    // Constructor lleno
    public UpdateCategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}