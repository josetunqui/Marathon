package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDTO {
    private boolean success;
    private String mensaje;
    private String sku;
    private Integer cantidadReservada;
    private Integer stockFisicoRestante;
}
