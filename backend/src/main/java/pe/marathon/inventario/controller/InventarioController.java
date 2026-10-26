package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.config.DataLoader;
import pe.marathon.inventario.dto.*;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.HistorialEscaneo;
import pe.marathon.inventario.service.IConteoService;
import pe.marathon.inventario.service.ReconciliationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventario")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Inventario & Conteo Cíclico", description = "Operaciones de conciliación, lecturas de escáner y ajustes")
public class InventarioController {

    private final ReconciliationService reconciliationService;
    private final IConteoService conteoService;
    private final DataLoader dataLoader;

    @GetMapping("/reconciliacion")
    @Operation(summary = "Obtener reporte de cruce Stock SAP vs Físico con cálculo de IRA %")
    public ResponseEntity<ReporteReconciliacionDTO> obtenerReconciliacion(
            @RequestParam(name = "linea", defaultValue = "ALL") String linea) {
        return ResponseEntity.ok(reconciliationService.generarReporte(linea));
    }

    @GetMapping("/kpis")
    @Operation(summary = "Obtener resumen ejecutivo de KPIs (IRA %, exactitud y valorizaciones)")
    public ResponseEntity<ResumenKpiDTO> obtenerKpis(
            @RequestParam(name = "linea", defaultValue = "ALL") String linea) {
        ReporteReconciliacionDTO reporte = reconciliationService.generarReporte(linea);
        return ResponseEntity.ok(reporte.getKpis());
    }

    @PostMapping("/scan")
    @Operation(summary = "Registrar lectura de código de barras (EAN-13 o SKU) desde escáner o pistola")
    public ResponseEntity<ScanResponseDTO> registrarEscaneo(@Valid @RequestBody ScanRequestDTO request) {
        return ResponseEntity.ok(conteoService.registrarEscaneo(request));
    }

    @PutMapping("/ajuste-manual")
    @Operation(summary = "Ajustar manualmente la cantidad contada de un SKU (Recuento o supervisión)")
    public ResponseEntity<ConteoFisico> ajusteManual(@Valid @RequestBody AjusteManualDTO ajuste) {
        return ResponseEntity.ok(conteoService.actualizarConteoExacto(ajuste));
    }

    @GetMapping("/historial-escaneos")
    @Operation(summary = "Obtener las últimas 50 lecturas de código de barras para la consola del escáner")
    public ResponseEntity<List<HistorialEscaneo>> obtenerHistorial() {
        return ResponseEntity.ok(conteoService.obtenerHistorialReciente());
    }

    @PostMapping("/reseed-demo")
    @Operation(summary = "Restaurar datos de demostración a su estado inicial de conteo cíclico")
    public ResponseEntity<Map<String, Object>> reseedDemoData() {
        dataLoader.inicializarTodo(true);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("mensaje", "Datos de prueba de Marathon Atocongo restaurados con éxito");
        resp.put("totalSkus", 22);
        resp.put("sede", "Marathon Sports Sede Atocongo");
        return ResponseEntity.ok(resp);
    }
}
