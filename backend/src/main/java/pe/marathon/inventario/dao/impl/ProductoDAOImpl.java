package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductoDAOImpl implements ProductoDAO {

    private final ProductoRepository productoRepository;

    @Override
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> buscarPorSku(String sku) {
        return productoRepository.findById(sku);
    }

    @Override
    public Optional<Producto> buscarPorEan(String ean) {
        return productoRepository.findByEan(ean);
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findBySkuIgnoreCaseOrEan(codigo, codigo);
    }

    @Override
    public List<Producto> buscarPorTermino(String termino) {
        return productoRepository.findByDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCaseOrEanContainingIgnoreCase(
                termino, termino, termino);
    }

    @Override
    public List<Producto> buscarPorLinea(String linea) {
        return productoRepository.findByLineaIgnoreCase(linea);
    }

    @Override
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public long contar() {
        return productoRepository.count();
    }
}
