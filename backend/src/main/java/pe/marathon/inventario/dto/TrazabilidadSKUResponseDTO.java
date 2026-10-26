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
public class TrazabilidadSKUResponseDTO {
    private String sku;
    private String ean;
    private String nombreProducto; // "Nike Mercurial Vapor 15"
    private String sedeAlmacen; // "Sede Atocongo - Almacén A"
    private Integer stockTeoricoSap; // 14
    private Integer conteoFisicoReal; // 12
    private Integer diferencia; // -2
    private String diferenciaTexto; // "-2 u."
    private List<MovimientoItemDTO> historialMovimientos;
}
