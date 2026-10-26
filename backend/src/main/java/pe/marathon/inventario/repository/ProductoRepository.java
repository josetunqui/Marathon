package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.Producto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    Optional<Producto> findByEan(String ean);
    List<Producto> findByLineaIgnoreCase(String linea);
    Optional<Producto> findBySkuIgnoreCaseOrEan(String sku, String ean);
    List<Producto> findByDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCaseOrEanContainingIgnoreCase(String desc, String sku, String ean);
}
