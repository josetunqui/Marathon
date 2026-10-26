package pe.marathon.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.ProductoDTO;
import pe.marathon.inventario.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoDAO productoDAO;

    public List<Producto> listarTodos(String linea) {
        if (linea != null && !linea.trim().isEmpty() && !"ALL".equalsIgnoreCase(linea)) {
            return productoDAO.buscarPorLinea(linea);
        }
        return productoDAO.obtenerTodos();
    }

    public Optional<Producto> buscarPorSkuOEan(String codigo) {
        Optional<Producto> opt = productoDAO.buscarPorSku(codigo);
        if (opt.isEmpty()) {
            opt = productoDAO.buscarPorEan(codigo);
        }
        return opt;
    }

    public Producto guardar(Producto producto) {
        return productoDAO.guardar(producto);
    }

    public List<Producto> buscarPorTerminoEntities(String termino) {
        return productoDAO.buscarPorTermino(termino);
    }

    @Override
    public List<ProductoDTO> listarTodos() {
        return productoDAO.obtenerTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductoDTO buscarPorSku(String sku) {
        return productoDAO.buscarPorSku(sku).map(this::mapToDTO).orElse(null);
    }

    @Override
    public ProductoDTO buscarPorEan(String ean) {
        return productoDAO.buscarPorEan(ean).map(this::mapToDTO).orElse(null);
    }

    @Override
    public List<ProductoDTO> buscarPorTermino(String termino) {
        return productoDAO.buscarPorTermino(termino).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProductoDTO mapToDTO(Producto p) {
        return ProductoDTO.builder()
                .sku(p.getSku())
                .ean(p.getEan())
                .linea(p.getLinea())
                .marca(p.getMarca())
                .descripcion(p.getDescripcion())
                .talla(p.getTalla())
                .color(p.getColor())
                .ubicacion(p.getUbicacion())
                .stockSap(p.getStockSap())
                .precioUnitario(p.getPrecioUnitario())
                .costoUnitario(p.getCostoUnitario())
                .stockPisoVenta(p.getStockPisoVenta())
                .stockAlmacen(p.getStockAlmacen())
                .stockVitrina(p.getStockVitrina())
                .build();
    }
}
