package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {
    private String sku;
    private String ean;
    private String linea;
    private String marca;
    private String descripcion;
    private String talla;
    private String color;
    private String ubicacion;
    private Integer stockSap;
    private Double precioUnitario;
    private Double costoUnitario;
    private Integer stockPisoVenta;
    private Integer stockAlmacen;
    private Integer stockVitrina;
}
