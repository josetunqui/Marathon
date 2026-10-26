package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.MovimientoTrazabilidad;
import java.util.List;

@Repository
public interface MovimientoTrazabilidadRepository extends JpaRepository<MovimientoTrazabilidad, Long> {
    List<MovimientoTrazabilidad> findBySkuOrderByFechaHoraDesc(String sku);
    List<MovimientoTrazabilidad> findAllByOrderByFechaHoraDesc();
}
