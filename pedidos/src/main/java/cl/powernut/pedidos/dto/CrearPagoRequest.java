package cl.powernut.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CrearPagoRequest(
        @NotNull(message = "ordenId es obligatorio")
        Long ordenId,

        @NotBlank(message = "resultado es obligatorio")
        @Pattern(regexp = "APROBADO|RECHAZADO", message = "resultado debe ser APROBADO o RECHAZADO")
        String resultado
) {}
