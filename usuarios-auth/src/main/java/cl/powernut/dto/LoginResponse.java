package cl.powernut.dto;

public record LoginResponse(

    String token,
    String refreshToken,
    String nombre,
    String correo,
    String rol
) {
    
}
