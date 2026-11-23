package com.powernut.catalogo.service;

import com.powernut.catalogo.dto.ProductoRequest;
import com.powernut.catalogo.exception.ProductoNoEncontradoException;
import com.powernut.catalogo.model.Producto;
import com.powernut.catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con id: " + id));
    }

    public List<Producto> listarPorCategoria(String categoria) {
        return repository.findByCategoriaIgnoreCase(categoria);
    }

    public Producto crear(ProductoRequest req) {
        Producto producto = new Producto(
                req.nombre().trim(),
                req.precio(),
                req.categoria().trim(),
                limpiarImg(req.img()),
                req.stock()
        );
        return repository.save(producto);
    }

    public Producto actualizar(Long id, ProductoRequest req) {
        Producto existente = obtenerPorId(id);
        existente.setNombre(req.nombre().trim());
        existente.setPrecio(req.precio());
        existente.setCategoria(req.categoria().trim());
        existente.setImg(limpiarImg(req.img()));
        existente.setStock(req.stock());
        return repository.save(existente);
    }

    private String limpiarImg(String img) {
        return img == null ? "" : img.trim();
    }
    
    public void eliminar(Long id) {
    Producto existente = obtenerPorId(id);
    repository.delete(existente);
    }
}