package ec.edu.ups.icc.proyecto.registrations.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/*
 * Solo puede existir una inscripción por participante y evento.
 *
 * No extiende BaseEntity: la tabla no define created_at
 * ni updated_at, sino registered_at y status_updated_at.
 */
@Entity
@Table(name = "registrations")
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código público usado en los comprobantes. Se genera en la aplicación.
    @Column(name = "registration_code", nullable = false, unique = true, updatable = false)
    private UUID registrationCode;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_id", nullable = false)
    private UserEntity participant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Generated(event = EventType.INSERT)
    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;

    /*
     * La tabla registrations no tiene trigger de actualización:
     * esta fecha se asigna desde el servicio en cada cambio de estado.
     */
    @Column(name = "status_updated_at", nullable = false)
    private OffsetDateTime statusUpdatedAt;

    /*
     * La base exige confirmed_at cuando el estado es CONFIRMED
     * y cancelled_at cuando es CANCELLED.
     */
    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;


    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Constructor vacío
    public RegistrationEntity() {
    }

    // Constructor lleno
    public RegistrationEntity(UUID registrationCode, EventEntity event, UserEntity participant) {
        this.registrationCode = registrationCode;
        this.event = event;
        this.participant = participant;
    }

    // Indica si la inscripción consume un cupo del evento.
    public boolean consumesCapacity() {
        return status == RegistrationStatus.CONFIRMED;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(UUID registrationCode) {
        this.registrationCode = registrationCode;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

    public UserEntity getParticipant() {
        return participant;
    }

    public void setParticipant(UserEntity participant) {
        this.participant = participant;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public OffsetDateTime getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(OffsetDateTime statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(OffsetDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Long getVersion() {
        return version;
    }
}