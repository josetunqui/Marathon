package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Catálogo SAP MB52", description = "Consulta del maestro de artículos y stock teórico")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar catálogo de productos (filtrado opcional por línea o búsqueda parcial por SKU/nombre/EAN)")
    public ResponseEntity<List<Producto>> listarProductos(
            @RequestParam(name = "linea", required = false) String linea,
            @RequestParam(name = "q", required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(productoService.buscarPorTerminoEntities(q.trim()));
        }
        return ResponseEntity.ok(productoService.listarTodos(linea));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Buscar producto por SKU o código de barras EAN-13")
    public ResponseEntity<Producto> buscarPorCodigo(@PathVariable String codigo) {
        return productoService.buscarPorSkuOEan(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
