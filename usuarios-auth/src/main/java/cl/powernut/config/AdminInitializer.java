package cl.powernut.config;

import cl.powernut.model.Rol;
import cl.powernut.model.Usuario;
import cl.powernut.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminInitializer(UsuarioRepository repo, PasswordEncoder encoder,
                            @Value("${admin.email}") String email,
                            @Value("${admin.password}") String password) {
        this.repo = repo;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!repo.existsByCorreo(email)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo(email);
            admin.setPassword(encoder.encode(password));
            admin.setRol(Rol.ADMIN);
            repo.save(admin);
            System.out.println(">> Admin inicial creado: " + email);
        }
    }
}