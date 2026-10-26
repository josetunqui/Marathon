package pe.marathon.inventario.reporte;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.marathon.inventario.dto.ItemReconciliacionDTO;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.dto.ResumenKpiDTO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.service.reporte.ExcelReporteService;
import pe.marathon.inventario.service.reporte.PdfReporteService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Exportación: Apache POI (Excel) y OpenPDF (PDF)")
public class ReporteExportTest {

    private final ExcelReporteService excelService = new ExcelReporteService();
    private final PdfReporteService pdfService = new PdfReporteService();

    private ReporteReconciliacionDTO crearReporteMock() {
        List<ItemReconciliacionDTO> items = new ArrayList<>();
        items.add(ItemReconciliacionDTO.builder()
                .sku("CAL-NIK-002")
                .ean("790123456712")
                .linea("Calzado")
                .marca("Nike")
                .descripcion("Zapatillas Nike Air Max Excee Running")
                .talla("42")
                .color("Blanco / Azul")
                .ubicacion("Pasillo A - Estante 03")
                .stockSap(10)
                .conteoFisico(10)
                .tieneConteo(true)
                .diferencia(0)
                .estado("CONFORME")
                .costoUnitario(210.0)
                .costoDiferencia(0.0)
                .build());

        items.add(ItemReconciliacionDTO.builder()
                .sku("CAL-NIK-001")
                .ean("7751234001015")
                .linea("Calzado")
                .marca("Nike")
                .descripcion("Zapatillas Nike Mercurial Vapor 15 Club FG/MG")
                .talla("41")
                .color("Negro / Carmesí")
                .ubicacion("Atocongo • Anaquel 04")
                .stockSap(14)
                .conteoFisico(12)
                .tieneConteo(true)
                .diferencia(-2)
                .estado("FALTANTE")
                .costoUnitario(160.0)
                .costoDiferencia(-320.0)
                .build());

        ResumenKpiDTO kpis = ResumenKpiDTO.builder()
                .totalSkus(2)
                .totalSap(24)
                .totalFisico(22)
                .conformes(1)
                .faltantes(1)
                .sobrantes(0)
                .ira(91.7)
                .iraCalificacion("Alerta")
                .build();

        return ReporteReconciliacionDTO.builder()
                .fechaGeneracion(LocalDateTime.now())
                .sede("Marathon Sports Sede Atocongo")
                .lineaFiltro("ALL")
                .kpis(kpis)
                .items(items)
                .build();
    }

    @Test
    @DisplayName("Apache POI: Debe generar bytes válidos de archivo Excel (.xlsx)")
    void testGenerarExcelReconciliacion() throws IOException {
        ReporteReconciliacionDTO reporte = crearReporteMock();
        byte[] excelBytes = excelService.generarExcelReconciliacion(reporte);

        assertNotNull(excelBytes, "El arreglo de bytes del Excel no debe ser nulo");
        assertTrue(excelBytes.length > 1000, "El archivo Excel generado debe tener un tamaño válido (> 1KB)");

        // Firma del encabezado de archivo ZIP/OOXML (PK..)
        assertEquals((byte) 0x50, excelBytes[0], "El archivo debe iniciar con la firma PK (ZIP/OOXML)");
        assertEquals((byte) 0x4B, excelBytes[1], "El archivo debe iniciar con la firma PK (ZIP/OOXML)");
    }

    @Test
    @DisplayName("OpenPDF: Debe generar bytes válidos de documento PDF con firma legal")
    void testGenerarPdfActaAuditoria() {
        ReporteReconciliacionDTO reporte = crearReporteMock();
        List<AuditoriaMerma> auditorias = Collections.singletonList(
                AuditoriaMerma.builder()
                        .id(1L)
                        .sku("CAL-NIK-001")
                        .descripcionProducto("Zapatillas Nike Mercurial Vapor 15")
                        .linea("Calzado")
                        .ubicacion("Atocongo • Anaquel 04")
                        .stockSap(14)
                        .conteoFisico(12)
                        .diferencia(-2)
                        .tipo("Faltante (-2)")
                        .causal("Hurto en Piso de Venta")
                        .comentario("Caja vacía detectada")
                        .validadoPor("Joseph Aaron Churampi")
                        .estadoAprobado(false)
                        .build()
        );

        byte[] pdfBytes = pdfService.generarActaAuditoriaPdf(reporte, auditorias);

        assertNotNull(pdfBytes, "El arreglo de bytes del PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 1000, "El archivo PDF generado debe tener un tamaño válido (> 1KB)");

        // Firma de encabezado PDF standard (%PDF)
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }
}
