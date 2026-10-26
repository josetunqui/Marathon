package pe.marathon.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad para registrar el historial de movimientos, escaneos y ajustes
 * de auditoría para la Trazabilidad SKU.
 * Cumple con principios de normalización relacional: almacena temporalidad estrictamente en 'fechaHora' (DATETIME).
 */
@Entity
@Table(name = "movimientos_trazabilidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoTrazabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 30, nullable = false)
    private String sku;

    @Column(name = "tipo_badge", length = 50, nullable = false) // "Ajustado", "Conteo Físico", "MB52 Sync", "Reserva Pre-Venta"
    private String tipoBadge;

    @Column(name = "titulo", length = 150, nullable = false) // "Ajuste de Auditoría Física de Inventario"
    private String titulo;

    @Column(name = "descripcion", length = 500, nullable = false) // "Confirmado por Joseph Churampi - Ajuste de merma en sistema SAP"
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "stock_sap")
    private Integer stockSap;

    @Column(name = "conteo_fisico")
    private Integer conteoFisico;

    @Column(name = "diferencia")
    private Integer diferencia;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }
}
