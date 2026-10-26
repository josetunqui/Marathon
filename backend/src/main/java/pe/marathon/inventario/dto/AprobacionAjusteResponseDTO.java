package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobacionAjusteResponseDTO {
    private boolean success;
    private String mensaje;
    private Long auditoriaId;
    private String sku;
    private boolean estadoAprobado;
    private String validadoPor;
}
