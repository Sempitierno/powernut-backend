package cl.powernut.pedidos.controller;

import cl.powernut.pedidos.dto.CrearPagoRequest;
import cl.powernut.pedidos.dto.PagoResponse;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagos", description = "Procesamiento de pago simulado (Webpay)")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    private Long validarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado (falta header X-User-Id)");
        }
        return usuarioId;
    }

    @PostMapping
    @Operation(summary = "Procesa el pago simulado de una orden (APROBADO o RECHAZADO)")
    public ResponseEntity<PagoResponse> procesarPago(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @Valid @RequestBody CrearPagoRequest req) {
        PagoResponse res = pagoService.procesarPago(validarUsuario(usuarioId), req.ordenId(), req.resultado());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/order/{ordenId}")
    @Operation(summary = "Obtiene el comprobante de pago de una orden")
    public ResponseEntity<PagoResponse> obtenerPagoPorOrden(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-User-Rol", required = false) String rol,
            @PathVariable Long ordenId) {
        return ResponseEntity.ok(pagoService.obtenerPagoPorOrden(ordenId, validarUsuario(usuarioId), rol));
    }
}
