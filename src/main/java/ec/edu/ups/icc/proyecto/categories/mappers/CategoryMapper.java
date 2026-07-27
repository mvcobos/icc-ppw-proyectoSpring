package ec.edu.ups.icc.proyecto.categories.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyecto.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyecto.categories.entities.CategoryEntity;

@Component
public class CategoryMapper {

    public CategoryEntity toEntity(CreateCategoryDto dto) {
        if (dto == null) {
            return null;
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(true);
        return entity;
    }

    public void updateEntity(CategoryEntity entity, UpdateCategoryDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }

    public CategoryResponseDto toResponseDto(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CategoryResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}