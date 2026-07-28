package ec.edu.ups.icc.proyecto.users.controllers;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.users.dtos.AssignRolesDto;
import ec.edu.ups.icc.proyecto.users.dtos.ChangeUserStatusDto;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import ec.edu.ups.icc.proyecto.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Usuarios",
    description = "Administración de usuarios y roles (uso exclusivo de ADMIN)"
)
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UsersController {

    private final UserService userService;

    // Constructor lleno
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    /*
     * GET /users?search=ana&page=0&size=10
     */
    @Operation(
        summary = "Listar usuarios",
        description = "Devuelve una página de usuarios, con filtro opcional por email o nombre."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de usuarios devuelta exitosamente"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede acceder")
    })
    @GetMapping
    public Page<UserResponseDto> findAll(
            @RequestParam(value = "search", required = false) String search,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return userService.findAll(search, pagination);
    }

    /*
     * GET /users/{id}
     */
    @Operation(
        summary = "Buscar usuario por ID",
        description = "Devuelve los datos de un usuario, sin exponer su contraseña."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public UserResponseDto findOne(@PathVariable("id") Long id) {
        return userService.findOne(id);
    }

    /*
     * PATCH /users/{id}/status
     */
    @Operation(
        summary = "Cambiar estado de un usuario",
        description = "Activa o bloquea un usuario. Un usuario BLOCKED no puede autenticarse."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/status")
    public UserResponseDto changeStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeUserStatusDto dto,
            @AuthenticationPrincipal UserDetailsImpl actor
    ) {
        return userService.changeStatus(id, dto, actor.getId());
    }

    /*
     * PUT /users/{id}/roles
     */
    @Operation(
        summary = "Asignar roles a un usuario",
        description = "Reemplaza el conjunto completo de roles del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario o rol no encontrado")
    })
    @PutMapping("/{id}/roles")
    public UserResponseDto assignRoles(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignRolesDto dto,
            @AuthenticationPrincipal UserDetailsImpl actor
    ) {
        return userService.assignRoles(id, dto, actor.getId());
    }
}