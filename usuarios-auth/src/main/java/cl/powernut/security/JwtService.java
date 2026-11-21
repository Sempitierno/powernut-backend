package cl.powernut.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.powernut.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtService {
    private final SecretKey key;
    private final long expMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value ("${jwt.expiration-ms}") long expMs ){
        this.key =
Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
         this.expMs = expMs;
    }

    public String generarToken(Usuario u){
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getCorreo())
                .claim("userId", u.getId())
                .claim("rol", u.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expMs))
                .signWith(key) //firma
                .compact();
    }

    //Lanza excepción si la firma no es válida o el token venció
    public Claims validar (String token){
        return Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token).getPayload();
    }
}
