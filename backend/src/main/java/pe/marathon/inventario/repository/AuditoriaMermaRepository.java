package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.AuditoriaMerma;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaMermaRepository extends JpaRepository<AuditoriaMerma, Long> {
    Optional<AuditoriaMerma> findFirstBySkuOrderByFechaValidacionDesc(String sku);
    List<AuditoriaMerma> findAllByOrderByFechaValidacionDesc();
    List<AuditoriaMerma> findByEstadoAprobadoOrderByFechaValidacionDesc(Boolean estadoAprobado);
    Optional<AuditoriaMerma> findBySku(String sku);
}
