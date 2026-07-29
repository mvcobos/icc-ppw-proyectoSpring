package ec.edu.ups.icc.proyecto.ratelimit.services;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.proyecto.core.exceptions.domain.TooManyRequestsException;
import ec.edu.ups.icc.proyecto.ratelimit.config.LoginBlockProperties;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    /*
     * Prefijo sugerido por el enunciado para identificar la finalidad
     * de la clave. El TTL lo asigna el script Lua.
     */
    private static final String BLOCKED_USER_PREFIX = "blocked-user:";

    private static final String UNKNOWN_IDENTIFIER = "unknown";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;
    private final LoginBlockProperties properties;

    public LoginAttemptServiceImpl(StringRedisTemplate redisTemplate,
            RedisScript<Long> rateLimitScript,
            LoginBlockProperties properties) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
        this.properties = properties;
    }

    /*
     * Solo lee el contador, no lo incrementa: un usuario bloqueado no
     * debe extender su propio castigo al reintentar.
     */
    @Override
    public void checkBlocked(String email) {

        String key = buildKey(email);

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return;
        }

        long failures = Long.parseLong(value);

        if (failures < properties.getMaxFailures()) {
            return;
        }

        Long remaining = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        long retryAfterSeconds = (remaining == null || remaining < 0)
                ? properties.getBlockSeconds()
                : remaining;

        throw new TooManyRequestsException(
                "Cuenta bloqueada temporalmente por intentos fallidos",
                retryAfterSeconds);
    }

    /*
     * Reutiliza el script atómico del rate limiting: incrementa el
     * contador de fallos y le asigna el TTL del bloqueo.
     */
    @Override
    public void registerFailure(String email) {

        redisTemplate.execute(
                rateLimitScript,
                List.of(buildKey(email)),
                String.valueOf(properties.getBlockSeconds()),
                String.valueOf(properties.getMaxFailures()));
    }

    // Un inicio de sesión correcto limpia el historial de fallos.
    @Override
    public void clearFailures(String email) {
        redisTemplate.delete(buildKey(email));
    }

    // El correo se normaliza a minúsculas: la tabla users también lo exige.
    private String buildKey(String email) {

        if (email == null || email.isBlank()) {
            return BLOCKED_USER_PREFIX + UNKNOWN_IDENTIFIER;
        }

        return BLOCKED_USER_PREFIX + email.trim().toLowerCase();
    }
}
