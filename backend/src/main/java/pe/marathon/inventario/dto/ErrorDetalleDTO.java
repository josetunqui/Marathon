package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetalleDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String ruta;
    private Map<String, String> validaciones;
}
