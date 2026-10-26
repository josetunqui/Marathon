package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaRequestDTO {
    private String sku;
    private String tipo; // "Faltante (-2)", "Sobrante (+1)"
    private String causal;
    private String comentario;
    private String validadoPor;
}
