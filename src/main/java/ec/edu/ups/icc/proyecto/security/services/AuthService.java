package ec.edu.ups.icc.proyecto.security.services;

import ec.edu.ups.icc.proyecto.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyecto.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto dto, String clientIp);

    AuthResponseDto login(LoginRequestDto dto, String clientIp);

    AuthResponseDto refresh(RefreshRequestDto dto, String clientIp);

    void logout(RefreshRequestDto dto);

    UserResponseDto me(Long userId);
}