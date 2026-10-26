package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.AprobacionAjusteResponseDTO;
import pe.marathon.inventario.dto.AprobarAjusteRequestDTO;
import pe.marathon.inventario.dto.DiscrepanciaItemDTO;
import pe.marathon.inventario.dto.ResumenDiscrepanciasDTO;
import java.util.List;

/**
 * Servicio para gestión y aprobación de discrepancias de inventario.
 */
public interface IAuditoriaService {
    List<DiscrepanciaItemDTO> obtenerDiscrepanciasAbiertas();
    ResumenDiscrepanciasDTO obtenerResumenDiscrepancias();
    AprobacionAjusteResponseDTO aprobarAjuste(Long auditoriaId, AprobarAjusteRequestDTO request);
    DiscrepanciaItemDTO registrarNuevaAuditoria(pe.marathon.inventario.model.AuditoriaMerma auditoria);
    void sincronizarDiscrepanciasDesdeConteo();
}
