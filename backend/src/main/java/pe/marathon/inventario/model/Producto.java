package pe.marathon.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un producto del catálogo de Marathon Sports sincronizado con SAP MB52.
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @Column(name = "sku", length = 30, nullable = false, unique = true)
    private String sku; // Ejemplo: "CAL-NIK-001"

    @Column(name = "ean", length = 30, nullable = false, unique = true)
    private String ean; // Código de barras EAN-13: "7751234001015"

    @Column(name = "linea", length = 50, nullable = false)
    private String linea; // "Calzado", "Ropa", "Accesorios"

    @Column(name = "marca", length = 50, nullable = false)
    private String marca; // "Nike", "Adidas", "Puma", "Marathon"

    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    @Column(name = "talla", length = 20)
    private String talla; // "41", "M", "N°5"

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion; // "Pasillo A - Estante 02"

    @Column(name = "stock_sap", nullable = false)
    private Integer stockSap; // Stock teórico según SAP

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario; // Precio de venta en Soles (S/.)

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario; // Costo valorizado en Soles (S/.)

    @Column(name = "stock_piso_venta")
    private Integer stockPisoVenta;

    @Column(name = "stock_almacen")
    private Integer stockAlmacen;

    @Column(name = "stock_vitrina")
    private Integer stockVitrina;

    public Integer getStockPisoVenta() {
        return stockPisoVenta != null ? stockPisoVenta : (stockSap != null ? (int) Math.round(stockSap * 0.4) : 0);
    }

    public Integer getStockAlmacen() {
        return stockAlmacen != null ? stockAlmacen : (stockSap != null ? (int) Math.round(stockSap * 0.5) : 0);
    }

    public Integer getStockVitrina() {
        return stockVitrina != null ? stockVitrina : (stockSap != null ? Math.max(0, stockSap - getStockPisoVenta() - getStockAlmacen()) : 0);
    }
}
