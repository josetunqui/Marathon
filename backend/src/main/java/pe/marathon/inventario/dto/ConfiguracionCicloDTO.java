package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCicloDTO {
    private String frecuencia; // "3 Conteos Fraccionados al Mes (Días 9, 19, 29)"
    private List<String> lineasMuestreo; // ["Calzado Deportivo", "Ropa & Textil", "Accesorios"]
    private String reglaMuestreo; // "Filtro activo: Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma)."
    private String responsableCalzado; // "Jose Armando Tunqui"
    private String rolResponsableCalzado; // "Auditor Principal Calzado"
    private String responsableJefe; // "Joseph Aaron Churampi"
    private String rolResponsableJefe; // "Jefe de Inventario"
}
