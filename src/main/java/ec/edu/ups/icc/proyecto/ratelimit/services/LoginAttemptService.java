package ec.edu.ups.icc.proyecto.ratelimit.services;

/*
 * Bloqueo temporal de usuarios tras varios intentos fallidos (Punto 6).
 *
 * A diferencia del rate limiting, que cuenta todas las solicitudes y se
 * reinicia cada minuto, aquí solo se cuentan las credenciales inválidas
 * y el bloqueo dura mucho más.
 */
public interface LoginAttemptService {

    /*
     * Lanza TooManyRequestsException si el correo está bloqueado.
     * Se llama antes de intentar autenticar.
     */
    void checkBlocked(String email);

    // Registra un intento fallido. Se llama cuando las credenciales fallan.
    void registerFailure(String email);

    // Borra el contador. Se llama tras un inicio de sesión exitoso.
    void clearFailures(String email);
}
