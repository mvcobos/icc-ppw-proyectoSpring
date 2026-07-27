package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;
import org.springframework.http.HttpStatus;

/*
 * Acceso denegado por regla de negocio explicita (no por falta de rol
 * ni por @PreAuthorize, que ya manejan AuthorizationDeniedException
 * y AccessDeniedException). Disponible para casos futuros donde se
 * necesite un codigo interno propio en vez del generico ACCESS_DENIED.
 */
public class ForbiddenException extends ApplicationException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}