package pe.marathon.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que audita cada lectura de código de barras efectuada por el escáner.
 */
@Entity
@Table(name = "historial_escaneos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEscaneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 30, nullable = false)
    private String sku;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad; // +1 o valor escaneado

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
