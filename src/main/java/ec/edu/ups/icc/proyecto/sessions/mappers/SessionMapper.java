package ec.edu.ups.icc.proyecto.sessions.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyecto.sessions.entities.SessionEntity;

// No asigna el evento: esa relación la resuelve el servicio.
@Component
public class SessionMapper {

    public SessionEntity toEntity(CreateSessionDto dto) {
        if (dto == null) {
            return null;
        }

        SessionEntity entity = new SessionEntity();
        applyDto(entity, dto);
        return entity;
    }

    /*
     * Aplica los cambios de un PUT sobre una entidad ya cargada.
     * No toca id ni el evento al que pertenece.
     */
    public void updateEntity(SessionEntity entity, CreateSessionDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        applyDto(entity, dto);
    }

    private void applyDto(SessionEntity entity, CreateSessionDto dto) {
        entity.setTitle(dto.getTitle().trim());
        entity.setDescription(normalize(dto.getDescription()));
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setLocation(normalize(dto.getLocation()));
        entity.setVirtualUrl(normalize(dto.getVirtualUrl()));
    }

    // texto vacío a null.
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public SessionResponseDto toResponseDto(SessionEntity entity) {
        if (entity == null) {
            return null;
        }

        SessionResponseDto dto = new SessionResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setLocation(entity.getLocation());
        dto.setVirtualUrl(entity.getVirtualUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getEvent() != null) {
            dto.setEventId(entity.getEvent().getId());
            dto.setEventTitle(entity.getEvent().getTitle());
        }

        return dto;
    }
}