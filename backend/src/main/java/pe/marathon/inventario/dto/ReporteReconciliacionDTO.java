package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteReconciliacionDTO {
    private LocalDateTime fechaGeneracion;
    private String sede;
    private String lineaFiltro;
    private ResumenKpiDTO kpis;
    private List<ItemReconciliacionDTO> items;
}
