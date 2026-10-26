package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.HistorialEscaneo;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) para el acceso a datos de Conteos Físicos e Historial de Escaneos.
 */
public interface ConteoFisicoDAO {
    List<ConteoFisico> obtenerTodos();
    Optional<ConteoFisico> buscarPorSku(String sku);
    ConteoFisico guardar(ConteoFisico conteo);
    HistorialEscaneo registrarEscaneo(HistorialEscaneo escaneo);
    List<HistorialEscaneo> obtenerUltimosEscaneos(int limite);
    int contarSkusContados();
}
