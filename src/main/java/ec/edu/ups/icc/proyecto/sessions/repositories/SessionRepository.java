package ec.edu.ups.icc.proyecto.sessions.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.sessions.entities.SessionEntity;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    Page<SessionEntity> findByEventId(Long eventId, Pageable pageable);

    /*
     * Busca una sesión asegurando que pertenezca al evento indicado.
     * Evita acceder a una sesión ajena conociendo solo su id.
     */
    Optional<SessionEntity> findByIdAndEventId(Long id, Long eventId);

    boolean existsByEventId(Long eventId);

    // Restricción uq_sessions_event_title_start
    boolean existsByEventIdAndTitleIgnoreCaseAndStartAt(Long eventId, String title, OffsetDateTime startAt);

    boolean existsByEventIdAndTitleIgnoreCaseAndStartAtAndIdNot(Long eventId, String title,
            OffsetDateTime startAt, Long id);
}