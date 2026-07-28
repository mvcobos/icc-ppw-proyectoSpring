package ec.edu.ups.icc.proyecto.ratelimit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/*
 * StringRedisTemplate no se declara aquí: lo autoconfigura
 * spring-boot-starter-data-redis. Solo se define lo que Spring
 * no puede construir solo.
 */
@Configuration
public class RedisConfig {

    /*
     * Incrementa el contador y le pone TTL en una sola operación
     * atómica, como exige el Punto 7.
     *
     * KEYS[1] = clave del contador
     * ARGV[1] = ventana en segundos
     * ARGV[2] = máximo de solicitudes permitidas
     *
     * Devuelve los segundos que faltan para que expire la ventana
     * si se superó el límite, o -1 si la solicitud se permite.
     */
    @Bean
    public RedisScript<Long> rateLimitScript() {

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        script.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                if current > tonumber(ARGV[2]) then
                    local ttl = redis.call('TTL', KEYS[1])
                    if ttl < 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                        ttl = tonumber(ARGV[1])
                    end
                    return ttl
                end
                return -1
                """);

        script.setResultType(Long.class);

        return script;
    }
}