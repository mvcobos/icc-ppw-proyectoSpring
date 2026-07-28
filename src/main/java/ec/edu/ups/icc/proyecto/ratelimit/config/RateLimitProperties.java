package ec.edu.ups.icc.proyecto.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private Limit login = new Limit();
    private Limit register = new Limit();
    private Limit publicEndpoints = new Limit();
    private Limit authenticated = new Limit();
    private Limit reports = new Limit();

    // Constructor vacío
    public RateLimitProperties() {
    }

    // Getters y setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Limit getLogin() {
        return login;
    }

    public void setLogin(Limit login) {
        this.login = login;
    }

    public Limit getRegister() {
        return register;
    }

    public void setRegister(Limit register) {
        this.register = register;
    }

    public Limit getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(Limit publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    public Limit getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Limit authenticated) {
        this.authenticated = authenticated;
    }

    public Limit getReports() {
        return reports;
    }

    public void setReports(Limit reports) {
        this.reports = reports;
    }

    /*
     * Un límite es un par: cuántas solicitudes se permiten y en qué
     * ventana de tiempo. La ventana define además el TTL de la clave.
     */
    public static class Limit {

        private int maxRequests;
        private long windowSeconds;

        // Constructor vacío
        public Limit() {
        }

        // Getters y setters
        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
}
