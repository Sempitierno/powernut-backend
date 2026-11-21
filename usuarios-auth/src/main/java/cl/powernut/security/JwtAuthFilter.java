package cl.powernut.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component 
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt){
        this.jwt = jwt;
    }

    @Override 
    protected void doFilterInternal(HttpServletRequest req,
HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException{
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")){
            try{
                Claims c = jwt.validar(header.substring(7));
                String rol = c.get("rol", String.class);
                var auth = new UsernamePasswordAuthenticationToken(
                    c.getSubject(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

SecurityContextHolder.getContext().setAuthentication(auth);
            } catch(JwtException e){
                SecurityContextHolder.clearContext(); //token alterado o vencido: queda sin autenticar
            }
        }
        chain.doFilter(req, res);
    }

    
}
