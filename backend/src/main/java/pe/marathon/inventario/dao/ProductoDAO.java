package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) para el acceso a datos del catálogo de Productos.
 * Aplica el principio de Inversión de Dependencias (DIP) y Segregación de Interfaces (ISP).
 */
public interface ProductoDAO {
    List<Producto> obtenerTodos();
    Optional<Producto> buscarPorSku(String sku);
    Optional<Producto> buscarPorEan(String ean);
    Optional<Producto> buscarPorCodigo(String codigo);
    List<Producto> buscarPorTermino(String termino);
    List<Producto> buscarPorLinea(String linea);
    Producto guardar(Producto producto);
    long contar();
}
