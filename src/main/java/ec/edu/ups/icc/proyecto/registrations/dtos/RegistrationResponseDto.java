package ec.edu.ups.icc.proyecto.registrations.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;

/**
 * DTO utilizado para devolver al cliente los datos públicos
 * de una inscripción como respuesta de la API.
 */
public class RegistrationResponseDto {

    private Long id;
    private UUID registrationCode;
    private Long eventId;
    private String eventTitle;
    private UserResponseDto participant;
    private RegistrationStatus status;
    private OffsetDateTime registeredAt;
    private OffsetDateTime statusUpdatedAt;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime cancelledAt;

    // Constructor vacío
    public RegistrationResponseDto() {
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

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public UserResponseDto getParticipant() {
        return participant;
    }

    public void setParticipant(UserResponseDto participant) {
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

    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
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
}