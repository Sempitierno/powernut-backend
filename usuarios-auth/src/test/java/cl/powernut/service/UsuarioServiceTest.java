package cl.powernut.service;

import cl.powernut.dto.LoginRequest;
import cl.powernut.dto.RegistroRequest;
import cl.powernut.exception.CredencialesInvalidasException;
import cl.powernut.model.Rol;
import cl.powernut.model.Usuario;
import cl.powernut.repository.UsuarioRepository;
import cl.powernut.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock RefreshTokenService refresh;
    @InjectMocks UsuarioService service;

    RegistroRequest req = new RegistroRequest("Victor", "VICTOR@Test.cl", "Clave123!", "RM", "Santiago");

    @Test
    void registrar_guardaComoClienteConClaveEncriptada() {
        when(repo.existsByCorreo("victor@test.cl")).thenReturn(false);
        when(encoder.encode("Clave123!")).thenReturn("HASH");
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario u = service.registrar(req);

        assertEquals("victor@test.cl", u.getCorreo());
        assertEquals("HASH", u.getPassword());
        assertEquals(Rol.CLIENTE, u.getRol());
    }

    @Test
    void registrar_correoDuplicado_lanzaErrorYNoGuarda() {
        when(repo.existsByCorreo("victor@test.cl")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(req));
        verify(repo, never()).save(any());
    }

    @Test
    void login_correoInexistente_lanzaCredencialesInvalidas() {
        when(repo.findByCorreo("nadie@test.cl")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie@test.cl", "x")));
    }

    @Test
    void login_claveIncorrecta_lanzaCredencialesInvalidas() {
        Usuario u = new Usuario();
        u.setPassword("HASH");
        when(repo.findByCorreo("victor@test.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("mala", "HASH")).thenReturn(false);
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("victor@test.cl", "mala")));
        verifyNoInteractions(jwt);
    }
}