package ec.edu.ups.icc.proyecto.events.entities;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyecto.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyecto.core.entities.BaseEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "events")
public class EventEntity extends BaseEntity {

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 20)
    private EventModality modality;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "virtual_url", length = 500)
    private String virtualUrl;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    /*
     * Cupos todavía disponibles.
     * Solo las inscripciones CONFIRMED consumen cupo.
     */
    @Column(name = "available_capacity", nullable = false)
    private Integer availableCapacity;

    @Column(name = "registration_start_at", nullable = false)
    private OffsetDateTime registrationStartAt;

    @Column(name = "registration_end_at", nullable = false)
    private OffsetDateTime registrationEndAt;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    // Relación muchos a uno con UserEntity.
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id", nullable = false)
    private UserEntity organizer;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /*
     * Si dos transacciones modifican el mismo evento a la vez,
     * la segunda falla en lugar de sobrescribir los cupos.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Constructor vacío
    public EventEntity() {
    }

    // Constructor lleno
    public EventEntity(String title, String description, EventModality modality, String location, String virtualUrl,
            Integer capacity, Integer availableCapacity, OffsetDateTime registrationStartAt,
            OffsetDateTime registrationEndAt, OffsetDateTime startAt, OffsetDateTime endAt,
            UserEntity organizer, CategoryEntity category) {
        this.title = title;
        this.description = description;
        this.modality = modality;
        this.location = location;
        this.virtualUrl = virtualUrl;
        this.capacity = capacity;
        this.availableCapacity = availableCapacity;
        this.registrationStartAt = registrationStartAt;
        this.registrationEndAt = registrationEndAt;
        this.startAt = startAt;
        this.endAt = endAt;
        this.organizer = organizer;
        this.category = category;
    }

    /*
     * Indica si el evento admite nuevas inscripciones.
     * Debe estar publicado, dentro del periodo de inscripción
     * y con cupos disponibles
     */
    public boolean isOpenForRegistration(OffsetDateTime now) {
        return status == EventStatus.PUBLISHED
                && !deleted
                && availableCapacity != null
                && availableCapacity > 0
                && now.isAfter(registrationStartAt)
                && now.isBefore(registrationEndAt);
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

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
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

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public UserEntity getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserEntity organizer) {
        this.organizer = organizer;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getVersion() {
        return version;
    }
}