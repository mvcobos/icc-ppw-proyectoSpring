package ec.edu.ups.icc.proyecto.events.entities;

/*
 * Estado del evento.
 *
 * Coincide con el CHECK chk_events_status de la tabla events.
 * Solo los eventos PUBLISHED admiten inscripciones (Punto 9).
 */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    FINISHED,
    CANCELLED
}