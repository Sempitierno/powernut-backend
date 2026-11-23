package cl.powernut.pedidos.client;

import cl.powernut.pedidos.dto.ProductoDto;
import cl.powernut.pedidos.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CatalogoClient {

    private final RestClient restClient;
    private final String catalogoUrl;
    private final boolean mockEnabled;
    private final Map<Long, ProductoDto> mockProductos = new ConcurrentHashMap<>();

    public CatalogoClient(
            @Value("${catalogo.url}") String catalogoUrl,
            @Value("${catalogo.mock:false}") boolean mockEnabled) {
        this.catalogoUrl = catalogoUrl;
        this.mockEnabled = mockEnabled;
        this.restClient = RestClient.create();

        // Datos de prueba solo usados cuando catalogo.mock=true
        mockProductos.put(1L, new ProductoDto(1L, "Crema de Mani Natural 500g", 6990, "Crema de mani",
                "https://mejorconsalud.as.com/wp-content/uploads/2020/09/frasco-mantequilla-cacahuete.jpg", 20));
        mockProductos.put(2L, new ProductoDto(2L, "Crema de Mani Crunchy 500g", 7490, "Crema de mani",
                "https://mejorconsalud.as.com/wp-content/uploads/2020/09/frasco-mantequilla-cacahuete.jpg", 10));
        mockProductos.put(3L, new ProductoDto(3L, "Barra de Proteina PowerNut", 2490, "Snacks",
                "https://mejorconsalud.as.com/wp-content/uploads/2020/09/frasco-mantequilla-cacahuete.jpg", 5));
    }

    public ProductoDto obtenerProducto(Long productoId) {
        if (mockEnabled) {
            ProductoDto p = mockProductos.get(productoId);
            if (p == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado con id " + productoId);
            }
            return p;
        }

        try {
            ProductoDto producto = restClient.get()
                    .uri(catalogoUrl + "/products/{id}", productoId)
                    .retrieve()
                    .body(ProductoDto.class);
            if (producto == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado con id " + productoId);
            }
            return producto;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado con id " + productoId);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Error al consultar catalogo-productos: " + ex.getMessage());
        }
    }

    public void descontarStock(ProductoDto producto, int cantidadComprada) {
        int nuevoStock = producto.stock() - cantidadComprada;
        if (nuevoStock < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock insuficiente para " + producto.nombre());
        }

        if (mockEnabled) {
            mockProductos.put(producto.id(), new ProductoDto(
                    producto.id(), producto.nombre(), producto.precio(),
                    producto.categoria(), producto.img(), nuevoStock
            ));
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                    "nombre", producto.nombre(),
                    "precio", producto.precio(),
                    "categoria", producto.categoria(),
                    "img", producto.img() == null ? "" : producto.img(),
                    "stock", nuevoStock
            );
            restClient.put()
                    .uri(catalogoUrl + "/products/{id}", producto.id())
                    .header("X-User-Rol", "ADMIN")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo actualizar stock en catalogo: " + ex.getMessage());
        }
    }
}
