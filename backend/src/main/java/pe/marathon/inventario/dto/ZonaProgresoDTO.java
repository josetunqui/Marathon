package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZonaProgresoDTO {
    private String nombreZona; // "Pasillo A"
    private int porcentajeCompletado; // 78
    private int skusContados; // 35
    private int skusPendientes; // 10
    private int totalSkus; // 45
}
