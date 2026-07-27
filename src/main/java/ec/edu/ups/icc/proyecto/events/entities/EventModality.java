package ec.edu.ups.icc.proyecto.events.entities;

/*
 * mediante chk_events_modality_data:
 *   PRESENTIAL -> location obligatorio, virtualUrl nulo
 *   VIRTUAL    -> virtualUrl obligatorio, location nulo
 *   HYBRID     -> ambos obligatorios
 */
public enum EventModality {
    PRESENTIAL,
    VIRTUAL,
    HYBRID
}