package ec.edu.ups.icc.proyecto.security.services;

import ec.edu.ups.icc.proyecto.auditlogs.services.AuditLogService;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.proyecto.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.proyecto.refreshtokens.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyecto.refreshtokens.services.RefreshTokenService;
import ec.edu.ups.icc.proyecto.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyecto.roles.entities.RoleName;
import ec.edu.ups.icc.proyecto.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyecto.security.config.JwtProperties;
import ec.edu.ups.icc.proyecto.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyecto.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyecto.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyecto.security.utils.JwtUtil;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.mappers.UserMapper;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    // Constructor lleno
    public AuthServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService,
            AuditLogService auditLogService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto, String clientIp) {
        String normalizedEmail = dto.getEmail().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("El correo ya esta registrado");
        }

        RoleEntity participantRole = roleRepository.findByName(RoleName.PARTICIPANT)
                .orElseThrow(() -> new IllegalStateException("Rol PARTICIPANT no encontrado en la base de datos"));

        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(participantRole);
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.issueRefreshToken(userDetails, clientIp);

        auditLogService.registerSuccess(savedUser.getId(), "REGISTER", "USER", savedUser.getId(), null, null);

        return new AuthResponseDto(accessToken, refreshToken, jwtProperties.getAccessExpiration() / 1000);
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto dto, String clientIp) {
        String normalizedEmail = dto.getEmail().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, dto.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.issueRefreshToken(userDetails, clientIp);

            auditLogService.registerSuccess(userDetails.getId(), "LOGIN", "USER", userDetails.getId(), null, null);

            return new AuthResponseDto(accessToken, refreshToken, jwtProperties.getAccessExpiration() / 1000);

        } catch (Exception ex) {
            /*
             * No se conoce el id del usuario si las credenciales son
             * invalidas (podria ni siquiera existir el correo), por lo
             * que se audita con actorId nulo. La excepcion se relanza
             * tal cual para que GlobalExceptionHandler la traduzca a un
             * 401 con mensaje generico (no revela si el correo existe).
             */
            auditLogService.registerFailure(null, "LOGIN", "USER", null);
            throw ex;
        }
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshRequestDto dto, String clientIp) {
        RefreshTokenEntity currentToken = refreshTokenService.validateActiveToken(dto.getRefreshToken());

        UserEntity user = currentToken.getUser();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = refreshTokenService.rotate(currentToken, userDetails, clientIp);

        return new AuthResponseDto(newAccessToken, newRefreshToken, jwtProperties.getAccessExpiration() / 1000);
    }

    @Override
    @Transactional
    public void logout(RefreshRequestDto dto) {
        RefreshTokenEntity token = refreshTokenService.validateActiveToken(dto.getRefreshToken());
        refreshTokenService.revoke(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto me(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return userMapper.toResponseDto(user);
    }
}