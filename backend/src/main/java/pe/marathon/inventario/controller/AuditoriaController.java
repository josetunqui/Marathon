package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.AprobacionAjusteResponseDTO;
import pe.marathon.inventario.dto.AprobarAjusteRequestDTO;
import pe.marathon.inventario.dto.DiscrepanciaItemDTO;
import pe.marathon.inventario.dto.ResumenDiscrepanciasDTO;
import pe.marathon.inventario.service.IAuditoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditorias")
@RequiredArgsConstructor
@Tag(name = "Validación & Auditoría", description = "Revisión de diferencias críticas, causas investigadas y aprobación de ajustes")
public class AuditoriaController {

    private final IAuditoriaService auditoriaService;

    @GetMapping("/pendientes")
    @Operation(summary = "Listar discrepancias abiertas / pendientes de aprobación", description = "Devuelve la tabla de auditorías abiertas para hoy")
    public ResponseEntity<List<DiscrepanciaItemDTO>> getDiscrepanciasAbiertas() {
        return ResponseEntity.ok(auditoriaService.obtenerDiscrepanciasAbiertas());
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtener resumen de discrepancias (Faltantes Críticos y Sobrantes)", description = "Devuelve contadores de faltantes y sobrantes")
    public ResponseEntity<ResumenDiscrepanciasDTO> getResumen() {
        return ResponseEntity.ok(auditoriaService.obtenerResumenDiscrepancias());
    }

    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar ajuste de inventario", description = "Confirma el ajuste de merma/sobrante y cuadra el stock en SAP")
    public ResponseEntity<AprobacionAjusteResponseDTO> aprobarAjuste(
            @PathVariable Long id,
            @RequestBody(required = false) AprobarAjusteRequestDTO request) {
        return ResponseEntity.ok(auditoriaService.aprobarAjuste(id, request));
    }

    @PostMapping("/crear")
    @Operation(summary = "Registrar nueva auditoría de discrepancia", description = "Permite al auditor registrar una investigación de discrepancia en cualquier SKU")
    public ResponseEntity<DiscrepanciaItemDTO> crearAuditoria(@RequestBody pe.marathon.inventario.model.AuditoriaMerma auditoria) {
        return ResponseEntity.ok(auditoriaService.registrarNuevaAuditoria(auditoria));
    }

    @GetMapping
    @Operation(summary = "Listar todas las auditorías", description = "Devuelve todas las auditorías registradas")
    public ResponseEntity<List<DiscrepanciaItemDTO>> listarTodas() {
        return ResponseEntity.ok(auditoriaService.obtenerDiscrepanciasAbiertas());
    }
}
