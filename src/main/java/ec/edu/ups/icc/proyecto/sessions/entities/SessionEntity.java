package ec.edu.ups.icc.proyecto.sessions.entities;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyecto.core.entities.BaseEntity;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * No tiene eliminación lógica: la tabla no define
 * la columna deleted, por lo que el borrado es físico.
 */
@Entity
@Table(name = "sessions")
public class SessionEntity extends BaseEntity {

    /*
     * Relación muchos a uno con EventEntity.
     *
     * La propiedad del recurso se hereda del evento:
     * el dueño de la sesión es el organizador del evento.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "virtual_url", length = 500)
    private String virtualUrl;

    // Constructor vacío
    public SessionEntity() {
    }

    // Constructor lleno
    public SessionEntity(EventEntity event, String title, String description, OffsetDateTime startAt,
            OffsetDateTime endAt, String location, String virtualUrl) {
        this.event = event;
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.virtualUrl = virtualUrl;
    }

    // Getters y setters
    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

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