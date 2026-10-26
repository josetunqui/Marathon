package pe.marathon.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que registra las discrepancias y justificaciones de mermas o sobrantes
 * para la revisión y aprobación por el Jefe de Tienda.
 */
@Entity
@Table(name = "auditorias_merma")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaMerma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 30, nullable = false)
    private String sku;

    @Column(name = "descripcion_producto", length = 200)
    private String descripcionProducto;

    @Column(name = "linea", length = 50)
    private String linea;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "stock_sap")
    private Integer stockSap;

    @Column(name = "conteo_fisico")
    private Integer conteoFisico;

    @Column(name = "diferencia")
    private Integer diferencia;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo; // "Faltante (-2)", "Sobrante (+2)"

    @Column(name = "causal", length = 200, nullable = false)
    private String causal; // "Hurto en Piso de Venta", "Picking Traspapelado", "Error de Recepción"

    @Column(name = "comentario", length = 500)
    private String comentario; // Evidencia / Nota: "Caja vacía en góndola principal"

    @Column(name = "validado_por", length = 100)
    private String validadoPor; // Jefe de Tienda / Auditor

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(name = "estado_aprobado", nullable = false)
    private Boolean estadoAprobado; // false = Pendiente, true = Aprobado

    @PrePersist
    public void prePersist() {
        if (this.fechaValidacion == null) {
            this.fechaValidacion = LocalDateTime.now();
        }
        if (this.estadoAprobado == null) {
            this.estadoAprobado = false;
        }
    }
}
