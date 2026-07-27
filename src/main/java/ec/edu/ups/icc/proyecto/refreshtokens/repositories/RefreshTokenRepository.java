package ec.edu.ups.icc.proyecto.refreshtokens.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.proyecto.refreshtokens.entities.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    // Búsqueda por el jti del token presentado.
    Optional<RefreshTokenEntity> findByTokenId(UUID tokenId);

    // Búsqueda por el hash, para no almacenar el token original.
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    // Tokens vigentes de un usuario: no revocados y sin expirar.
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(Long userId, OffsetDateTime now);

    /*
     * Revoca de una sola vez todos los tokens vigentes de un usuario.
     *
     * Se usa al cerrar sesión en todos los dispositivos o al
     * detectar la reutilización de un token ya rotado.
     */
    @Modifying
    @Query("""
            UPDATE RefreshTokenEntity t
            SET t.revokedAt = :revokedAt
            WHERE t.user.id = :userId
              AND t.revokedAt IS NULL
            """)
    int revokeAllByUserId(@Param("userId") Long userId, @Param("revokedAt") OffsetDateTime revokedAt);
}