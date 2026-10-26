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
 * Entidad que representa al personal de Marathon Sports autorizado para auditar o escanear.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @Column(name = "id", length = 30)
    private String id; // "user-01"

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre; // "Joseph Aaron Churampi Quispe"

    @Column(name = "usuario", length = 50, nullable = false, unique = true)
    private String usuario; // "jchurampi"

    @Column(name = "clave", length = 100, nullable = false)
    private String clave;

    @Column(name = "rol", length = 50, nullable = false)
    private String rol; // "Jefe de Tienda", "Supervisor de Inventario", "Personal de Tienda"

    @Column(name = "sede", length = 100)
    private String sede; // "Marathon Atocongo"

    @Column(name = "codigo", length = 30)
    private String codigo; // "U21212777"
}
