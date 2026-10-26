package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.MovimientoTrazabilidad;
import java.util.List;

/**
 * Data Access Object (DAO) para el registro de trazabilidad histórica de SKUs.
 */
public interface MovimientoTrazabilidadDAO {
    List<MovimientoTrazabilidad> obtenerTodos();
    List<MovimientoTrazabilidad> obtenerPorSku(String sku);
    MovimientoTrazabilidad guardar(MovimientoTrazabilidad movimiento);
}
