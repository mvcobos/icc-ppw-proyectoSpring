package ec.edu.ups.icc.proyecto.registrations.entities;

/*
 * PENDING   -> solicitada, aún no consume cupo
 * CONFIRMED -> aceptada por el organizador, consume cupo
 * REJECTED  -> rechazada por el organizador
 * CANCELLED -> cancelada por el participante
 */
public enum RegistrationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED
}