package cl.powernut.service;

import cl.powernut.dto.LoginRequest;
import cl.powernut.dto.LoginResponse;
import cl.powernut.dto.RegistroRequest;
import cl.powernut.exception.CredencialesInvalidasException;
import cl.powernut.model.Rol;
import cl.powernut.model.Usuario;
import cl.powernut.repository.UsuarioRepository;
import cl.powernut.security.JwtService;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Usuario registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        if (repo.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ese correo ya está registrado");
        }
        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setCorreo(correo);
        u.setPassword(encoder.encode(req.password())); // se guarda el hash, no la clave
        u.setRegion(req.region());
        u.setComuna(req.comuna());
        u.setRol(Rol.CLIENTE); // siempre cliente, el rol no viene del front
        return repo.save(u);
    }

    public boolean correoDisponible(String correo) {
        return !repo.existsByCorreo(correo.trim().toLowerCase());
    }

    public LoginResponse login(LoginRequest req){
        Usuario u = repo.findByCorreo(req.correo().trim().toLowerCase())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!encoder.matches(req.password(), u.getPassword())){
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(jwt.generarToken(u), u.getNombre(), u.getCorreo(), u.getRol().name());
    }

    public Usuario buscarPorCorreo(String correo){
        return repo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public List<Usuario> listar(){
        return repo.findAll();
    }

}