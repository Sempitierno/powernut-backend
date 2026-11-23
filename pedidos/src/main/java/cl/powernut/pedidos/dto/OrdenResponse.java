package cl.powernut.pedidos.dto;

import cl.powernut.pedidos.model.EstadoOrden;
import cl.powernut.pedidos.model.ItemOrden;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id,
        Long usuarioId,
        LocalDateTime fechaCreacion,
        EstadoOrden estado,
        Integer total,
        List<ItemOrden> items
) {}
