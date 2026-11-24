package cl.powernut.service;

import cl.powernut.dto.LoginRequest;
import cl.powernut.dto.LoginResponse;
import cl.powernut.dto.RegistroRequest;
import cl.powernut.exception.CredencialesInvalidasException;
import cl.powernut.model.RefreshToken;
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
    private final RefreshTokenService refresh;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt, RefreshTokenService refresh) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refresh = refresh;
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

    public LoginResponse login(LoginRequest req) {
    Usuario u = repo.findByCorreo(req.correo().trim().toLowerCase())
            .orElseThrow(CredencialesInvalidasException::new);
    if (!encoder.matches(req.password(), u.getPassword())) {
        throw new CredencialesInvalidasException();
    }
    return respuestaConTokens(u);
}

public LoginResponse renovar(String refreshToken) {
    RefreshToken rt = refresh.validar(refreshToken);
    refresh.revocar(rt);                 // rotación: el viejo ya no sirve
    return respuestaConTokens(rt.getUsuario());
}

public void logout(String refreshToken) {
    refresh.revocar(refresh.validar(refreshToken));
}

private LoginResponse respuestaConTokens(Usuario u) {
    return new LoginResponse(
            jwt.generarToken(u),
            refresh.crear(u).getToken(),
            u.getNombre(), u.getCorreo(), u.getRol().name());
}

    public Usuario buscarPorCorreo(String correo){
        return repo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public List<Usuario> listar(){
        return repo.findAll();
    }

}