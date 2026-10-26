package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.HistorialEscaneo;

import java.util.List;

@Repository
public interface HistorialEscaneoRepository extends JpaRepository<HistorialEscaneo, Long> {
    List<HistorialEscaneo> findTop50ByOrderByTimestampDesc();
}
