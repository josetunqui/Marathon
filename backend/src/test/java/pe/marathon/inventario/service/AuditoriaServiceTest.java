package pe.marathon.inventario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.marathon.inventario.dao.AuditoriaDAO;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.AprobacionAjusteResponseDTO;
import pe.marathon.inventario.dto.AprobarAjusteRequestDTO;
import pe.marathon.inventario.dto.DiscrepanciaItemDTO;
import pe.marathon.inventario.dto.ResumenDiscrepanciasDTO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.impl.AuditoriaServiceImpl;
import pe.marathon.inventario.strategy.DiscrepancyClassificationStrategy;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditoriaServiceTest {

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ConteoFisicoDAO conteoFisicoDAO;

    @Mock
    private ITrazabilidadService trazabilidadService;

    @Mock
    private DiscrepancyClassificationStrategy discrepancyStrategy;

    @InjectMocks
    private AuditoriaServiceImpl auditoriaService;

    private AuditoriaMerma auditoriaPrueba;

    @BeforeEach
    void setUp() {
        auditoriaPrueba = AuditoriaMerma.builder()
                .id(1L)
                .sku("CAL-NIK-001")
                .linea("CALZADO")
                .descripcionProducto("Zapatillas Nike Mercurial Vapor 15 Club")
                .ubicacion("Atocongo • Anaquel 04")
                .stockSap(14)
                .conteoFisico(12)
                .diferencia(-2)
                .tipo("Faltante (-2)")
                .causal("Hurto en Piso de Venta")
                .comentario("Caja vacía en góndola principal")
                .validadoPor("Joseph Aaron Churampi")
                .fechaValidacion(LocalDateTime.now())
                .estadoAprobado(false)
                .build();
    }

    @Test
    @DisplayName("TDD 4: Listado de discrepancias abiertas retorna elementos formateados correctamente")
    void testObtenerDiscrepanciasAbiertas() {
        when(auditoriaDAO.obtenerTodas()).thenReturn(List.of(auditoriaPrueba));
        when(discrepancyStrategy.obtenerColorBadge(-2)).thenReturn("danger");

        List<DiscrepanciaItemDTO> items = auditoriaService.obtenerDiscrepanciasAbiertas();

        assertNotNull(items);
        assertEquals(1, items.size());
        DiscrepanciaItemDTO dto = items.get(0);
        assertEquals("CAL-NIK-001", dto.getSku());
        assertEquals("14 vs 12 (-2)", dto.getSapVsFisTexto());
        assertEquals("danger", dto.getColorDiferencia());
        assertEquals("Hurto en Piso de Venta", dto.getCausaInvestigada());
        assertFalse(dto.getEstadoAprobado());
    }

    @Test
    @DisplayName("TDD 5: Aprobación de ajuste actualiza estado, cuadra stock en SAP y registra trazabilidad")
    void testAprobarAjusteExitoso() {
        AprobarAjusteRequestDTO request = AprobarAjusteRequestDTO.builder()
                .causaInvestigada("Hurto en Piso de Venta")
                .evidenciaNota("Caja vacía confirmada en cámaras")
                .validadoPor("Joseph Aaron Churampi")
                .build();

        Producto prod = Producto.builder().sku("CAL-NIK-001").stockSap(14).build();

        when(auditoriaDAO.buscarPorId(1L)).thenReturn(Optional.of(auditoriaPrueba));
        when(productoDAO.buscarPorSku("CAL-NIK-001")).thenReturn(Optional.of(prod));

        AprobacionAjusteResponseDTO response = auditoriaService.aprobarAjuste(1L, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.isEstadoAprobado());
        assertEquals("Joseph Aaron Churampi", response.getValidadoPor());

        assertTrue(auditoriaPrueba.getEstadoAprobado());
        verify(auditoriaDAO, times(1)).guardar(auditoriaPrueba);
        assertEquals(12, prod.getStockSap()); // Stock ajustado a conteo físico
        verify(productoDAO, times(1)).guardar(prod);
        verify(trazabilidadService, times(1)).registrarMovimiento(
                eq("CAL-NIK-001"), eq("Ajustado"), anyString(), anyString(), eq("Joseph Aaron Churampi"), eq(14), eq(12), eq(-2)
        );
    }

    @Test
    @DisplayName("TDD 6: Resumen de discrepancias computa faltantes críticos y sobrantes")
    void testObtenerResumenDiscrepancias() {
        AuditoriaMerma faltante = AuditoriaMerma.builder().diferencia(-2).estadoAprobado(false).build();
        AuditoriaMerma sobrante = AuditoriaMerma.builder().diferencia(2).estadoAprobado(false).build();

        when(auditoriaDAO.obtenerTodas()).thenReturn(Arrays.asList(faltante, sobrante));

        ResumenDiscrepanciasDTO resumen = auditoriaService.obtenerResumenDiscrepancias();

        assertNotNull(resumen);
        assertEquals(1, resumen.getFaltantesCriticos());
        assertEquals(1, resumen.getSobrantesBajoAnalisis());
        assertEquals(2, resumen.getTotalPendientes());
    }
}
