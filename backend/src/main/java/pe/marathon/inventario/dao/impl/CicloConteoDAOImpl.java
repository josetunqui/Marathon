package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.CicloConteoDAO;
import pe.marathon.inventario.model.CicloConteo;
import pe.marathon.inventario.repository.CicloConteoRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CicloConteoDAOImpl implements CicloConteoDAO {

    private final CicloConteoRepository cicloConteoRepository;

    @Override
    public List<CicloConteo> obtenerTodos() {
        return cicloConteoRepository.findAll();
    }

    @Override
    public Optional<CicloConteo> buscarPorId(String id) {
        return cicloConteoRepository.findById(id);
    }

    @Override
    public CicloConteo guardar(CicloConteo ciclo) {
        return cicloConteoRepository.save(ciclo);
    }
}
