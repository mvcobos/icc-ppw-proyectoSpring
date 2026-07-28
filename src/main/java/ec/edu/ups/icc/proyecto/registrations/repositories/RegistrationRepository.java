package ec.edu.ups.icc.proyecto.registrations.repositories;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    // Control de inscripción duplicada: respalda uq_registrations_event_participant.
    Optional<RegistrationEntity> findByEventIdAndParticipantId(Long eventId, Long participantId);

    Page<RegistrationEntity> findByEventId(Long eventId, Pageable pageable);

    Page<RegistrationEntity> findByEventIdAndStatus(Long eventId, RegistrationStatus status, Pageable pageable);

    Page<RegistrationEntity> findByParticipantId(Long participantId, Pageable pageable);

    // Usado para impedir eliminar un evento con inscripciones activas.
    boolean existsByEventIdAndStatusIn(Long eventId, Collection<RegistrationStatus> statuses);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    /*
     * Inscripciones de un evento dentro de un rango de fechas (Punto 13).
     *
     * El rango no usa IS NULL: PostgreSQL no puede deducir el tipo de un
     * parámetro temporal nulo. El servicio envía límites amplios cuando
     * el filtro no viene.
     */
    @Query("""
            SELECT r
            FROM RegistrationEntity r
            WHERE r.event.id = :eventId
              AND r.registeredAt >= :from
              AND r.registeredAt <= :to
            ORDER BY r.registeredAt ASC
            """)
    List<RegistrationEntity> findForReport(
            @Param("eventId") Long eventId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}