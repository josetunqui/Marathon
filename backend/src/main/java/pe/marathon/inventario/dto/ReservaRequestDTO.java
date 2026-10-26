package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaRequestDTO {
    private String sku;
    private Integer cantidad;
    private String cliente;
    private String usuario;
}
