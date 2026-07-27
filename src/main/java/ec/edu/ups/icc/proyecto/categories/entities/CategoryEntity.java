package ec.edu.ups.icc.proyecto.categories.entities;

import ec.edu.ups.icc.proyecto.core.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 * Entidad JPA del recurso categories.
 *
 * Representa la tabla categories en PostgreSQL.
 * La eliminación es lógica mediante active = false.
 *
 * El nombre es único sin distinguir mayúsculas:
 * la base lo garantiza con el índice uq_categories_name_lower.
 */
@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // Constructor vacío
    public CategoryEntity() {
    }

    // Constructor lleno
    public CategoryEntity(String name, String description) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}