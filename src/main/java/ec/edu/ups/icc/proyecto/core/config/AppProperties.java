package ec.edu.ups.icc.proyecto.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * Mapea las propiedades app.* de application.yml.
 * timezone es la zona de negocio usada para mostrar los instantes
 * almacenados en UTC (Punto 14).
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String timezone;

    // Constructor vacío
    public AppProperties() {
    }

    // Getters y setters
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}