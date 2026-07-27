package ec.edu.ups.icc.proyecto.users.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserResponseDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getStatus()
        );
    }
}