package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.CicloConteo;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) para los ciclos programados de inventario.
 */
public interface CicloConteoDAO {
    List<CicloConteo> obtenerTodos();
    Optional<CicloConteo> buscarPorId(String id);
    CicloConteo guardar(CicloConteo ciclo);
}
