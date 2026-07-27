package ec.edu.ups.icc.proyecto.refreshtokens.services;

import ec.edu.ups.icc.proyecto.refreshtokens.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;

// La clase encargada de emitir, validar, rotar y revocar los refresh tokens.

public interface RefreshTokenService {

    /*
     * Genera un refresh token JWT, guarda su hash en base de datos y
     * devuelve el token en claro (solo se entrega una vez al cliente)
     */
    String issueRefreshToken(UserDetailsImpl userDetails, String createdByIp);

    /*
     * Valida firma, expiracion, tipo, y que siga activo en base de datos
     * (no revocado ni reemplazado) Lanza BadCredentialsException si falla
     * cualquiera de esas condiciones
     */
    RefreshTokenEntity validateActiveToken(String rawRefreshToken);

    /*
     * Revoca el token actual y emite uno nuevo enlazado mediante
     * replacedByTokenId (rotacion en cada refresh)
     */
    String rotate(RefreshTokenEntity currentToken, UserDetailsImpl userDetails, String requestIp);

    // Revoca un unico token (logout de la sesion actual)
    void revoke(RefreshTokenEntity token);

    // Revoca todos los refresh tokens activos de un usuario (logout global)
    void revokeAllForUser(Long userId);
}