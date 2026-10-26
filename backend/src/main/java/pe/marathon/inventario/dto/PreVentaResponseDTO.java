package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreVentaResponseDTO {
    private boolean encontrado;
    private String sku;
    private String ean;
    private String marca;
    private String descripcion;
    private String color;
    private Double precioUnitario;
    private Integer stockTotalFisico;
    private Integer stockSap;
    private boolean coincideSap;
    private String notaSap; // "Coincide 100% con Teórico SAP"
    private List<UbicacionStockDTO> distribucionUbicaciones;
}
