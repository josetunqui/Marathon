package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.HistorialEscaneo;
import pe.marathon.inventario.repository.ConteoFisicoRepository;
import pe.marathon.inventario.repository.HistorialEscaneoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConteoFisicoDAOImpl implements ConteoFisicoDAO {

    private final ConteoFisicoRepository conteoFisicoRepository;
    private final HistorialEscaneoRepository historialEscaneoRepository;

    @Override
    public List<ConteoFisico> obtenerTodos() {
        return conteoFisicoRepository.findAll();
    }

    @Override
    public Optional<ConteoFisico> buscarPorSku(String sku) {
        return conteoFisicoRepository.findBySku(sku);
    }

    @Override
    public ConteoFisico guardar(ConteoFisico conteo) {
        return conteoFisicoRepository.save(conteo);
    }

    @Override
    public HistorialEscaneo registrarEscaneo(HistorialEscaneo escaneo) {
        return historialEscaneoRepository.save(escaneo);
    }

    @Override
    public List<HistorialEscaneo> obtenerUltimosEscaneos(int limite) {
        List<HistorialEscaneo> todos = historialEscaneoRepository.findTop50ByOrderByTimestampDesc();
        return todos.stream().limit(limite).collect(Collectors.toList());
    }

    @Override
    public int contarSkusContados() {
        return conteoFisicoRepository.findAll().size();
    }
}
