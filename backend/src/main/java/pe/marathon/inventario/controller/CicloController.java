package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.CicloConteoDTO;
import pe.marathon.inventario.dto.ConfiguracionCicloDTO;
import pe.marathon.inventario.service.ICicloService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ciclos")
@RequiredArgsConstructor
@Tag(name = "Configuración de Ciclo", description = "Planificación de reglas, frecuencias, categorías y asignación de responsables")
public class CicloController {

    private final ICicloService cicloService;

    @GetMapping("/configuracion")
    @Operation(summary = "Obtener configuración activa de ciclo de inventario", description = "Devuelve frecuencias, categorías y responsables asignados")
    public ResponseEntity<ConfiguracionCicloDTO> getConfiguracion() {
        return ResponseEntity.ok(cicloService.obtenerConfiguracionActiva());
    }

    @PutMapping("/configuracion")
    @Operation(summary = "Guardar configuración de ciclo de inventario", description = "Actualiza las reglas de muestreo, frecuencia y responsables")
    public ResponseEntity<ConfiguracionCicloDTO> guardarConfiguracion(@RequestBody ConfiguracionCicloDTO dto) {
        return ResponseEntity.ok(cicloService.guardarConfiguracion(dto));
    }

    @PostMapping("/configuracion")
    @Operation(summary = "Guardar configuración de ciclo de inventario (POST)", description = "Alias POST para actualizar configuración")
    public ResponseEntity<ConfiguracionCicloDTO> guardarConfiguracionPost(@RequestBody ConfiguracionCicloDTO dto) {
        return ResponseEntity.ok(cicloService.guardarConfiguracion(dto));
    }

    @GetMapping
    @Operation(summary = "Listar los ciclos programados", description = "Obtiene los 3 ciclos mensuales (Días 9, 19 y 28)")
    public ResponseEntity<List<CicloConteoDTO>> listarCiclos() {
        return ResponseEntity.ok(cicloService.listarCiclos());
    }
}
