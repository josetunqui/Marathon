package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.CicloConteo;

import java.util.Optional;

@Repository
public interface CicloConteoRepository extends JpaRepository<CicloConteo, String> {
    Optional<CicloConteo> findByLineaIgnoreCase(String linea);
}
