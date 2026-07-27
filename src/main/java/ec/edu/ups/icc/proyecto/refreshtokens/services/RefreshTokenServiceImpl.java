package ec.edu.ups.icc.proyecto.refreshtokens.services;

import ec.edu.ups.icc.proyecto.refreshtokens.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyecto.refreshtokens.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.proyecto.security.config.JwtProperties;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import ec.edu.ups.icc.proyecto.security.utils.JwtUtil;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    // Constructor lleno
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public String issueRefreshToken(UserDetailsImpl userDetails, String createdByIp) {
        /*
         * getReferenceById no hace SELECT: entrega un proxy suficiente
         * para asignar la relacion sin recargar el usuario completo.
         */
        UserEntity user = userRepository.getReferenceById(userDetails.getId());

        String rawToken = jwtUtil.generateRefreshToken(userDetails);

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setTokenId(UUID.randomUUID());
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(OffsetDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshExpiration())));
        entity.setCreatedByIp(createdByIp);

        refreshTokenRepository.save(entity);

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenEntity validateActiveToken(String rawRefreshToken) {
        if (!jwtUtil.validateRefreshToken(rawRefreshToken)) {
            throw new BadCredentialsException("Refresh token invalido o expirado");
        }

        RefreshTokenEntity entity = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalido o expirado"));

        /*
         * Chequeo en BD ademas del chequeo criptografico: un token puede
         * ser aun válido segun su firma/expiracion pero ya fue revocado
         * o reemplazado por rotacion. isActive(now) cubre ambos casos.
         */
        if (!entity.isActive(OffsetDateTime.now())) {
            throw new BadCredentialsException("Refresh token invalido o expirado");
        }

        return entity;
    }

    @Override
    @Transactional
    public String rotate(RefreshTokenEntity currentToken, UserDetailsImpl userDetails, String requestIp) {
        String newRawToken = issueRefreshToken(userDetails, requestIp);

        RefreshTokenEntity newEntity = refreshTokenRepository.findByTokenHash(hash(newRawToken))
                .orElseThrow(() -> new IllegalStateException("El token recien emitido no se pudo recuperar"));

        currentToken.setRevokedAt(OffsetDateTime.now());
        currentToken.setReplacedByTokenId(newEntity.getTokenId());
        refreshTokenRepository.save(currentToken);

        return newRawToken;
    }

    @Override
    @Transactional
    public void revoke(RefreshTokenEntity token) {
        token.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now());
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash no disponible", ex);
        }
    }
}