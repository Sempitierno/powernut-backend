package cl.powernut.pedidos.dto;

import cl.powernut.pedidos.model.EstadoOrden;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long ordenId,
        Integer monto,
        String estado,
        EstadoOrden estadoOrden,
        String codigoTransaccion,
        LocalDateTime fechaPago
) {}
