package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDemoDTO {
    private String username;
    private String password;
    private String nombreCompleto;
    private String rol;
    private String descripcion;
}
