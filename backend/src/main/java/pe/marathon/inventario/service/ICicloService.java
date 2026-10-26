package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.CicloConteoDTO;
import pe.marathon.inventario.dto.ConfiguracionCicloDTO;
import java.util.List;

/**
 * Servicio de configuración y reglas de planificación del ciclo de inventario.
 */
public interface ICicloService {
    ConfiguracionCicloDTO obtenerConfiguracionActiva();
    ConfiguracionCicloDTO guardarConfiguracion(ConfiguracionCicloDTO dto);
    List<CicloConteoDTO> listarCiclos();
}
