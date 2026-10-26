package pe.marathon.inventario.service;

import pe.marathon.inventario.dto.ProductoDTO;
import java.util.List;

/**
 * Servicio para consulta y mantenimiento del catálogo de productos.
 */
public interface IProductoService {
    List<ProductoDTO> listarTodos();
    ProductoDTO buscarPorSku(String sku);
    ProductoDTO buscarPorEan(String ean);
    List<ProductoDTO> buscarPorTermino(String termino);
}
