package cl.powernut.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.powernut.model.Usuario;



public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByCorreo(String correo); // si también lo usas
}
