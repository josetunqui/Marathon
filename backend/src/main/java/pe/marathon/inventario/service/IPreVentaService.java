package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.PreVentaResponseDTO;
import pe.marathon.inventario.dto.ReservaRequestDTO;
import pe.marathon.inventario.dto.ReservaResponseDTO;

/**
 * Servicio para consulta pre-venta rápida y desglose de ubicaciones físicas.
 */
public interface IPreVentaService {
    PreVentaResponseDTO consultarStock(String query);
    ReservaResponseDTO reservarStock(ReservaRequestDTO request);
    boolean solicitarAuditoriaRapida(String sku, String motivo);
}
