package pe.marathon.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que almacena la configuración activa de la planificación del ciclo de inventario.
 */
@Entity
@Table(name = "configuracion_ciclo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCiclo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "frecuencia", length = 120, nullable = false)
    private String frecuencia; // "3 Conteos Fraccionados al Mes (Días 9, 19, 29)"

    @Column(name = "categorias_muestreo", length = 250, nullable = false)
    private String categoriasMuestreo; // "Calzado Deportivo,Ropa & Textil,Accesorios"

    @Column(name = "regla_muestreo", length = 300, nullable = false)
    private String reglaMuestreo; // "Filtro activo: Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma)."

    @Column(name = "responsable_calzado", length = 100)
    private String responsableCalzado; // "Jose Armando Tunqui"

    @Column(name = "rol_responsable_calzado", length = 100)
    private String rolResponsableCalzado; // "Auditor Principal Calzado"

    @Column(name = "responsable_jefe", length = 100)
    private String responsableJefe; // "Joseph Aaron Churampi"

    @Column(name = "rol_responsable_jefe", length = 100)
    private String rolResponsableJefe; // "Jefe de Inventario"
}
