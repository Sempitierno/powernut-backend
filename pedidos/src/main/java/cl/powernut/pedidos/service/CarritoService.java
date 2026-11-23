package cl.powernut.pedidos.service;

import cl.powernut.pedidos.client.CatalogoClient;
import cl.powernut.pedidos.dto.CarritoResponse;
import cl.powernut.pedidos.dto.ProductoDto;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.model.Carrito;
import cl.powernut.pedidos.model.ItemCarrito;
import cl.powernut.pedidos.repository.CarritoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CatalogoClient catalogoClient;

    public CarritoService(CarritoRepository carritoRepository, CatalogoClient catalogoClient) {
        this.carritoRepository = carritoRepository;
        this.catalogoClient = catalogoClient;
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtenerCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> new Carrito(usuarioId));
        return toResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregarItem(Long usuarioId, Long productoId, Integer cantidad) {
        ProductoDto producto = catalogoClient.obtenerProducto(productoId);
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuarioId)));

        Optional<ItemCarrito> existente = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();

        int nuevaCantidad = existente.map(i -> i.getCantidad() + cantidad).orElse(cantidad);
        if (nuevaCantidad > producto.stock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para " + producto.nombre() + ". Disponible: " + producto.stock());
        }

        if (existente.isPresent()) {
            ItemCarrito item = existente.get();
            item.setCantidad(nuevaCantidad);
            item.setNombre(producto.nombre());
            item.setPrecio(producto.precio());
            item.setImg(producto.img());
        } else {
            ItemCarrito nuevo = new ItemCarrito(
                    carrito, producto.id(), producto.nombre(),
                    producto.precio(), producto.img(), cantidad
            );
            carrito.getItems().add(nuevo);
        }

        Carrito guardado = carritoRepository.save(carrito);
        return toResponse(guardado);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto no esta en el carrito"));

        ProductoDto producto = catalogoClient.obtenerProducto(productoId);
        if (cantidad > producto.stock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para " + producto.nombre() + ". Disponible: " + producto.stock());
        }

        item.setCantidad(cantidad);
        item.setPrecio(producto.precio());
        Carrito guardado = carritoRepository.save(carrito);
        return toResponse(guardado);
    }

    @Transactional
    public CarritoResponse quitarItem(Long usuarioId, Long productoId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));

        boolean eliminado = carrito.getItems().removeIf(i -> i.getProductoId().equals(productoId));
        if (!eliminado) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Producto no esta en el carrito");
        }

        Carrito guardado = carritoRepository.save(carrito);
        return toResponse(guardado);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carrito -> {
            carrito.getItems().clear();
            carritoRepository.save(carrito);
        });
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<ItemCarrito> items = List.copyOf(carrito.getItems());
        int total = items.stream().mapToInt(i -> i.getPrecio() * i.getCantidad()).sum();
        int totalItems = items.stream().mapToInt(ItemCarrito::getCantidad).sum();
        return new CarritoResponse(carrito.getUsuarioId(), items, total, totalItems);
    }
}
