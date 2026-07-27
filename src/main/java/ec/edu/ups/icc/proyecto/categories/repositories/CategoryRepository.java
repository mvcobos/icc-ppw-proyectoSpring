package ec.edu.ups.icc.proyecto.categories.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.categories.entities.CategoryEntity;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findByActiveTrue();

    Page<CategoryEntity> findByActiveTrue(Pageable pageable);

    Optional<CategoryEntity> findByIdAndActiveTrue(Long id);

    boolean existsByIdAndActiveTrue(Long id);

    // Control de duplicados al crear
    boolean existsByNameIgnoreCase(String name);

    // Control de duplicados al actualizar, excluyendo el propio registro
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}