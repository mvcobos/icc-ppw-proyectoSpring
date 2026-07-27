package ec.edu.ups.icc.proyecto.events.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.events.entities.EventModality;
import ec.edu.ups.icc.proyecto.events.entities.EventStatus;

/*
 * Repositorio encargado de gestionar la persistencia
 * de eventos usando Spring Data JPA.
 */
@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    Optional<EventEntity> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    // Usado al eliminar una categoría o un organizador
    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    /*
     * Listado paginado de eventos activos con filtros opcionales
     * y búsqueda por texto (Punto 9).
     *
     * Si un filtro llega como null, no se aplica.
     *
     * El rango de fechas no usa IS NULL: PostgreSQL no puede deducir
     * el tipo de un parámetro temporal nulo. El servicio envía
     * límites amplios cuando el filtro no viene.
     */
    @Query(
            value = """
                    SELECT e
                    FROM EventEntity e
                    WHERE e.deleted = false
                      AND (COALESCE(:search, '') = ''
                           OR LOWER(e.title) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))
                           OR LOWER(e.description) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')))
                      AND (:categoryId IS NULL OR e.category.id = :categoryId)
                      AND (:organizerId IS NULL OR e.organizer.id = :organizerId)
                      AND (:modality IS NULL OR e.modality = :modality)
                      AND (:status IS NULL OR e.status = :status)
                      AND e.startAt >= :startFrom
                      AND e.startAt <= :startTo
                    """,
            countQuery = """
                    SELECT COUNT(e)
                    FROM EventEntity e
                    WHERE e.deleted = false
                      AND (COALESCE(:search, '') = ''
                           OR LOWER(e.title) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))
                           OR LOWER(e.description) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')))
                      AND (:categoryId IS NULL OR e.category.id = :categoryId)
                      AND (:organizerId IS NULL OR e.organizer.id = :organizerId)
                      AND (:modality IS NULL OR e.modality = :modality)
                      AND (:status IS NULL OR e.status = :status)
                      AND e.startAt >= :startFrom
                      AND e.startAt <= :startTo
                    """
    )
    Page<EventEntity> findWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("organizerId") Long organizerId,
            @Param("modality") EventModality modality,
            @Param("status") EventStatus status,
            @Param("startFrom") OffsetDateTime startFrom,
            @Param("startTo") OffsetDateTime startTo,
            Pageable pageable
    );
}