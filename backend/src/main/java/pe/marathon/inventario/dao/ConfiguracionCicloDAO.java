package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.ConfiguracionCiclo;
import java.util.Optional;

/**
 * Data Access Object (DAO) para la configuración activa de ciclos.
 */
public interface ConfiguracionCicloDAO {
    Optional<ConfiguracionCiclo> obtenerConfiguracionActiva();
    ConfiguracionCiclo guardar(ConfiguracionCiclo configuracion);
}
