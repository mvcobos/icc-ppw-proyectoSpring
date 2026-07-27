package ec.edu.ups.icc.proyecto.core.utils;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.BadRequestException;

/*
 * Construye objetos Pageable a partir de PaginationDto
 *
 * Cada servicio pasa su propia lista blanca de campos ordenables,
 * para evitar ordenar por campos inexistentes o por relaciones.
 */
public final class PageableBuilder {

    private PageableBuilder() {
    }

    public static Pageable build(PaginationDto pagination, Set<String> allowedSortFields) {

        String sortBy = normalizeSortBy(pagination.getSortBy(), allowedSortFields);

        Sort.Direction direction = normalizeDirection(pagination.getDirection());

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(
                pagination.getPage(),
                pagination.getSize(),
                sort
        );
    }

    // Valida que el campo de ordenamiento exista y esté permitido.
    private static String normalizeSortBy(String sortBy, Set<String> allowedSortFields) {

        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        }

        return sortBy;
    }

    // Convierte la dirección recibida por query param en Sort.Direction.
    private static Sort.Direction normalizeDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }

        throw new BadRequestException("Dirección de ordenamiento no válida: " + direction);
    }
}