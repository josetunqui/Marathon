package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dao.AuditoriaDAO;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.service.ReconciliationService;
import pe.marathon.inventario.service.reporte.ExcelReporteService;
import pe.marathon.inventario.service.reporte.PdfReporteService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador de Reportes y Exportaciones.
 * Implementa las librerías Apache POI (Excel) y OpenPDF (PDF).
 */
@RestController
@RequestMapping("/api/v1/reportes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Reportes & Exportaciones", description = "Exportación de conciliación y auditorías en formatos Excel (Apache POI) y PDF (OpenPDF)")
public class ReporteController {

    private final ReconciliationService reconciliationService;
    private final ExcelReporteService excelReporteService;
    private final PdfReporteService pdfReporteService;
    private final AuditoriaDAO auditoriaDAO;

    @GetMapping("/excel/reconciliacion")
    @Operation(summary = "Exportar reporte de reconciliación a Excel (.xlsx) con Apache POI")
    public ResponseEntity<byte[]> exportarExcelReconciliacion(
            @RequestParam(name = "linea", defaultValue = "ALL") String linea) throws IOException {

        ReporteReconciliacionDTO reporte = reconciliationService.generarReporte(linea);
        byte[] excelBytes = excelReporteService.generarExcelReconciliacion(reporte);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "Reconciliacion_Marathon_Atocongo_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/excel/auditorias")
    @Operation(summary = "Exportar reporte de discrepancias y mermas a Excel (.xlsx) con Apache POI")
    public ResponseEntity<byte[]> exportarExcelAuditorias() throws IOException {
        List<AuditoriaMerma> auditorias = auditoriaDAO.obtenerTodas();
        byte[] excelBytes = excelReporteService.generarExcelAuditorias(auditorias);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "Auditorias_Merma_Marathon_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/pdf/acta-auditoria")
    @Operation(summary = "Generar y descargar Acta Oficial de Auditoría en PDF con OpenPDF")
    public ResponseEntity<byte[]> generarActaAuditoriaPdf(
            @RequestParam(name = "linea", defaultValue = "ALL") String linea) {

        ReporteReconciliacionDTO reporte = reconciliationService.generarReporte(linea);
        List<AuditoriaMerma> auditorias = auditoriaDAO.obtenerTodas();
        byte[] pdfBytes = pdfReporteService.generarActaAuditoriaPdf(reporte, auditorias);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "Acta_Auditoria_Marathon_Atocongo_" + fecha + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
