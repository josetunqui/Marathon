package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.TrazabilidadSKUResponseDTO;

/**
 * Servicio de auditoría y trazabilidad histórica cronológica de movimientos por SKU.
 */
public interface ITrazabilidadService {
    TrazabilidadSKUResponseDTO obtenerTrazabilidadPorSku(String sku);
    void registrarMovimiento(String sku, String tipoBadge, String titulo, String descripcion, String usuario, Integer stockSap, Integer conteoFisico, Integer diferencia);
}
