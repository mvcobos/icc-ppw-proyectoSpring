package ec.edu.ups.icc.proyecto.auditlogs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditLogEntity;
import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditResult;
import ec.edu.ups.icc.proyecto.auditlogs.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

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

        enrichWithRequestContext(entity);
        save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailure(Long actorId, String action, String resourceType, Long resourceId) {

        AuditLogEntity entity = new AuditLogEntity(actorId, action, resourceType, resourceId, AuditResult.FAILED);

        enrichWithRequestContext(entity);
        save(entity);
    }

    private void enrichWithRequestContext(AuditLogEntity entity) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            entity.setCorrelationId(java.util.UUID.randomUUID().toString());
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        entity.setIpAddress(request.getRemoteAddr());
        entity.setHttpMethod(request.getMethod());
        entity.setEndpoint(request.getRequestURI());

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        entity.setCorrelationId(correlationId != null ? correlationId : java.util.UUID.randomUUID().toString());
    }
    
    private void save(AuditLogEntity entity) {
        try {
            auditLogRepository.save(entity);
        } catch (Exception ex) {
            log.error("No se pudo registrar la auditoría de la acción {}: {}",
                    entity.getAction(), ex.getMessage());
        }
    }
}