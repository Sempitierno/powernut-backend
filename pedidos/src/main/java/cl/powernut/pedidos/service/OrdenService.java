package cl.powernut.pedidos.service;

import cl.powernut.pedidos.client.CatalogoClient;
import cl.powernut.pedidos.dto.OrdenResponse;
import cl.powernut.pedidos.dto.ProductoDto;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.model.Carrito;
import cl.powernut.pedidos.model.ItemCarrito;
import cl.powernut.pedidos.model.ItemOrden;
import cl.powernut.pedidos.model.Orden;
import cl.powernut.pedidos.repository.CarritoRepository;
import cl.powernut.pedidos.repository.OrdenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenService {

    private final CarritoRepository carritoRepository;
    private final OrdenRepository ordenRepository;
    private final CatalogoClient catalogoClient;

    public OrdenService(CarritoRepository carritoRepository,
                        OrdenRepository ordenRepository,
                        CatalogoClient catalogoClient) {
        this.carritoRepository = carritoRepository;
        this.ordenRepository = ordenRepository;
        this.catalogoClient = catalogoClient;
    }

    @Transactional
    public OrdenResponse crearOrdenDesdeCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio"));

        if (carrito.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio");
        }

        Orden orden = new Orden(usuarioId, 0);
        int total = 0;

        for (ItemCarrito itemCarrito : carrito.getItems()) {
            ProductoDto producto = catalogoClient.obtenerProducto(itemCarrito.getProductoId());

            if (itemCarrito.getCantidad() > producto.stock()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para " + producto.nombre() + ". Disponible: " + producto.stock());
            }

            int subtotal = producto.precio() * itemCarrito.getCantidad();
            total += subtotal;

            ItemOrden itemOrden = new ItemOrden(
                    orden,
                    producto.id(),
                    producto.nombre(),
                    producto.precio(),
                    producto.img(),
                    itemCarrito.getCantidad()
            );
            orden.getItems().add(itemOrden);
        }

        orden.setTotal(total);
        Orden guardada = ordenRepository.save(orden);

        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarOrdenesUsuario(Long usuarioId) {
        return ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtenerOrdenPorId(Long ordenId, Long usuarioId, String rol) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada con id " + ordenId));

        if (!"ADMIN".equalsIgnoreCase(rol) && !orden.getUsuarioId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a esta orden");
        }

        return toResponse(orden);
    }

    private OrdenResponse toResponse(Orden orden) {
        return new OrdenResponse(
                orden.getId(),
                orden.getUsuarioId(),
                orden.getFechaCreacion(),
                orden.getEstado(),
                orden.getTotal(),
                List.copyOf(orden.getItems())
        );
    }
}
