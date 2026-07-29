package ec.edu.ups.icc.proyecto.ratelimit.services;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.proyecto.core.exceptions.domain.TooManyRequestsException;
import ec.edu.ups.icc.proyecto.ratelimit.config.RateLimitProperties;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    /*
     * Prefijos que identifican la finalidad de cada clave (Punto 6).
     * El TTL lo asigna el propio script Lua.
     */
    private static final String LOGIN_PREFIX = "rate-limit:login:";
    private static final String REGISTER_PREFIX = "rate-limit:register:";
    private static final String PUBLIC_PREFIX = "rate-limit:public:";
    private static final String USER_PREFIX = "rate-limit:user:";
    private static final String REPORTS_PREFIX = "rate-limit:reports:";

    private static final String UNKNOWN_IDENTIFIER = "unknown";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;
    private final RateLimitProperties properties;

    public RateLimitServiceImpl(StringRedisTemplate redisTemplate,
            RedisScript<Long> rateLimitScript,
            RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
        this.properties = properties;
    }

    /*
     * El login se identifica por IP y correo a la vez: así un atacante
     * no agota el límite de un usuario legítimo desde otra IP.
     */
    @Override
    public void checkLogin(String ip, String email) {
        String key = LOGIN_PREFIX + normalize(ip) + ":" + normalizeEmail(email);

        check(key, properties.getLogin());
    }

    @Override
    public void checkRegister(String ip) {
        check(REGISTER_PREFIX + normalize(ip), properties.getRegister());
    }

    @Override
    public void checkPublicEndpoint(String ip) {
        check(PUBLIC_PREFIX + normalize(ip), properties.getPublicEndpoints());
    }

    @Override
    public void checkAuthenticatedUser(Long userId) {
        check(USER_PREFIX + userId, properties.getAuthenticated());
    }

    @Override
    public void checkReports(Long userId) {
        check(REPORTS_PREFIX + userId, properties.getReports());
    }

    /*
     * Ejecuta el contador atómico en Redis.
     *
     * El script devuelve -1 cuando la solicitud se permite, o los
     * segundos que faltan para reiniciar la ventana cuando no.
     */
    private void check(String key, RateLimitProperties.Limit limit) {

        if (!properties.isEnabled()) {
            return;
        }

        Long retryAfterSeconds = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(limit.getWindowSeconds()),
                String.valueOf(limit.getMaxRequests()));

        if (retryAfterSeconds != null && retryAfterSeconds >= 0) {
            throw new TooManyRequestsException(
                    "Límite de solicitudes excedido", retryAfterSeconds);
        }
    }

    /*
     * Evita claves con valores nulos o vacíos, que agruparían
     * peticiones de origen distinto en un mismo contador.
     */
    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return UNKNOWN_IDENTIFIER;
        }

        return value.trim();
    }

    // El correo se normaliza a minúsculas: la tabla users también lo exige.
    private String normalizeEmail(String email) {
        return normalize(email).toLowerCase();
    }
}