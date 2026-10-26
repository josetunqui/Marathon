package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.ConteoFisico;

import java.util.Optional;

@Repository
public interface ConteoFisicoRepository extends JpaRepository<ConteoFisico, Long> {
    Optional<ConteoFisico> findBySku(String sku);
}
