package cl.powernut.pedidos.controller;

import cl.powernut.pedidos.dto.OrdenResponse;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Ordenes", description = "Creacion y consulta de pedidos del usuario")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    private Long validarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado (falta header X-User-Id)");
        }
        return usuarioId;
    }

    @PostMapping
    @Operation(summary = "Crea una orden a partir del carrito actual del usuario")
    public ResponseEntity<OrdenResponse> crearOrden(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        OrdenResponse creada = ordenService.crearOrdenDesdeCarrito(validarUsuario(usuarioId));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    @Operation(summary = "Lista el historial de ordenes del usuario autenticado")
    public ResponseEntity<List<OrdenResponse>> listarMisOrdenes(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        return ResponseEntity.ok(ordenService.listarOrdenesUsuario(validarUsuario(usuarioId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una orden por ID (solo dueno o ADMIN)")
    public ResponseEntity<OrdenResponse> obtenerOrden(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-User-Rol", required = false) String rol,
            @PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerOrdenPorId(id, validarUsuario(usuarioId), rol));
    }
}
