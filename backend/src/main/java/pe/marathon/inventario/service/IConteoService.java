package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.*;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.HistorialEscaneo;
import java.util.List;

/**
 * Servicio enfocado en la captura de conteo físico por escáner y ajustes de conteo.
 */
public interface IConteoService {
    ScanResponseDTO registrarEscaneo(ScanRequestDTO request);
    ZonaProgresoDTO obtenerProgresoZona(String nombreZona);
    List<CapturaSesionDTO> obtenerUltimasCapturas(int limite);
    ConteoFisico actualizarConteoExacto(AjusteManualDTO ajuste);
    List<HistorialEscaneo> obtenerHistorialReciente();
}
