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
import pe.marathon.inventario.dto.ScanRequestDTO;
import pe.marathon.inventario.dto.ScanResponseDTO;
import pe.marathon.inventario.dto.ZonaProgresoDTO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.impl.ConteoServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConteoServiceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ConteoFisicoDAO conteoFisicoDAO;

    @Mock
    private ITrazabilidadService trazabilidadService;

    @InjectMocks
    private ConteoServiceImpl conteoService;

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = Producto.builder()
                .sku("CAL-NIK-002")
                .ean("790123456712")
                .marca("Nike")
                .descripcion("Zapatillas Nike Air Max Excee Running")
                .linea("Calzado")
                .talla("42")
                .color("Blanco/Azul")
                .ubicacion("Pasillo A - Estante 03")
                .stockSap(10)
                .precioUnitario(389.00)
                .costoUnitario(210.00)
                .build();
    }

    @Test
    @DisplayName("TDD 1: Escaneo de código existente incrementa el conteo físico y retorna DTO con datos completos")
    void testRegistrarEscaneoExitoso() {
        // Arrange
        ScanRequestDTO request = ScanRequestDTO.builder()
                .codigo("790123456712")
                .cantidad(1)
                .usuario("Jose Armando Tunqui")
                .ubicacion("Pasillo A - Estante 03")
                .build();

        when(productoDAO.buscarPorCodigo("790123456712")).thenReturn(Optional.of(productoPrueba));
        when(conteoFisicoDAO.buscarPorSku("CAL-NIK-002")).thenReturn(Optional.of(
                ConteoFisico.builder().sku("CAL-NIK-002").conteoFisico(10).fechaConteo(LocalDateTime.now()).build()
        ));

        // Act
        ScanResponseDTO response = conteoService.registrarEscaneo(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("CAL-NIK-002", response.getSku());
        assertEquals("Zapatillas Nike Air Max Excee Running", response.getDescripcion());
        assertEquals(11, response.getConteoAcumulado());
        assertEquals(1, response.getCantidadRegistrada());
        assertNotNull(response.getHoraRegistro());

        verify(conteoFisicoDAO, times(1)).guardar(any(ConteoFisico.class));
        verify(trazabilidadService, times(1)).registrarMovimiento(
                eq("CAL-NIK-002"), eq("Conteo Físico"), anyString(), anyString(), eq("Jose Armando Tunqui"), eq(10), eq(11), eq(1)
        );
    }

    @Test
    @DisplayName("TDD 2: Escaneo de código inexistente retorna respuesta controlada")
    void testRegistrarEscaneoCodigoInexistente() {
        // Arrange
        ScanRequestDTO request = ScanRequestDTO.builder()
                .codigo("999999999999")
                .cantidad(1)
                .build();

        when(productoDAO.buscarPorCodigo("999999999999")).thenReturn(Optional.empty());
        when(productoDAO.buscarPorSku("999999999999")).thenReturn(Optional.empty());
        when(productoDAO.buscarPorEan("999999999999")).thenReturn(Optional.empty());

        // Act
        ScanResponseDTO response = conteoService.registrarEscaneo(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMensaje().contains("no fue encontrado"));
    }

    @Test
    @DisplayName("TDD 3: Cálculo del progreso de zona retorna valores consistentes")
    void testObtenerProgresoZona() {
        when(conteoFisicoDAO.contarSkusContados()).thenReturn(9);

        ZonaProgresoDTO progreso = conteoService.obtenerProgresoZona("Pasillo A");

        assertNotNull(progreso);
        assertEquals("Pasillo A", progreso.getNombreZona());
        assertTrue(progreso.getPorcentajeCompletado() > 0 && progreso.getPorcentajeCompletado() <= 100);
        assertTrue(progreso.getSkusContados() > 0);
        assertEquals(progreso.getTotalSkus(), progreso.getSkusContados() + progreso.getSkusPendientes());
    }
}
