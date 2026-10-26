package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.TrazabilidadSKUResponseDTO;
import pe.marathon.inventario.service.ITrazabilidadService;

@RestController
@RequestMapping("/api/v1/trazabilidad")
@RequiredArgsConstructor
@Tag(name = "Trazabilidad SKU", description = "Auditoría completa de discrepancias y registros cronológicos de movimientos")
public class TrazabilidadController {

    private final ITrazabilidadService trazabilidadService;

    @GetMapping("/{sku}")
    @Operation(summary = "Obtener historial y trazabilidad de un SKU", description = "Devuelve el estado actual SAP vs Físico y el timeline cronológico")
    public ResponseEntity<TrazabilidadSKUResponseDTO> getTrazabilidad(@PathVariable String sku) {
        return ResponseEntity.ok(trazabilidadService.obtenerTrazabilidadPorSku(sku));
    }

    @GetMapping
    @Operation(summary = "Obtener trazabilidad del producto principal", description = "Devuelve trazabilidad por defecto del SKU principal")
    public ResponseEntity<TrazabilidadSKUResponseDTO> getTrazabilidadDefault() {
        return ResponseEntity.ok(trazabilidadService.obtenerTrazabilidadPorSku("CAL-NIK-001"));
    }
}
