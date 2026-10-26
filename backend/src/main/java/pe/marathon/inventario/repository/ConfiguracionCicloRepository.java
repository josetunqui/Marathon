package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.ConfiguracionCiclo;

@Repository
public interface ConfiguracionCicloRepository extends JpaRepository<ConfiguracionCiclo, Long> {
}
