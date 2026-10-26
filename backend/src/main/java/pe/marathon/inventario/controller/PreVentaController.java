package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.PreVentaResponseDTO;
import pe.marathon.inventario.dto.ReservaRequestDTO;
import pe.marathon.inventario.dto.ReservaResponseDTO;
import pe.marathon.inventario.service.IPreVentaService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/preventa")
@RequiredArgsConstructor
@Tag(name = "Consulta Pre-Venta", description = "Verificación de ubicaciones exactas y disponibilidad física antes de procesar venta")
public class PreVentaController {

    private final IPreVentaService preVentaService;

    @GetMapping("/consulta")
    @Operation(summary = "Consultar disponibilidad y distribución física de un SKU", description = "Muestra stock en Piso de Venta, Almacén Principal y Vitrina Frontal")
    public ResponseEntity<PreVentaResponseDTO> consultarStock(@RequestParam(required = false, defaultValue = "Nike Air Max Excee Running") String q) {
        return ResponseEntity.ok(preVentaService.consultarStock(q));
    }

    @PostMapping("/reservar")
    @Operation(summary = "Reservar producto para pre-venta", description = "Aparta temporalmente una unidad en Piso de Venta")
    public ResponseEntity<ReservaResponseDTO> reservarStock(@RequestBody ReservaRequestDTO request) {
        return ResponseEntity.ok(preVentaService.reservarStock(request));
    }

    @PostMapping("/solicitar-auditoria")
    @Operation(summary = "Solicitar auditoría rápida", description = "Genera alerta de auditoría por discrepancia detectada en tienda")
    public ResponseEntity<?> solicitarAuditoriaRapida(@RequestBody Map<String, String> payload) {
        String sku = payload.getOrDefault("sku", "CAL-NIK-002");
        String motivo = payload.getOrDefault("motivo", "Discrepancia detectada en mostrador de Pre-Venta");
        boolean ok = preVentaService.solicitarAuditoriaRapida(sku, motivo);
        return ResponseEntity.ok(Map.of("success", ok, "mensaje", "Solicitud de auditoría enviada al Jefe de Inventario"));
    }
}
