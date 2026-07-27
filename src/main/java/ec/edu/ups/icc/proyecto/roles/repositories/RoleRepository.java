package ec.edu.ups.icc.proyecto.roles.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyecto.roles.entities.RoleName;

// Solo se consulta: los roles se insertan en la migración V1.
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleName name);

    boolean existsByName(RoleName name);
}