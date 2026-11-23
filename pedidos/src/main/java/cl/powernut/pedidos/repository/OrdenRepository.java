package cl.powernut.pedidos.repository;

import cl.powernut.pedidos.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
