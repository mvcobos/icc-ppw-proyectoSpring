package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;
import org.springframework.http.HttpStatus;

/*
 * TODO E3: Excepción que se lanza cuando Redis detecta que se
 * supero el limite de solicitudes. retryAfterSeconds alimenta el
 * encabezado Retry-After que exige el enunciado.
 */
public class TooManyRequestsException extends ApplicationException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}