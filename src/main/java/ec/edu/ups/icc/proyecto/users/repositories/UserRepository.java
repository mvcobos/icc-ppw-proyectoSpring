package ec.edu.ups.icc.proyecto.users.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Usado en login y en el registro
    Optional<UserEntity> findByEmail(String email);

    // Verifica correos duplicados
    boolean existsByEmail(String email);

    // Usuario existente y con la cuenta activa
    Optional<UserEntity> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByIdAndStatus(Long id, UserStatus status);

    /*
     * Búsqueda paginada por email o nombre (Punto 3, módulo de usuarios).
     * search llega ya en minúsculas y envuelto en % desde el service.
     */
    @Query("""
            SELECT u FROM UserEntity u
            WHERE (:search IS NULL
                OR LOWER(u.email) LIKE :search
                OR LOWER(u.firstName) LIKE :search
                OR LOWER(u.lastName) LIKE :search)
            """)
    Page<UserEntity> findWithSearch(@Param("search") String search, Pageable pageable);
}