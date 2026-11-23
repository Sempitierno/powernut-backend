package cl.powernut.pedidos.service;

import cl.powernut.pedidos.client.CatalogoClient;
import cl.powernut.pedidos.dto.PagoResponse;
import cl.powernut.pedidos.dto.ProductoDto;
import cl.powernut.pedidos.exception.ApiException;
import cl.powernut.pedidos.model.EstadoOrden;
import cl.powernut.pedidos.model.ItemOrden;
import cl.powernut.pedidos.model.Orden;
import cl.powernut.pedidos.model.Pago;
import cl.powernut.pedidos.repository.OrdenRepository;
import cl.powernut.pedidos.repository.PagoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PagoService {

    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final CatalogoClient catalogoClient;

    public PagoService(OrdenRepository ordenRepository,
                       PagoRepository pagoRepository,
                       CatalogoClient catalogoClient) {
        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
        this.catalogoClient = catalogoClient;
    }

    @Transactional
    public PagoResponse procesarPago(Long usuarioId, Long ordenId, String resultado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada con id " + ordenId));

        if (!orden.getUsuarioId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para pagar esta orden");
        }

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La orden ya fue procesada (estado actual: " + orden.getEstado() + ")");
        }

        if (pagoRepository.findByOrdenId(ordenId).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La orden ya tiene un pago registrado");
        }

        String resultadoNormalizado = resultado.toUpperCase();
        if ("APROBADO".equals(resultadoNormalizado)) {
            for (ItemOrden item : orden.getItems()) {
                ProductoDto producto = catalogoClient.obtenerProducto(item.getProductoId());
                catalogoClient.descontarStock(producto, item.getCantidad());
            }
            orden.setEstado(EstadoOrden.APROBADO);
        } else {
            orden.setEstado(EstadoOrden.ANULADO);
        }

        ordenRepository.save(orden);

        String codigo = "WP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Pago pago = pagoRepository.save(new Pago(orden, orden.getTotal(), resultadoNormalizado, codigo));

        return toResponse(pago, orden);
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPorOrden(Long ordenId, Long usuarioId, String rol) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada con id " + ordenId));

        if (!"ADMIN".equalsIgnoreCase(rol) && !orden.getUsuarioId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este pago");
        }

        Pago pago = pagoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pago no encontrado para la orden " + ordenId));

        return toResponse(pago, orden);
    }

    private PagoResponse toResponse(Pago pago, Orden orden) {
        return new PagoResponse(
                pago.getId(),
                orden.getId(),
                pago.getMonto(),
                pago.getEstado(),
                orden.getEstado(),
                pago.getCodigoTransaccion(),
                pago.getFechaPago()
        );
    }
}
