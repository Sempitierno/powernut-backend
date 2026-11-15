package cl.powernut.dto;

import jakarta.validation.constraints.*;

/**
 * RegistroRequest
 */
public record RegistroRequest(
    @NotBlank @Pattern (regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El nombre solo puede tener letras")
    String nombre,
    
    @NotBlank @Email (message = "Correo inválido")
    String correo,

    @NotBlank @Size (min = 8, message = "Mínimo 8 caracteres")
    @Pattern (regexp = "^(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "Debe tener 1 número y 1 símbolo")
    String password,

    @NotBlank String region,
    @NotBlank String comuna
) {}