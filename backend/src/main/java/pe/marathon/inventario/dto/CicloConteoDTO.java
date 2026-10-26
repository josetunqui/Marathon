package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CicloConteoDTO {
    private String id;
    private String linea;
    private Integer diaProgramado;
    private String nombre;
    private String descripcion;
    private String estado;
    private String frecuencia;
    private String responsable;
    private String fechaLimite;
}
