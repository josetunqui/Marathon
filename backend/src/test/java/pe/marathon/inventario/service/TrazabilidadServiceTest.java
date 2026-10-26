package pe.marathon.inventario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.MovimientoTrazabilidadDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.TrazabilidadSKUResponseDTO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.MovimientoTrazabilidad;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.impl.TrazabilidadServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrazabilidadServiceTest {

    @Mock
    private MovimientoTrazabilidadDAO movimientoDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ConteoFisicoDAO conteoFisicoDAO;

    @InjectMocks
    private TrazabilidadServiceImpl trazabilidadService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = Producto.builder()
                .sku("CAL-NIK-001")
                .ean("7751234001015")
                .descripcion("Zapatillas Nike Mercurial Vapor 15 Club FG/MG")
                .stockSap(14)
                .build();
    }

    @Test
    @DisplayName("TDD 9: Trazabilidad retorna métricas actuales y lista cronológica de eventos")
    void testObtenerTrazabilidadPorSku() {
        when(productoDAO.buscarPorSku("CAL-NIK-001")).thenReturn(Optional.of(producto));
        when(conteoFisicoDAO.buscarPorSku("CAL-NIK-001")).thenReturn(Optional.of(
                ConteoFisico.builder().sku("CAL-NIK-001").conteoFisico(12).build()
        ));

        MovimientoTrazabilidad m1 = MovimientoTrazabilidad.builder()
                .id(1L)
                .sku("CAL-NIK-001")
                .tipoBadge("Ajustado")
                .titulo("Ajuste de Auditoría Física")
                .descripcion("Confirmado por Joseph Churampi")
                .fechaHora(LocalDateTime.of(2026, 10, 9, 14, 40))
                .usuario("Joseph Aaron Churampi")
                .build();

        when(movimientoDAO.obtenerPorSku("CAL-NIK-001")).thenReturn(List.of(m1));

        TrazabilidadSKUResponseDTO response = trazabilidadService.obtenerTrazabilidadPorSku("CAL-NIK-001");

        assertNotNull(response);
        assertEquals("CAL-NIK-001", response.getSku());
        assertEquals(14, response.getStockTeoricoSap());
        assertEquals(12, response.getConteoFisicoReal());
        assertEquals(-2, response.getDiferencia());
        assertEquals("-2 u.", response.getDiferenciaTexto());
        assertEquals(1, response.getHistorialMovimientos().size());
        assertEquals("Ajustado", response.getHistorialMovimientos().get(0).getTipoBadge());
    }

    @Test
    @DisplayName("TDD 10: Registrar movimiento almacena correctamente en DAO")
    void testRegistrarMovimiento() {
        trazabilidadService.registrarMovimiento(
                "CAL-NIK-001", "Conteo Físico", "Escaneo de terminal", "12 unidades", "Jose Tunqui", 14, 12, -2
        );

        verify(movimientoDAO, times(1)).guardar(any(MovimientoTrazabilidad.class));
    }
}
