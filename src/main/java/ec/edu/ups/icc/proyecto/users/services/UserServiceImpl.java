package ec.edu.ups.icc.proyecto.users.services;

import ec.edu.ups.icc.proyecto.auditlogs.services.AuditLogService;
import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.core.utils.PageableBuilder;
import ec.edu.ups.icc.proyecto.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyecto.roles.entities.RoleName;
import ec.edu.ups.icc.proyecto.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyecto.users.dtos.AssignRolesDto;
import ec.edu.ups.icc.proyecto.users.dtos.ChangeUserStatusDto;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;
import ec.edu.ups.icc.proyecto.users.mappers.UserMapper;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    // Lista blanca de campos permitidos para ordenar.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "email", "status", "createdAt", "updatedAt"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    // Constructor lleno
    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(String search, PaginationDto pagination) {

        Pageable pageable = PageableBuilder.build(pagination, ALLOWED_SORT_FIELDS);

        String normalizedSearch = normalizeSearch(search);

        return userRepository.findWithSearch(normalizedSearch, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findOne(Long id) {
        return userMapper.toResponseDto(findUserOrThrow(id));
    }

    /*
     * PATCH /users/{id}/status
     *
     * Un usuario BLOCKED no puede autenticarse: lo impone
     * UserDetailsImpl.isEnabled() leyendo este mismo campo, así que
     * el efecto es inmediato sin tocar nada de seguridad aparte.
     */
    @Override
    @Transactional
    public UserResponseDto changeStatus(Long id, ChangeUserStatusDto dto, Long actorId) {

        UserEntity user = findUserOrThrow(id);

        UserStatus previousStatus = user.getStatus();

        user.setStatus(dto.getStatus());
        UserEntity saved = userRepository.save(user);

        auditLogService.registerSuccess(actorId, "CHANGE_USER_STATUS", "USER", id,
                previousStatus.name(), dto.getStatus().name());

        return userMapper.toResponseDto(saved);
    }

    /*
     * PUT /users/{id}/roles
     *
     * Reemplaza el conjunto completo de roles del usuario (no agrega
     * ni quita uno por uno), tal como indica el verbo PUT.
     */
    @Override
    @Transactional
    public UserResponseDto assignRoles(Long id, AssignRolesDto dto, Long actorId) {

        UserEntity user = findUserOrThrow(id);

        String previousRoles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(","));

        Set<RoleEntity> newRoles = new HashSet<>();
        for (RoleName roleName : dto.getRoles()) {
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        UserEntity saved = userRepository.save(user);

        String newRolesJoined = newRoles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(","));

        auditLogService.registerSuccess(actorId, "ASSIGN_USER_ROLES", "USER", id,
                previousRoles, newRolesJoined);

        return userMapper.toResponseDto(saved);
    }

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
    }
}