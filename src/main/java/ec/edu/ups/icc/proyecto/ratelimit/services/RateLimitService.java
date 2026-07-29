package ec.edu.ups.icc.proyecto.ratelimit.services;
/*
 * Contadores de solicitudes en Redis (Puntos 6 y 7).
 *
 * Cada método incrementa el contador de su operación y lanza
 * TooManyRequestsException cuando se supera el límite configurado.
 */
public interface RateLimitService {

    // Inicio de sesión: 5 solicitudes por minuto, por IP y correo.
    void checkLogin(String ip, String email);

    // Registro: 3 solicitudes por hora, por IP.
    void checkRegister(String ip);

    // Endpoints públicos: 60 solicitudes por minuto, por IP.
    void checkPublicEndpoint(String ip);

    // Endpoints autenticados: 120 solicitudes por minuto, por usuario.
    void checkAuthenticatedUser(Long userId);

    // Generación de reportes: 5 solicitudes por minuto, por usuario.
    void checkReports(Long userId);
}