package ec.edu.ups.icc.proyecto.categories.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyecto.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyecto.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.services.CategoryService;
import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Categorías",
    description = "Gestión de categorías de eventos"
)
@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final CategoryService categoryService;

    public CategoriesController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
        summary = "Listar categorías activas",
        description = "Devuelve todas las categorías disponibles."
    )
    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.findAll();
    }

    /*
     * Listado paginado
     * GET /categories/page?page=0&size=5&sortBy=name&direction=desc
     * @ModelAtribute: vincular los datos de una petición HTTP a un objeto Java
     */
    @Operation(
        summary = "Listar categorías paginadas",
        description = "Devuelve una página de categorías según parámetros de paginación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de categorías devuelta exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos")
    })
    @GetMapping("/page")
    public Page<CategoryResponseDto> findAllPage(@Valid @ModelAttribute PaginationDto pagination) {
        return categoryService.findAllPage(pagination);
    }

    @Operation(
        summary = "Buscar categoría por ID",
        description = "Devuelve una categoría según su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría encontrada exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public CategoryResponseDto findOne(@PathVariable("id") Long id) {
        return categoryService.findOne(id);
    }

    /*
     * POST /categories
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear nueva categoría",
        description = "Crea una categoría. El nombre no puede estar repetido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Categoría creada exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de la categoría inválidos"),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una categoría con ese nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@Valid @RequestBody CreateCategoryDto dto) {
        return categoryService.create(dto);
    }

    /*
     * PUT /categories/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar categoría",
        description = "Actualiza completamente una categoría existente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría actualizada exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada"),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una categoría con ese nombre")
    })
    @PutMapping("/{id}")
    public CategoryResponseDto update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCategoryDto dto
    ) {
        return categoryService.update(id, dto);
    }

    /*
     * DELETE /categories/{id}
     *
     * Eliminación lógica: active = false.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina lógicamente una categoría según su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Categoría eliminada exitosamente"),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
    }
}