package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenDiscrepanciasDTO {
    private int faltantesCriticos; // e.g. 3
    private int sobrantesBajoAnalisis; // e.g. 1
    private int totalPendientes;
    private int totalAprobados;
}
