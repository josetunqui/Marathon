package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.AuditoriaMerma;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) para el acceso a datos de Auditorías de Discrepancias.
 */
public interface AuditoriaDAO {
    List<AuditoriaMerma> obtenerTodas();
    List<AuditoriaMerma> obtenerPorEstadoAprobado(Boolean aprobado);
    Optional<AuditoriaMerma> buscarPorId(Long id);
    Optional<AuditoriaMerma> buscarPorSku(String sku);
    AuditoriaMerma guardar(AuditoriaMerma auditoria);
    long contarPendientes();
}
