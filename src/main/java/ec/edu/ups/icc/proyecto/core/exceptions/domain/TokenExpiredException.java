package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;
import org.springframework.http.HttpStatus;

/*
 * El token JWT es valido pero ya expiro.
 * Se distingue de InvalidTokenException porque el cliente debe
 * reaccionar distinto: aqui corresponde usar el refresh token,
 * no volver a iniciar sesion desde cero.
 */
public class TokenExpiredException extends ApplicationException {
    public TokenExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", message);
    }
}