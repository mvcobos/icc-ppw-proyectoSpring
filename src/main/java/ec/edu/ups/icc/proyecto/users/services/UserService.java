package ec.edu.ups.icc.proyecto.users.services;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.users.dtos.AssignRolesDto;
import ec.edu.ups.icc.proyecto.users.dtos.ChangeUserStatusDto;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import org.springframework.data.domain.Page;

/*
 * Administración de usuarios y roles (Punto 3, endpoints asignados a E2).
 * Todos los métodos son de uso exclusivo de ADMIN (verificado en el
 * controller con @PreAuthorize).
 */
public interface UserService {

    Page<UserResponseDto> findAll(String search, PaginationDto pagination);

    UserResponseDto findOne(Long id);

    UserResponseDto changeStatus(Long id, ChangeUserStatusDto dto, Long actorId);

    UserResponseDto assignRoles(Long id, AssignRolesDto dto, Long actorId);
}