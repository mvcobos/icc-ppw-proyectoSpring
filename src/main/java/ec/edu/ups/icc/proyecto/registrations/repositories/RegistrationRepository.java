package ec.edu.ups.icc.proyecto.registrations.repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}