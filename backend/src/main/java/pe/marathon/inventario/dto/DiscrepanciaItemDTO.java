package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscrepanciaItemDTO {
    private Long id;
    private String linea; // "CALZADO", "ROPA", "ACCESORIOS"
    private String lineaBadgeColor; // "danger", "warning", "info"
    private String sku; // "CAL-NIK-001"
    private String descripcion; // "Zapatillas Nike Mercurial Vapor 15 Club"
    private String subtituloUbicacion; // "Atocongo • Anaquel 04"
    private Integer stockSap; // 14
    private Integer conteoFisico; // 12
    private Integer diferencia; // -2
    private String sapVsFisTexto; // "14 vs 12 (-2)"
    private String colorDiferencia; // "danger", "warning", "primary"
    private String causaInvestigada; // "Hurto en Piso de Venta"
    private String evidenciaNota; // "Caja vacía en góndola principal"
    private Boolean estadoAprobado; // false = Pendiente, true = Aprobado
}
