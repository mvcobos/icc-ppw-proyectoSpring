package ec.edu.ups.icc.proyecto.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * Mapea las propiedades login-block.* de application.yml.
 * Configura el bloqueo temporal por intentos fallidos (Punto 6).
 */
@Component
@ConfigurationProperties(prefix = "login-block")
public class LoginBlockProperties {

    private int maxFailures = 5;
    private long blockSeconds = 900;

    // Constructor vacío
    public LoginBlockProperties() {
    }

    // Getters y setters
    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    public long getBlockSeconds() {
        return blockSeconds;
    }

    public void setBlockSeconds(long blockSeconds) {
        this.blockSeconds = blockSeconds;
    }
}
