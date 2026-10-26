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
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.dto.ResumenKpiDTO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.Producto;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Reconciliación e Índice de Registro de Inventario (IRA %)")
public class ReconciliationServiceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ConteoFisicoDAO conteoFisicoDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @InjectMocks
    private ReconciliationService reconciliationService;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto("CAL-NIK-002", "790123456712", "Calzado", "Nike",
                "Zapatillas Nike Air Max Excee Running", "42", "Blanco / Azul",
                "Pasillo A - Estante 03", 10, 389.00, 210.00, 4, 5, 1);

        producto2 = new Producto("CAL-NIK-001", "7751234001015", "Calzado", "Nike",
                "Zapatillas Nike Mercurial Vapor 15 Club", "41", "Negro / Carmesí",
                "Atocongo • Anaquel 04", 14, 269.90, 160.00, 6, 7, 1);
    }

    @Test
    @DisplayName("Debe calcular correctamente el IRA % y detectar discrepancias entre Stock SAP y Físico")
    void testCalculoReconciliacionYIra() {
        when(productoDAO.obtenerTodos()).thenReturn(Arrays.asList(producto1, producto2));

        // Conteo 1: 10 unidades (cuadrado con SAP: 10)
        ConteoFisico c1 = ConteoFisico.builder()
                .sku("CAL-NIK-002")
                .conteoFisico(10)
                .build();

        // Conteo 2: 12 unidades (SAP: 14 -> faltante de -2)
        ConteoFisico c2 = ConteoFisico.builder()
                .sku("CAL-NIK-001")
                .conteoFisico(12)
                .build();

        when(conteoFisicoDAO.obtenerTodos()).thenReturn(Arrays.asList(c1, c2));
        when(auditoriaDAO.obtenerTodas()).thenReturn(Collections.emptyList());

        ReporteReconciliacionDTO reporte = reconciliationService.generarReporte("ALL");

        assertNotNull(reporte);
        assertEquals(2, reporte.getItems().size());

        ResumenKpiDTO kpis = reporte.getKpis();
        assertNotNull(kpis);
        assertEquals(2, kpis.getTotalSkus());
        assertEquals(24, kpis.getTotalSap());
        assertEquals(22, kpis.getTotalFisico());
        assertEquals(1, kpis.getConformes());
        assertEquals(1, kpis.getFaltantes());
        assertEquals(0, kpis.getSobrantes());

        // IRA % = (conformes / totalSkus) * 100 = (1 / 2) * 100 = 50.0%
        assertEquals(50.0, kpis.getIra(), 0.1);
    }
}
