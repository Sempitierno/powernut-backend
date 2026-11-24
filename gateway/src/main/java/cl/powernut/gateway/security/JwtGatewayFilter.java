package cl.powernut.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final SecretKey key;

    public JwtGatewayFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        HttpMethod method = req.getMethod();

        // 1. Borra cualquier X-User-* que mande el cliente: solo el gateway puede ponerlos
        ServerHttpRequest.Builder builder = req.mutate().headers(h -> {
            h.remove("X-User-Id");
            h.remove("X-User-Email");
            h.remove("X-User-Rol");
        });

        // 2. Rutas públicas: login/registro, ver productos y el preflight de CORS
        boolean publica = HttpMethod.OPTIONS.equals(method)
                || path.startsWith("/auth/")
                || (HttpMethod.GET.equals(method) && path.startsWith("/products"));

        // 3. Si viene token, se valida y se agregan los headers con los datos del usuario
        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims c = Jwts.parser().verifyWith(key).build()
                        .parseSignedClaims(auth.substring(7)).getPayload();
                builder.header("X-User-Id", String.valueOf(c.get("userId")));
                builder.header("X-User-Email", c.getSubject());
                builder.header("X-User-Rol", c.get("rol", String.class));
            } catch (JwtException e) {
                if (!publica) return rechazar(exchange);
            }
        } else if (!publica) {
            return rechazar(exchange);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private Mono<Void> rechazar(ServerWebExchange exchange) {
        ServerHttpResponse res = exchange.getResponse();
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"error\":\"Token inválido o ausente\"}".getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -1; // se ejecuta antes que los demás filtros
    }
}