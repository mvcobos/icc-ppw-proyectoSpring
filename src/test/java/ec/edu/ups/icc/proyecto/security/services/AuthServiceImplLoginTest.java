package ec.edu.ups.icc.proyecto.security.services;

import ec.edu.ups.icc.proyecto.auditlogs.services.AuditLogService;
import ec.edu.ups.icc.proyecto.refreshtokens.services.RefreshTokenService;
import ec.edu.ups.icc.proyecto.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyecto.security.config.JwtProperties;
import ec.edu.ups.icc.proyecto.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyecto.security.utils.JwtUtil;
import ec.edu.ups.icc.proyecto.users.mappers.UserMapper;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLoginTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_conCredencialesInvalidas_lanzaBadCredentialsExceptionYRegistraFallo() {
        // Arrange
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("inexistente@academic.test");
        dto.setPassword("ClaveIncorrecta1*");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(dto, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        /*
         * El actorId es null porque no se sabe si el usuario existe:
         * esto es justamente lo que evita revelar si el correo esta
         * registrado (Punto 4).
         */
        verify(auditLogService).registerFailure(isNull(), eq("LOGIN"), eq("USER"), isNull());
        verifyNoInteractions(refreshTokenService);
    }
}