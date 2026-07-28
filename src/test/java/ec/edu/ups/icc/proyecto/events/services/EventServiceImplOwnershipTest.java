package ec.edu.ups.icc.proyecto.events.services;

import ec.edu.ups.icc.proyecto.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.proyecto.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyecto.events.entities.EventEntity;
import ec.edu.ups.icc.proyecto.events.mappers.EventMapper;
import ec.edu.ups.icc.proyecto.events.repositories.EventRepository;
import ec.edu.ups.icc.proyecto.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyecto.users.entities.UserEntity;
import ec.edu.ups.icc.proyecto.users.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplOwnershipTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @AfterEach
    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    @Test
    void update_conAdmin_bypasseaLaValidacionDePropiedad() {
        // Arrange
        UserEntity organizer = new UserEntity();
        organizer.setId(2L); // dueño real del evento

        EventEntity event = new EventEntity();
        event.setOrganizer(organizer);

        mockCurrentUserRole("ROLE_ADMIN");

        // Act & Assert: el ADMIN (id=99, distinto al organizador real) invoca validateOwnership.
        // Se espera que NO lance AccessDeniedException por el bypass de rol.
        assertThatCode(() -> invokeValidateOwnership(event, 99L))
                .doesNotThrowAnyException();
    }

    @Test
    void update_conOrganizadorAjeno_lanzaAccessDeniedException() {
        // Arrange
        UserEntity organizer = new UserEntity();
        organizer.setId(2L); // dueño real del evento

        EventEntity event = new EventEntity();
        event.setOrganizer(organizer);

        mockCurrentUserRole("ROLE_ORGANIZER");

        // Act & Assert: organizador id=7 intenta tocar el evento del id=2.
        assertThatThrownBy(() -> invokeValidateOwnership(event, 7L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("otro organizador");
    }

    /*
     * validateOwnership es privado: se invoca por reflexion para no
     * tener que exponerlo solo para el test. Alternativa mas limpia
     * si se prefiere: cambiar su visibilidad a package-private.
     */
    private void invokeValidateOwnership(EventEntity event, Long currentUserId) throws Exception {
        Method method = EventServiceImpl.class
                .getDeclaredMethod("validateOwnership", EventEntity.class, Long.class);
        method.setAccessible(true);

        try {
            method.invoke(eventService, event, currentUserId);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeEx) {
                throw runtimeEx;
            }
            throw ex;
        }
    }

    private void mockCurrentUserRole(String authority) {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);
        doReturn(List.of(grantedAuthority)).when(authentication).getAuthorities();

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }
}