package pe.marathon.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que registra el conteo físico consolidado de un producto durante la auditoría.
 */
@Entity
@Table(name = "conteos_fisicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConteoFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 30, nullable = false, unique = true)
    private String sku;

    @Column(name = "conteo_fisico", nullable = false)
    private Integer conteoFisico; // Unidades contadas físicamente

    @Column(name = "fecha_conteo")
    private LocalDateTime fechaConteo;

    @Column(name = "escaneado_por", length = 100)
    private String escaneadoPor; // Nombre del operador o supervisor

    @Column(name = "ubicacion", length = 100)
    private String ubicacion; // Pasillo / Góndola donde se contó

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.fechaConteo == null) {
            this.fechaConteo = LocalDateTime.now();
        }
    }
}
