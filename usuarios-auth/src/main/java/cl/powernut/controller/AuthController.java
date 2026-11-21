package cl.powernut.controller;

import cl.powernut.dto.LoginRequest;
import cl.powernut.dto.LoginResponse;
import cl.powernut.dto.RegistroRequest;
import cl.powernut.dto.UsuarioResponse;
import cl.powernut.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody RegistroRequest req) {
        return UsuarioResponse.de(service.registrar(req));
    }

    @GetMapping("/validate-email")
    public Map<String, Boolean> validarCorreo(@RequestParam String correo) {
        return Map.of("disponible", service.correoDisponible(correo));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        //TODO: process POST request
        
        return service.login(req);
    }
    
}