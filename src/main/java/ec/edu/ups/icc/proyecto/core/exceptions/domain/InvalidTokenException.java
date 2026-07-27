package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;
import org.springframework.http.HttpStatus;

/*
 * El token JWT tiene un formato invalido, o no corresponde
 * a este emisor (issuer).
 */
public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message);
    }
}