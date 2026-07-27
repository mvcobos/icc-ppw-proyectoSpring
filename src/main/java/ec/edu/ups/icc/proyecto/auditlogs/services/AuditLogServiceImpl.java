package ec.edu.ups.icc.proyecto.auditlogs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditLogEntity;
import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditResult;
import ec.edu.ups.icc.proyecto.auditlogs.repositories.AuditLogRepository;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerSuccess(Long actorId, String action, String resourceType, Long resourceId,
            String previousValue, String newValue) {

        AuditLogEntity entity = new AuditLogEntity(actorId, action, resourceType, resourceId, AuditResult.SUCCESS);
        entity.setPreviousValue(previousValue);
        entity.setNewValue(newValue);

        save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailure(Long actorId, String action, String resourceType, Long resourceId) {

        AuditLogEntity entity = new AuditLogEntity(actorId, action, resourceType, resourceId, AuditResult.FAILED);

        save(entity);
    }

    /*
     * La auditoría no debe hacer fallar la operación auditada.
     *
     * Si el registro falla, se deja constancia en el log técnico
     * y la petición principal continúa con normalidad.
     *
     * TODO E2: completar ipAddress, httpMethod, endpoint y correlationId
     * desde la petición HTTP actual.
     */
    private void save(AuditLogEntity entity) {
        try {
            auditLogRepository.save(entity);
        } catch (Exception ex) {
            log.error("No se pudo registrar la auditoría de la acción {}: {}",
                    entity.getAction(), ex.getMessage());
        }
    }
}