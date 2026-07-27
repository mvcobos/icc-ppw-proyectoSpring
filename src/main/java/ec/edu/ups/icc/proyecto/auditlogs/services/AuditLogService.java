package ec.edu.ups.icc.proyecto.auditlogs.services;

/*
 * Pensado para invocarse desde otros servicios al ejecutar
 * operaciones críticas. Nunca debe interrumpir la operación
 * que está auditando.
 */
public interface AuditLogService {

    // Registra una operación ejecutada correctamente.
    void registerSuccess(Long actorId, String action, String resourceType, Long resourceId,
            String previousValue, String newValue);

    // Registra un intento fallido. El actor puede ser nulo.
    void registerFailure(Long actorId, String action, String resourceType, Long resourceId);
}