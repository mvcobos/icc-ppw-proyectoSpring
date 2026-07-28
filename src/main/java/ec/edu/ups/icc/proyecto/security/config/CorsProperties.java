package ec.edu.ups.icc.proyecto.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * Mapea la propiedad cors.allowed-origins de application-dev.yml y
 * application-prod.yml, alimentada por ALLOWED_ORIGINS (Puntos 8 y 16).
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of();

    // Constructor vacío
    public CorsProperties() {
    }

    // Getters y setters
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}