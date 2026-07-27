package ec.edu.ups.icc.proyecto.auditlogs.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditLogEntity;
import ec.edu.ups.icc.proyecto.auditlogs.entities.AuditResult;

/*
 * Los registros solo se insertan y se consultan:
 * la auditoría nunca se modifica ni se elimina.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    Page<AuditLogEntity> findByActorId(Long actorId, Pageable pageable);

    Page<AuditLogEntity> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);

    Page<AuditLogEntity> findByAction(String action, Pageable pageable);

    Page<AuditLogEntity> findByResult(AuditResult result, Pageable pageable);
}