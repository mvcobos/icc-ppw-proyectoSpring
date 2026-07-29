package ec.edu.ups.icc.proyecto.ratelimit.interceptors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ec.edu.ups.icc.proyecto.ratelimit.services.RateLimitService;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Aplica los límites del Punto 7 antes de que el controlador procese
 * la solicitud.
 *
 * Se ejecuta dentro del DispatcherServlet, después de la cadena de
 * Spring Security: por eso el SecurityContext ya sabe si la petición
 * es de un usuario autenticado o de una IP anónima, y por eso la
 * excepción llega al @RestControllerAdvice.
 *
 * Login y registro no pasan por aquí: se controlan en AuthServiceImpl,
 * donde además del IP se conoce el correo.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String REPORTS_PATH = "/reports/";
    private static final String CERTIFICATE_SUFFIX = "certificate.pdf";

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) {

        UserDetailsImpl currentUser = getCurrentUser();

        if (currentUser == null) {
            rateLimitService.checkPublicEndpoint(request.getRemoteAddr());
            return true;
        }

        if (isReportRequest(request)) {
            rateLimitService.checkReports(currentUser.getId());
            return true;
        }

        rateLimitService.checkAuthenticatedUser(currentUser.getId());

        return true;
    }

    /*
     * Devuelve el usuario autenticado o null si la petición es anónima.
     * El principal "anonymousUser" es un String, no un UserDetailsImpl.
     */
    private UserDetailsImpl getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (!(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }

        return userDetails;
    }

    /*
     * Los reportes tienen un límite más bajo porque generan archivos:
     * son mucho más costosos que una consulta normal.
     */
    private boolean isReportRequest(HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.contains(REPORTS_PATH) || path.endsWith(CERTIFICATE_SUFFIX);
    }
}