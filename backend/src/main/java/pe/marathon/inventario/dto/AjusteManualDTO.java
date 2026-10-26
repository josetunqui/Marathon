package pe.marathon.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteManualDTO {

    @NotBlank(message = "El SKU del producto es obligatorio")
    private String sku;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "La cantidad física no puede ser negativa")
    private Integer cantidad;

    private String operador;
    private String motivo;
}
