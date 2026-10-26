package pe.marathon.inventario.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.marathon.inventario.dao.CicloConteoDAO;
import pe.marathon.inventario.dao.ConfiguracionCicloDAO;
import pe.marathon.inventario.dto.CicloConteoDTO;
import pe.marathon.inventario.dto.ConfiguracionCicloDTO;
import pe.marathon.inventario.model.CicloConteo;
import pe.marathon.inventario.model.ConfiguracionCiclo;
import pe.marathon.inventario.service.ICicloService;
import pe.marathon.inventario.strategy.SamplingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CicloServiceImpl implements ICicloService {

    private final ConfiguracionCicloDAO configuracionDAO;
    private final CicloConteoDAO cicloConteoDAO;
    private final SamplingStrategy samplingStrategy;

    @Override
    public ConfiguracionCicloDTO obtenerConfiguracionActiva() {
        Optional<ConfiguracionCiclo> opt = configuracionDAO.obtenerConfiguracionActiva();
        if (opt.isPresent()) {
            ConfiguracionCiclo c = opt.get();
            List<String> cats = Arrays.stream(c.getCategoriasMuestreo().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            return ConfiguracionCicloDTO.builder()
                    .frecuencia(c.getFrecuencia())
                    .lineasMuestreo(cats)
                    .reglaMuestreo(c.getReglaMuestreo())
                    .responsableCalzado(c.getResponsableCalzado())
                    .rolResponsableCalzado(c.getRolResponsableCalzado())
                    .responsableJefe(c.getResponsableJefe())
                    .rolResponsableJefe(c.getRolResponsableJefe())
                    .build();
        }

        // Configuración predeterminada que coincide exactamente con el prototipo
        List<String> defaultCats = Arrays.asList("Calzado Deportivo", "Ropa & Textil", "Accesorios");
        return ConfiguracionCicloDTO.builder()
                .frecuencia("3 Conteos Fraccionados al Mes (Días 9, 19, 29)")
                .lineasMuestreo(defaultCats)
                .reglaMuestreo(samplingStrategy.getNombreEstrategia())
                .responsableCalzado("Jose Armando Tunqui")
                .rolResponsableCalzado("Auditor Principal Calzado")
                .responsableJefe("Joseph Aaron Churampi")
                .rolResponsableJefe("Jefe de Inventario")
                .build();
    }

    @Override
    @Transactional
    public ConfiguracionCicloDTO guardarConfiguracion(ConfiguracionCicloDTO dto) {
        String catsStr = dto.getLineasMuestreo() != null ? String.join(",", dto.getLineasMuestreo()) : "Calzado Deportivo,Ropa & Textil,Accesorios";
        ConfiguracionCiclo c = ConfiguracionCiclo.builder()
                .frecuencia(dto.getFrecuencia() != null ? dto.getFrecuencia() : "3 Conteos Fraccionados al Mes (Días 9, 19, 29)")
                .categoriasMuestreo(catsStr)
                .reglaMuestreo(dto.getReglaMuestreo() != null ? dto.getReglaMuestreo() : samplingStrategy.getNombreEstrategia())
                .responsableCalzado(dto.getResponsableCalzado() != null ? dto.getResponsableCalzado() : "Jose Armando Tunqui")
                .rolResponsableCalzado(dto.getRolResponsableCalzado() != null ? dto.getRolResponsableCalzado() : "Auditor Principal Calzado")
                .responsableJefe(dto.getResponsableJefe() != null ? dto.getResponsableJefe() : "Joseph Aaron Churampi")
                .rolResponsableJefe(dto.getRolResponsableJefe() != null ? dto.getRolResponsableJefe() : "Jefe de Inventario")
                .build();

        configuracionDAO.guardar(c);
        return obtenerConfiguracionActiva();
    }

    @Override
    public List<CicloConteoDTO> listarCiclos() {
        List<CicloConteo> ciclos = cicloConteoDAO.obtenerTodos();
        List<CicloConteoDTO> dtos = new ArrayList<>();
        for (CicloConteo c : ciclos) {
            dtos.add(CicloConteoDTO.builder()
                    .id(c.getId())
                    .linea(c.getLinea())
                    .diaProgramado(c.getDiaProgramado())
                    .nombre(c.getNombre())
                    .descripcion(c.getDescripcion())
                    .estado(c.getEstado())
                    .frecuencia(c.getFrecuencia())
                    .responsable(c.getResponsable())
                    .fechaLimite(c.getFechaLimite())
                    .build());
        }
        return dtos;
    }
}
