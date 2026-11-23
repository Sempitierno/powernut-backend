package cl.powernut.pedidos.dto;

import cl.powernut.pedidos.model.ItemCarrito;
import java.util.List;

public record CarritoResponse(
        Long usuarioId,
        List<ItemCarrito> items,
        Integer total,
        Integer totalItems
) {}
