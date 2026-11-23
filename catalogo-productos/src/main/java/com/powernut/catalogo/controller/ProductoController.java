package com.powernut.catalogo.controller;

import com.powernut.catalogo.dto.ProductoRequest;
import com.powernut.catalogo.exception.AccesoDenegadoException;
import com.powernut.catalogo.model.Producto;
import com.powernut.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Productos", description = "CRUD del catálogo de productos PowerNut")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public Producto obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/category/{categoria}")
    @Operation(summary = "Listar productos por categoría")
    public List<Producto> listarPorCategoria(@PathVariable String categoria) {
        return service.listarPorCategoria(categoria);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto (solo ADMIN)")
    public Producto crear(
            @RequestHeader(value = "X-User-Rol", required = false) String rol,
            @Valid @RequestBody ProductoRequest req) {
        exigirAdmin(rol);
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (solo ADMIN)")
    public Producto actualizar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String rol,
            @Valid @RequestBody ProductoRequest req) {
        exigirAdmin(rol);
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar producto (solo ADMIN)")
    public void eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String rol) {
        exigirAdmin(rol);
        service.eliminar(id);
    }

    private void exigirAdmin(String rol) {
        if (!"ADMIN".equals(rol)) {
            throw new AccesoDenegadoException("Acceso denegado: se requiere rol ADMIN");
        }
    }
}