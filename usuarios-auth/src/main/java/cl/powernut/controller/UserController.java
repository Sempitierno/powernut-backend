package cl.powernut.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.powernut.dto.UsuarioResponse;
import cl.powernut.model.Usuario;
import cl.powernut.service.UsuarioService;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("/users")
public class UserController {

    private final UsuarioService service;

    public UserController(UsuarioService service){
        this.service = service;
    }
    

    //Cualquier usuario con token válido: devuelve sus datos

    @GetMapping("/me")
    public UsuarioResponse me(Authentication auth) {
        return
UsuarioResponse.de(service.buscarPorCorreo(auth.getName()));
    }

    //solo admin: lista a todos los usuarios
    @GetMapping 
    @PreAuthorize ("hasRole('ADMIN')")
    public List<UsuarioResponse> listar(){
        return 
service.listar().stream().map(UsuarioResponse::de).toList();
    }
    
}
