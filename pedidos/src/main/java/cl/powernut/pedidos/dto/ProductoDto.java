package cl.powernut.pedidos.dto;

public record ProductoDto(
        Long id,
        String nombre,
        Integer precio,
        String categoria,
        String img,
        Integer stock
) {}
