package cl.powernut.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.powernut.exception.TokenInvalidoException;
import cl.powernut.model.RefreshToken;
import cl.powernut.model.Usuario;
import cl.powernut.repository.RefreshTokenRepository;

@Service 
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long dias;

    public RefreshTokenService(RefreshTokenRepository repo, @Value ("${jwt.refresh-expiration-days}") long dias){
        this.repo = repo;
        this.dias = dias;
    }

    public RefreshToken crear (Usuario u){
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString()); //valor aleatorio imposible de adivinar
        rt.setUsuario(u);
        rt.setExpiracion(Instant.now().plus(dias, ChronoUnit.DAYS));
        return repo.save(rt);
    }

    public RefreshToken validar (String token) {
        RefreshToken rt = repo.findByToken(token).orElseThrow(TokenInvalidoException::new);
        if (rt.isRevocado() ||
rt.getExpiracion().isBefore(Instant.now())) {
            throw new TokenInvalidoException();
        }
        return rt;
    }

    public void revocar(RefreshToken rt){
        rt.setRevocado(true);
        repo.save(rt);
    }

    
}
