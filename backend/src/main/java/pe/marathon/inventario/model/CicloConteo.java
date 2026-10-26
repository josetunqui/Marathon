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
 * Entidad que gestiona la programación mensual fraccionada (Días 9, 19 y 28).
 */
@Entity
@Table(name = "ciclos_conteo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CicloConteo {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id; // "ciclo-calzado-09", "ciclo-ropa-19", "ciclo-accesorios-28"

    @Column(name = "linea", length = 50, nullable = false)
    private String linea; // "Calzado", "Ropa", "Accesorios"

    @Column(name = "dia_programado", nullable = false)
    private Integer diaProgramado; // 9, 19 o 28

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado; // "Pendiente", "En Proceso", "Completado"

    @Column(name = "frecuencia", length = 50)
    private String frecuencia; // "Mensual (Día 9)"

    @Column(name = "responsable", length = 100)
    private String responsable;

    @Column(name = "fecha_limite", length = 20)
    private String fechaLimite; // "2026-10-09"
}
