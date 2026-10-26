package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.marathon.inventario.model.AuditoriaMerma;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReconciliacionDTO {
    private String sku;
    private String ean;
    private String linea;
    private String marca;
    private String descripcion;
    private String talla;
    private String color;
    private String ubicacion;
    private Integer stockSap;
    private Integer conteoFisico;
    private boolean tieneConteo;
    private Integer diferencia;
    private String estado; // "CONFORME", "SOBRANTE", "FALTANTE", "NO CONTADO"
    private String badgeClass; // "badge-conforme", "badge-faltante", "badge-sobrante", "badge-no-contado"
    private Double precioUnitario;
    private Double costoUnitario;
    private Double valorDiferencia;
    private Double costoDiferencia;
    private boolean estaValidado;
    private AuditoriaMerma auditoria;
}
