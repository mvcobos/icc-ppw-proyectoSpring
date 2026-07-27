package ec.edu.ups.icc.proyecto.events.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.categories.mappers.CategoryMapper;
import ec.edu.ups.icc.proyecto.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.users.mappers.UserMapper;

/*
 * No asigna organizer ni category: esas relaciones las resuelve
 * el servicio consultando los repositorios correspondientes.
 */
@Component
public class EventMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    public EventMapper(UserMapper userMapper, CategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
    }

    public EventEntity toEntity(CreateEventDto dto) {
        if (dto == null) {
            return null;
        }

        EventEntity entity = new EventEntity();
        applyDto(entity, dto);
        entity.setAvailableCapacity(dto.getCapacity());
        return entity;
    }

    /*
     * No toca id, status, deleted, availableCapacity ni las relaciones.
     */
    public void updateEntity(EventEntity entity, CreateEventDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        applyDto(entity, dto);
    }

    private void applyDto(EventEntity entity, CreateEventDto dto) {
        entity.setTitle(dto.getTitle().trim());
        entity.setDescription(dto.getDescription().trim());
        entity.setModality(dto.getModality());
        entity.setLocation(normalize(dto.getLocation()));
        entity.setVirtualUrl(normalize(dto.getVirtualUrl()));
        entity.setCapacity(dto.getCapacity());
        entity.setRegistrationStartAt(dto.getRegistrationStartAt());
        entity.setRegistrationEndAt(dto.getRegistrationEndAt());
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
    }

    /*
     * Convierte un texto vacío en null.
     *
     * La base exige null, no cadena vacía, en location y virtualUrl
     * según la modalidad (chk_events_modality_data).
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public EventResponseDto toResponseDto(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        EventResponseDto dto = new EventResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setModality(entity.getModality());
        dto.setLocation(entity.getLocation());
        dto.setVirtualUrl(entity.getVirtualUrl());
        dto.setCapacity(entity.getCapacity());
        dto.setAvailableCapacity(entity.getAvailableCapacity());
        dto.setRegistrationStartAt(entity.getRegistrationStartAt());
        dto.setRegistrationEndAt(entity.getRegistrationEndAt());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setStatus(entity.getStatus());
        dto.setOrganizer(userMapper.toResponseDto(entity.getOrganizer()));
        dto.setCategory(categoryMapper.toResponseDto(entity.getCategory()));
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}