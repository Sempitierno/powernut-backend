package cl.powernut.dto;

public record LoginResponse(

    String token,
    String nombre,
    String correo,
    String rol
) {
    
}
