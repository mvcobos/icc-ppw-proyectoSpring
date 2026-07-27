package ec.edu.ups.icc.proyecto.users.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Usado en login y en el registro (E2)
    Optional<UserEntity> findByEmail(String email);

    // Verifica correos duplicados
    boolean existsByEmail(String email);

    // Usuario existente y con la cuenta activa
    Optional<UserEntity> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByIdAndStatus(Long id, UserStatus status);
}