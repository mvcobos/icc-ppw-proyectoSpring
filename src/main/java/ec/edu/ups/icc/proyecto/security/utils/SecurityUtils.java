package ec.edu.ups.icc.proyecto.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/*
 * Utilidades de solo lectura sobre el usuario autenticado en el
 * SecurityContext de la petición actual.
 */
public class SecurityUtils {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    // Constructor  vacío
    private SecurityUtils() {
    }

    /*
     * Indica si el usuario autenticado en la petición actual tiene
     * el rol ADMIN. Se usa para el bypass donde un ADMIN puede operar sobre cualquier recurso sin importar
     * recurso sin importar quién sea su dueño.
     */
    public static boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ADMIN_AUTHORITY));
    }
}