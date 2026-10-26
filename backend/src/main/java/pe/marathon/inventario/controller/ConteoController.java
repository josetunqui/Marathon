package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.CapturaSesionDTO;
import pe.marathon.inventario.dto.ScanRequestDTO;
import pe.marathon.inventario.dto.ScanResponseDTO;
import pe.marathon.inventario.dto.ZonaProgresoDTO;
import pe.marathon.inventario.service.IConteoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conteo")
@RequiredArgsConstructor
@Tag(name = "Conteo Escáner", description = "Captura rápida de conteo físico vía terminal RF y escáner")
public class ConteoController {

    private final IConteoService conteoService;

    @PostMapping("/escanear")
    @Operation(summary = "Registrar escaneo de código de barras", description = "Captura +1, +5, +10 unidades o ajuste manual de un SKU")
    public ResponseEntity<ScanResponseDTO> registrarEscaneo(@RequestBody ScanRequestDTO request) {
        ScanResponseDTO response = conteoService.registrarEscaneo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progreso-zona")
    @Operation(summary = "Obtener porcentaje y progreso de la zona actual", description = "Calcula el avance de la zona seleccionada (ej. Pasillo A)")
    public ResponseEntity<ZonaProgresoDTO> getProgresoZona(@RequestParam(defaultValue = "Pasillo A") String zona) {
        return ResponseEntity.ok(conteoService.obtenerProgresoZona(zona));
    }

    @GetMapping("/ultimas-capturas")
    @Operation(summary = "Obtener últimas capturas de la sesión", description = "Devuelve el historial en tiempo real de los últimos escaneos")
    public ResponseEntity<List<CapturaSesionDTO>> getUltimasCapturas(@RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(conteoService.obtenerUltimasCapturas(limite));
    }
}
