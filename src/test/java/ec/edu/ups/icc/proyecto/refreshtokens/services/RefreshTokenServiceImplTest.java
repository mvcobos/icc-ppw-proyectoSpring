package ec.edu.ups.icc.proyecto.refreshtokens.services;

import ec.edu.ups.icc.proyecto.refreshtokens.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyecto.refreshtokens.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.proyecto.security.config.JwtProperties;
import ec.edu.ups.icc.proyecto.security.utils.JwtUtil;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void validateActiveToken_conTokenRevocado_lanzaBadCredentialsException() {
        // Arrange
        String rawToken = "un.token.jwt.valido.en.firma";

        UserEntity user = new UserEntity();

        RefreshTokenEntity revoked = new RefreshTokenEntity(
                UUID.randomUUID(), user, "hash-cualquiera",
                OffsetDateTime.now().plusDays(7));
        revoked.setRevokedAt(OffsetDateTime.now().minusMinutes(5)); // ya revocado

        when(jwtUtil.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.validateActiveToken(rawToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("invalido o expirado");
    }

    @Test
    void validateActiveToken_conTokenExpirado_lanzaBadCredentialsException() {
        // Arrange
        String rawToken = "otro.token.jwt.valido.en.firma";

        UserEntity user = new UserEntity();

        RefreshTokenEntity expired = new RefreshTokenEntity(
                UUID.randomUUID(), user, "otro-hash",
                OffsetDateTime.now().minusMinutes(1)); // expiro hace 1 minuto

        when(jwtUtil.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.validateActiveToken(rawToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("invalido o expirado");
    }

    @Test
    void validateActiveToken_conFirmaInvalida_lanzaBadCredentialsExceptionSinConsultarBd() {
        // Arrange: el propio jjwt rechaza el token antes de tocar la BD.
        String rawToken = "token.con.firma.invalida";

        when(jwtUtil.validateRefreshToken(rawToken)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.validateActiveToken(rawToken))
                .isInstanceOf(BadCredentialsException.class);
    }
}