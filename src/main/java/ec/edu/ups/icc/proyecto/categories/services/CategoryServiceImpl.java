package ec.edu.ups.icc.proyecto.categories.services;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyecto.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyecto.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyecto.categories.mappers.CategoryMapper;
import ec.edu.ups.icc.proyecto.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.core.utils.PageableBuilder;

@Service
public class CategoryServiceImpl implements CategoryService {

    // Lista de campos permitidos para ordenar.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "createdAt", "updatedAt"
    );

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    // Todas las categorías activas.
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll() {
        return categoryRepository.findByActiveTrue().stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    // Catg activas con paginación y ordenamiento.
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> findAllPage(PaginationDto pagination) {

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        return categoryRepository.findByActiveTrue(pageable)
                .map(categoryMapper::toResponseDto);
    }

    // Busca una categoría activa por su id.
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findOne(Long id) {
        CategoryEntity entity = findActiveCategoryOrThrow(id);
        return categoryMapper.toResponseDto(entity);
    }

    // POST
    // No se permiten categorías duplicadas
    @Override
    @Transactional
    public CategoryResponseDto create(CreateCategoryDto dto) {

        String name = dto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        CategoryEntity entity = categoryMapper.toEntity(dto);
        entity.setName(name);

        return categoryMapper.toResponseDto(categoryRepository.save(entity));
    }

    // PUT
    // Al actualizar se excluye el propio registro del control de duplicados.
    @Override
    @Transactional
    public CategoryResponseDto update(Long id, UpdateCategoryDto dto) {

        CategoryEntity entity = findActiveCategoryOrThrow(id);

        String name = dto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        categoryMapper.updateEntity(entity, dto);
        entity.setName(name);

        return categoryMapper.toResponseDto(categoryRepository.save(entity));
    }

    // DELETE
    // Eliminación lógica: la categoría deja de estar disponible
    // para nuevos eventos, pero los eventos existentes la conservan.
    @Override
    @Transactional
    public void delete(Long id) {

        CategoryEntity entity = findActiveCategoryOrThrow(id);

        entity.setActive(false);
        categoryRepository.save(entity);
    }

    /*
     * Busca una categoría activa.
     *
     * Si no existe o está inactiva, devuelve 404.
     */
    private CategoryEntity findActiveCategoryOrThrow(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}