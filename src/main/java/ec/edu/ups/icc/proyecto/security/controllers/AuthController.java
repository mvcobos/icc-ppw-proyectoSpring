package ec.edu.ups.icc.proyecto.security.controllers;

import ec.edu.ups.icc.proyecto.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyecto.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyecto.security.services.AuthService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticacion", description = "Registro, login, renovacion y cierre de sesion")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor lleno
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     * POST /auth/register
     */
    @Operation(
        summary = "Registrar un nuevo usuario",
        description = "Crea un usuario con el rol PARTICIPANT por defecto y devuelve sus tokens."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de registro invalidos"),
        @ApiResponse(responseCode = "409", description = "El correo ya esta registrado")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto dto, HttpServletRequest request) {
        return authService.register(dto, request.getRemoteAddr());
    }

    /*
     * POST /auth/login
     */
    @Operation(
        summary = "Iniciar sesion",
        description = "Autentica al usuario y devuelve access token y refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticacion exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto dto, HttpServletRequest request) {
        return authService.login(dto, request.getRemoteAddr());
    }

    /*
     * POST /auth/refresh
     */
    @Operation(
        summary = "Renovar tokens",
        description = "Valida el refresh token, lo rota y entrega un nuevo par de tokens."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens renovados"),
        @ApiResponse(responseCode = "401", description = "Refresh token invalido, expirado o revocado")
    })
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshRequestDto dto, HttpServletRequest request) {
        return authService.refresh(dto, request.getRemoteAddr());
    }

    /*
     * POST /auth/logout
     */
    @Operation(
        summary = "Cerrar sesion",
        description = "Revoca el refresh token presentado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sesion cerrada"),
        @ApiResponse(responseCode = "401", description = "Refresh token invalido")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequestDto dto) {
        authService.logout(dto);
    }

    /*
     * GET /auth/me
     */
    @Operation(
        summary = "Usuario autenticado actual",
        description = "Devuelve los datos del usuario que realiza la peticion."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos del usuario"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return authService.me(userDetails.getId());
    }
}