package ec.edu.ups.icc.proyecto.security.services;

import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 * Punto de entrada que Spring Security usa durante la autenticacion para
 * cargar un usuario a partir del identificador (aqui, el email).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor lleno (inyeccion por constructor, sin @Autowired)
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * El email se normaliza a minusculas antes de buscar, igual que se
     * normaliza al registrar (la base lo exige con chk_users_email_lowercase).
     *
     * Si no existe, se lanza UsernameNotFoundException: Spring Security la
     * captura internamente y la homologa con "credenciales invalidas" antes
     * de que llegue al cliente, por lo que el mensaje generico del punto 4
     * se cumple sin trabajo adicional aqui.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return UserDetailsImpl.build(user);
    }
}