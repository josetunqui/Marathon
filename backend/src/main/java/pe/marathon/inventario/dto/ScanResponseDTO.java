package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResponseDTO {
    private boolean success;
    private String mensaje;
    private String sku;
    private String ean;
    private String descripcion;
    private String marca;
    private String linea;
    private String talla;
    private String color;
    private String ubicacion;
    private Integer cantidadRegistrada;
    private Integer conteoAcumulado;
    private Integer conteoFisico;
    private Integer stockSap;
    private Integer diferencia;
    private String estado;
    private String horaRegistro;

    public Integer getConteoAcumulado() {
        return conteoAcumulado != null ? conteoAcumulado : conteoFisico;
    }

    public Integer getConteoFisico() {
        return conteoFisico != null ? conteoFisico : conteoAcumulado;
    }
}
