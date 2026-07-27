package ec.edu.ups.icc.proyecto.security.services;

import ec.edu.ups.icc.proyecto.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.entities.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/*
 * Adaptador entre UserEntity y el contrato UserDetails que Spring Security
 * necesita para autenticar y autorizar.
 *
 * No se persiste ni se expone directamente: es un objeto de sesion en memoria,
 * construido a partir del UserEntity ya cargado desde la base de datos.
 */
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final Collection<GrantedAuthority> authorities;

    // Constructor lleno
    public UserDetailsImpl(Long id, String email, String passwordHash,
            UserStatus status, Collection<GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.authorities = authorities;
    }

    /*
     * Construye el UserDetails a partir de la entidad, anteponiendo ROLE_
     * a cada nombre de rol (en la tabla roles estan sin ese prefijo).
     */
    public static UserDetailsImpl build(UserEntity user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(RoleEntity::getName)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                authorities);
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /*
     * Un usuario BLOCKED no debe poder autenticarse.
     * Se modela con isEnabled(), que Spring Security revisa automaticamente
     * durante la autenticacion y responde con DisabledException.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}