package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionStockDTO {
    private String nombreUbicacion; // "Piso de Venta (Pasillo A - Estante 03)"
    private String subtitulo; // "Disponible", "En Reserva", "Exhibición"
    private String tipo; // "disponible", "reserva", "vitrina"
    private Integer cantidad; // 4
}
