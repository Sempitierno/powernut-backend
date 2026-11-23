package com.powernut.catalogo.config;

import com.powernut.catalogo.model.Producto;
import com.powernut.catalogo.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarProductosIniciales(ProductoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Producto(
                                "Crema de Maní Natural — 500 g",
                                6990,
                                "Crema de maní",
                                "https://via.placeholder.com/400x400?text=PowerNut+500g",
                                50
                        ),
                        new Producto(
                                "Crema de Maní Natural — 1 kg",
                                11990,
                                "Crema de maní",
                                "https://via.placeholder.com/400x400?text=PowerNut+1kg",
                                50
                        )
                ));
            }
        };
    }
}