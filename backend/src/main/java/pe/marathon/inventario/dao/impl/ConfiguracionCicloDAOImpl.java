package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.ConfiguracionCicloDAO;
import pe.marathon.inventario.model.ConfiguracionCiclo;
import pe.marathon.inventario.repository.ConfiguracionCicloRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConfiguracionCicloDAOImpl implements ConfiguracionCicloDAO {

    private final ConfiguracionCicloRepository configRepository;

    @Override
    public Optional<ConfiguracionCiclo> obtenerConfiguracionActiva() {
        List<ConfiguracionCiclo> todas = configRepository.findAll();
        if (todas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(todas.get(todas.size() - 1));
    }

    @Override
    public ConfiguracionCiclo guardar(ConfiguracionCiclo configuracion) {
        return configRepository.save(configuracion);
    }
}
