package ec.edu.ups.icc.proyecto.security.dtos;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequestDto {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    // Constructor vacío
    public RefreshRequestDto() {
    }

    // Getters y setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}