package pe.marathon.inventario.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.marathon.inventario.dao.CicloConteoDAO;
import pe.marathon.inventario.dao.ConfiguracionCicloDAO;
import pe.marathon.inventario.dto.ConfiguracionCicloDTO;
import pe.marathon.inventario.model.ConfiguracionCiclo;
import pe.marathon.inventario.service.impl.CicloServiceImpl;
import pe.marathon.inventario.strategy.SamplingStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CicloServiceTest {

    @Mock
    private ConfiguracionCicloDAO configuracionDAO;

    @Mock
    private CicloConteoDAO cicloConteoDAO;

    @Mock
    private SamplingStrategy samplingStrategy;

    @InjectMocks
    private CicloServiceImpl cicloService;

    @Test
    @DisplayName("TDD 11: Obtener configuración activa retorna reglas y responsables")
    void testObtenerConfiguracionActiva() {
        ConfiguracionCiclo config = ConfiguracionCiclo.builder()
                .id(1L)
                .frecuencia("3 Conteos Fraccionados al Mes (Días 9, 19, 29)")
                .categoriasMuestreo("Calzado Deportivo,Ropa & Textil,Accesorios")
                .reglaMuestreo("Filtro activo: Clasificación ABC")
                .responsableCalzado("Jose Armando Tunqui")
                .rolResponsableCalzado("Auditor Principal Calzado")
                .responsableJefe("Joseph Aaron Churampi")
                .rolResponsableJefe("Jefe de Inventario")
                .build();

        when(configuracionDAO.obtenerConfiguracionActiva()).thenReturn(Optional.of(config));

        ConfiguracionCicloDTO dto = cicloService.obtenerConfiguracionActiva();

        assertNotNull(dto);
        assertEquals("3 Conteos Fraccionados al Mes (Días 9, 19, 29)", dto.getFrecuencia());
        assertEquals(3, dto.getLineasMuestreo().size());
        assertEquals("Jose Armando Tunqui", dto.getResponsableCalzado());
        assertEquals("Joseph Aaron Churampi", dto.getResponsableJefe());
    }

    @Test
    @DisplayName("TDD 12: Guardar configuración persiste cambios en DAO")
    void testGuardarConfiguracion() {
        ConfiguracionCicloDTO input = ConfiguracionCicloDTO.builder()
                .frecuencia("3 Conteos Fraccionados al Mes (Días 9, 19, 29)")
                .lineasMuestreo(Arrays.asList("Calzado Deportivo", "Ropa & Textil"))
                .reglaMuestreo("Filtro ABC")
                .responsableCalzado("Jose Tunqui")
                .responsableJefe("Joseph Churampi")
                .build();

        ConfiguracionCiclo guardado = ConfiguracionCiclo.builder()
                .frecuencia(input.getFrecuencia())
                .categoriasMuestreo("Calzado Deportivo,Ropa & Textil")
                .reglaMuestreo("Filtro ABC")
                .responsableCalzado("Jose Tunqui")
                .responsableJefe("Joseph Churampi")
                .build();

        when(configuracionDAO.guardar(any(ConfiguracionCiclo.class))).thenReturn(guardado);
        when(configuracionDAO.obtenerConfiguracionActiva()).thenReturn(Optional.of(guardado));

        ConfiguracionCicloDTO resultado = cicloService.guardarConfiguracion(input);

        assertNotNull(resultado);
        verify(configuracionDAO, times(1)).guardar(any(ConfiguracionCiclo.class));
    }
}
