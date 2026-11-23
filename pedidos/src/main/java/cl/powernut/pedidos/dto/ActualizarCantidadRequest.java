package cl.powernut.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCantidadRequest(
        @NotNull(message = "cantidad es obligatoria")
        @Min(value = 1, message = "cantidad debe ser al menos 1")
        Integer cantidad
) {}
