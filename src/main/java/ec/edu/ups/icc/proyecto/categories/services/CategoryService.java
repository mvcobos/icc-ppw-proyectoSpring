package ec.edu.ups.icc.proyecto.categories.services;

import java.util.List;

import org.springframework.data.domain.Page;

import ec.edu.ups.icc.proyecto.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyecto.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;

public interface CategoryService {

    List<CategoryResponseDto> findAll();

    Page<CategoryResponseDto> findAllPage(PaginationDto pagination);

    CategoryResponseDto findOne(Long id);

    CategoryResponseDto create(CreateCategoryDto dto);

    CategoryResponseDto update(Long id, UpdateCategoryDto dto);

    // Elimina lógicamente una categoría (active = false).
    void delete(Long id);
}