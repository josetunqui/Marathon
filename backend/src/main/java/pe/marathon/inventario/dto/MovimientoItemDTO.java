package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoItemDTO {
    private Long id;
    private String tipoBadge; // "Ajustado", "Conteo Físico", "MB52 Sync"
    private String badgeColor; // "danger", "primary", "success"
    private String fechaTexto; // "Día 9 de c/mes, 14:40"
    private String titulo; // "Ajuste de Auditoría Física de Inventario"
    private String descripcion; // "Confirmado por Joseph Churampi - Ajuste de merma en sistema SAP"
    private String usuario;
}
