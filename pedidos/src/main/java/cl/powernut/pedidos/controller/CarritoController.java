package cl.powernut.pedidos.controller;

import cl.powernut.pedidos.dto.ActualizarCantidadRequest;
import cl.powernut.pedidos.dto.AgregarItemRequest;
import cl.powernut.pedidos.dto.CarritoResponse;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Carrito", description = "Gestion del carrito de compras por usuario")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    private Long validarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado (falta header X-User-Id)");
        }
        return usuarioId;
    }

    @GetMapping
    @Operation(summary = "Obtiene el carrito del usuario autenticado")
    public ResponseEntity<CarritoResponse> obtenerCarrito(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(validarUsuario(usuarioId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Agrega un producto al carrito validando stock")
    public ResponseEntity<CarritoResponse> agregarItem(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @Valid @RequestBody AgregarItemRequest req) {
        CarritoResponse res = carritoService.agregarItem(validarUsuario(usuarioId), req.productoId(), req.cantidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/items/{productoId}")
    @Operation(summary = "Actualiza la cantidad de un producto en el carrito")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @PathVariable Long productoId,
            @Valid @RequestBody ActualizarCantidadRequest req) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(validarUsuario(usuarioId), productoId, req.cantidad()));
    }

    @DeleteMapping("/items/{productoId}")
    @Operation(summary = "Quita un producto del carrito")
    public ResponseEntity<CarritoResponse> quitarItem(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(carritoService.quitarItem(validarUsuario(usuarioId), productoId));
    }

    @DeleteMapping
    @Operation(summary = "Vacia el carrito completo del usuario")
    public ResponseEntity<Void> vaciarCarrito(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        carritoService.vaciarCarrito(validarUsuario(usuarioId));
        return ResponseEntity.noContent().build();
    }
}
