package pe.marathon.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanRequestDTO {

    @NotBlank(message = "El código EAN-13 o SKU es obligatorio para la lectura")
    private String codigo; // SKU o EAN

    @Min(value = 1, message = "La cantidad registrada debe ser de al menos 1 unidad")
    private Integer cantidad; // +1, +5, +10 o manual

    private String usuario; // e.g. "Joseph Aaron Churampi"
    private String operador; // alias para compatibilidad
    private String ubicacion; // e.g. "Pasillo A - Estante 03"

    public String getUsuario() {
        return usuario != null ? usuario : operador;
    }

    public String getOperador() {
        return operador != null ? operador : usuario;
    }
}
