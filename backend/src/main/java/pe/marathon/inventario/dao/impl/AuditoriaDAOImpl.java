package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.AuditoriaDAO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.repository.AuditoriaMermaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuditoriaDAOImpl implements AuditoriaDAO {

    private final AuditoriaMermaRepository auditoriaMermaRepository;

    @Override
    public List<AuditoriaMerma> obtenerTodas() {
        return auditoriaMermaRepository.findAllByOrderByFechaValidacionDesc();
    }

    @Override
    public List<AuditoriaMerma> obtenerPorEstadoAprobado(Boolean aprobado) {
        return auditoriaMermaRepository.findByEstadoAprobadoOrderByFechaValidacionDesc(aprobado);
    }

    @Override
    public Optional<AuditoriaMerma> buscarPorId(Long id) {
        return auditoriaMermaRepository.findById(id);
    }

    @Override
    public Optional<AuditoriaMerma> buscarPorSku(String sku) {
        return auditoriaMermaRepository.findBySku(sku);
    }

    @Override
    public AuditoriaMerma guardar(AuditoriaMerma auditoria) {
        return auditoriaMermaRepository.save(auditoria);
    }

    @Override
    public long contarPendientes() {
        return auditoriaMermaRepository.findByEstadoAprobadoOrderByFechaValidacionDesc(false).size();
    }
}
