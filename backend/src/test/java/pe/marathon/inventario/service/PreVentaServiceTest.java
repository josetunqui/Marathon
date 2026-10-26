package pe.marathon.inventario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.PreVentaResponseDTO;
import pe.marathon.inventario.dto.ReservaRequestDTO;
import pe.marathon.inventario.dto.ReservaResponseDTO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.impl.PreVentaServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PreVentaServiceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ConteoFisicoDAO conteoFisicoDAO;

    @Mock
    private ITrazabilidadService trazabilidadService;

    @InjectMocks
    private PreVentaServiceImpl preVentaService;

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = Producto.builder()
                .sku("CAL-NIK-002")
                .ean("790123456712")
                .marca("Nike")
                .descripcion("Zapatillas Nike Air Max Excee Running")
                .color("Blanco/Azul")
                .precioUnitario(389.00)
                .stockSap(10)
                .stockPisoVenta(4)
                .stockAlmacen(5)
                .stockVitrina(1)
                .build();
    }

    @Test
    @DisplayName("TDD 7: Consulta pre-venta retorna desglose por 3 ubicaciones físicas y stock total")
    void testConsultarStock() {
        when(productoDAO.buscarPorCodigo("790123456712")).thenReturn(Optional.of(productoPrueba));
        when(conteoFisicoDAO.buscarPorSku("CAL-NIK-002")).thenReturn(Optional.of(
                ConteoFisico.builder().sku("CAL-NIK-002").conteoFisico(10).build()
        ));

        PreVentaResponseDTO response = preVentaService.consultarStock("790123456712");

        assertNotNull(response);
        assertTrue(response.isEncontrado());
        assertEquals("CAL-NIK-002", response.getSku());
        assertEquals(389.00, response.getPrecioUnitario());
        assertEquals(10, response.getStockTotalFisico());
        assertTrue(response.isCoincideSap());
        assertEquals(3, response.getDistribucionUbicaciones().size());

        assertEquals("Piso de Venta (Pasillo A - Estante 03)", response.getDistribucionUbicaciones().get(0).getNombreUbicacion());
        assertEquals(4, response.getDistribucionUbicaciones().get(0).getCantidad());
        assertEquals("Almacén Principal (Bloque B - Altura 1)", response.getDistribucionUbicaciones().get(1).getNombreUbicacion());
        assertEquals(5, response.getDistribucionUbicaciones().get(1).getCantidad());
        assertEquals("Vitrina Frontal Sede Atocongo", response.getDistribucionUbicaciones().get(2).getNombreUbicacion());
        assertEquals(1, response.getDistribucionUbicaciones().get(2).getCantidad());
    }

    @Test
    @DisplayName("TDD 8: Reserva de stock descuenta de Piso de Venta y genera movimiento de trazabilidad")
    void testReservarStockExitoso() {
        when(productoDAO.buscarPorSku("CAL-NIK-002")).thenReturn(Optional.of(productoPrueba));

        ReservaRequestDTO request = ReservaRequestDTO.builder()
                .sku("CAL-NIK-002")
                .cantidad(1)
                .usuario("Asesor Comercial")
                .cliente("Juan Pérez")
                .build();

        ReservaResponseDTO response = preVentaService.reservarStock(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getCantidadReservada());
        assertEquals(3, response.getStockFisicoRestante()); // 4 - 1 = 3

        verify(productoDAO, times(1)).guardar(productoPrueba);
        verify(trazabilidadService, times(1)).registrarMovimiento(
                eq("CAL-NIK-002"), eq("Reserva Pre-Venta"), anyString(), anyString(), eq("Asesor Comercial"), any(), any(), any()
        );
    }
}
