package ec.edu.ups.icc.proyecto.registrations.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyecto.users.mappers.UserMapper;

/*
 * No hay conversión de entrada: el evento llega en la ruta
 * y el participante desde el usuario autenticado.
 */
@Component
public class RegistrationMapper {

    private final UserMapper userMapper;

    public RegistrationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public RegistrationResponseDto toResponseDto(RegistrationEntity entity) {
        if (entity == null) {
            return null;
        }

        RegistrationResponseDto dto = new RegistrationResponseDto();
        dto.setId(entity.getId());
        dto.setRegistrationCode(entity.getRegistrationCode());
        dto.setParticipant(userMapper.toResponseDto(entity.getParticipant()));
        dto.setStatus(entity.getStatus());
        dto.setRegisteredAt(entity.getRegisteredAt());
        dto.setStatusUpdatedAt(entity.getStatusUpdatedAt());
        dto.setConfirmedAt(entity.getConfirmedAt());
        dto.setCancelledAt(entity.getCancelledAt());

        if (entity.getEvent() != null) {
            dto.setEventId(entity.getEvent().getId());
            dto.setEventTitle(entity.getEvent().getTitle());
        }

        return dto;
    }
}