package pe.marathon.inventario.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.marathon.inventario.dao.*;
import pe.marathon.inventario.model.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Carga inicial de datos de Marathon Sports - Sede Atocongo.
 * Sincroniza datos idénticos a los prototipos validados por el cliente.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductoDAO productoDAO;
    private final ConteoFisicoDAO conteoDAO;
    private final CicloConteoDAO cicloDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final UsuarioDAO usuarioDAO;
    private final MovimientoTrazabilidadDAO movimientoDAO;
    private final ConfiguracionCicloDAO configuracionCicloDAO;

    @Override
    public void run(String... args) throws Exception {
        inicializarTodo(false);
    }

    public synchronized void inicializarTodo(boolean forzar) {
        if (productoDAO.contar() == 0) {
            cargarProductos();
            cargarConteosIniciales();
            cargarCiclos();
            cargarAuditorias();
            cargarUsuarios();
            cargarTrazabilidadInicial();
            cargarConfiguracionInicial();
            System.out.println("✅ [DATA-LOADER] Base de datos sincronizada con prototipos y 22 productos de Marathon Atocongo.");
        } else if (forzar) {
            cargarProductos();
            cargarConteosIniciales();
            for (AuditoriaMerma a : auditoriaDAO.obtenerTodas()) {
                a.setEstadoAprobado(false);
                auditoriaDAO.guardar(a);
            }
            System.out.println("✅ [DATA-LOADER] Conteos físicos, productos y auditorías pendientes restaurados a valores iniciales.");
        }
    }

    private void cargarProductos() {
        List<Producto> productos = Arrays.asList(
            // --- LÍNEA CALZADO ---
            new Producto("CAL-NIK-001", "7751234001015", "Calzado", "Nike", "Zapatillas Nike Mercurial Vapor 15 Club FG/MG", "41", "Negro / Carmesí", "Atocongo • Anaquel 04", 14, 269.90, 160.00, 6, 7, 1),
            new Producto("CAL-NIK-002", "790123456712", "Calzado", "Nike", "Zapatillas Nike Air Max Excee Running", "42", "Blanco / Azul", "Pasillo A - Estante 03", 10, 389.00, 210.00, 4, 5, 1),
            new Producto("CAL-ADI-003", "7751234001039", "Calzado", "Adidas", "Zapatillas Adidas Predator Accuracy.3 FG", "40.5", "Negro / Blanco / Rosa", "Atocongo • Anaquel 04", 12, 299.90, 180.00, 5, 6, 1),
            new Producto("CAL-ADI-004", "7751234001046", "Calzado", "Adidas", "Zapatillas Adidas Grand Court Base 2.0 Urbano", "42", "Blanco Clásico", "Pasillo B - Estante 01", 20, 199.90, 115.00, 8, 10, 2),
            new Producto("CAL-PUM-005", "7751234001053", "Calzado", "Puma", "Zapatillas Puma Future Match TT Fútbol Césped", "41", "Azul Eléctrico / Naranja", "Pasillo A - Estante 04", 8, 289.90, 170.00, 3, 4, 1),
            new Producto("CAL-PUM-006", "7751234001060", "Calzado", "Puma", "Zapatillas Puma Smash V2 L Casual", "39", "Blanco / Negro", "Pasillo B - Estante 02", 16, 189.90, 110.00, 6, 8, 2),
            new Producto("CAL-UND-007", "7751234001077", "Calzado", "Under Armour", "Zapatillas UA Charged Assert 10 Running", "43", "Gris Grafito", "Pasillo B - Estante 04", 7, 279.90, 165.00, 3, 3, 1),
            new Producto("CAL-MAR-008", "7751234001084", "Calzado", "Marathon", "Zapatillas Marathon Training Pro Flex", "40", "Negro / Rojo", "Pasillo C - Estante 01", 18, 159.90, 85.00, 8, 8, 2),

            // --- LÍNEA ROPA ---
            new Producto("ROP-MAR-101", "7751234002012", "Ropa", "Marathon", "Camiseta Oficial Selección Peruana Local 2026", "M", "Blanco / Rojo", "Isla Central - Perchero 01", 35, 269.90, 140.00, 14, 18, 3),
            new Producto("ROP-MAR-102", "7751234002029", "Ropa", "Marathon", "Camiseta Oficial Selección Peruana Visitante", "L", "Rojo / Dorado", "Isla Central - Perchero 02", 22, 269.90, 140.00, 9, 11, 2),
            new Producto("ROP-MAR-103", "7751234002036", "Ropa", "Marathon", "Camiseta Oficial Universitario 2026", "M", "Crema Tradicional", "Atocongo • Anaquel 04", 20, 249.90, 135.00, 8, 10, 2),
            new Producto("ROP-NIK-104", "7751234002043", "Ropa", "Nike", "Camiseta Alianza Lima Oficial Stadium 2026", "S", "Azul / Blanco", "Pared Textil - Sector Sur", 19, 279.90, 155.00, 8, 9, 2),
            new Producto("ROP-ADI-105", "7751234002050", "Ropa", "Adidas", "Camiseta Sporting Cristal Oficial 2026", "L", "Celeste / Blanco", "Pared Textil - Sector Este", 15, 249.90, 135.00, 6, 8, 1),
            new Producto("ROP-NIK-106", "7751234002067", "Ropa", "Nike", "Pantalón Buzo Nike Dri-FIT Academy", "M", "Negro", "Perchero Textil 04", 24, 179.90, 95.00, 10, 12, 2),
            new Producto("ROP-ADI-107", "7751234002074", "Ropa", "Adidas", "Casaca Cortaviento Adidas Tiro 24", "L", "Azul Marino / Blanco", "Perchero Textil 05", 11, 229.90, 125.00, 4, 6, 1),
            new Producto("ROP-MAR-108", "7751234002081", "Ropa", "Marathon", "Short Deportivo Marathon Entrenamiento Pro", "M", "Negro / Rojo", "Mesa Textil 01", 30, 79.90, 38.00, 12, 15, 3),

            // --- LÍNEA ACCESORIOS ---
            new Producto("ACC-ADI-201", "7751234003019", "Accesorios", "Adidas", "Balón de Fútbol Adidas Club N°5 Oficial", "N°5", "Blanco / Negro / Neón", "Módulo Central Accesorios", 23, 99.90, 52.00, 10, 12, 3),
            new Producto("ACC-PUM-202", "7751234003026", "Accesorios", "Puma", "Balón Puma Orbita LaLiga 1 Training", "N°5", "Amarillo / Multicolor", "Módulo Central Accesorios", 14, 89.90, 46.00, 6, 7, 1),
            new Producto("ACC-NIK-203", "7751234003033", "Accesorios", "Nike", "Mochila Nike Brasilia 9.5 Training (24L)", "Única", "Negro / Logo Blanco", "Pared Accesorios 01", 12, 149.90, 82.00, 5, 6, 1),
            new Producto("ACC-MAR-204", "7751234003040", "Accesorios", "Marathon", "Canilleras Marathon Pro Shield con Tobillera", "M", "Rojo / Negro", "Góndola Accesorios 02", 40, 49.90, 22.00, 16, 20, 4),
            new Producto("ACC-ADI-205", "7751234003057", "Accesorios", "Adidas", "Guantes de Arquero Adidas Predator Training", "9", "Negro / Rojo / Blanco", "Góndola Accesorios 03", 8, 119.90, 65.00, 3, 4, 1),
            new Producto("ACC-NIK-206", "7751234003064", "Accesorios", "Nike", "Pack x3 Pares Medias Deportivas Nike Everyday", "L (39-43)", "Blanco", "Mostrador de Caja", 45, 59.90, 28.00, 20, 20, 5)
        );
        for (Producto p : productos) {
            productoDAO.guardar(p);
        }
    }

    private void cargarConteosIniciales() {
        List<ConteoFisico> conteos = Arrays.asList(
            new ConteoFisico(null, "CAL-NIK-001", 12, LocalDateTime.now().minusHours(4), "Jose Armando Tunqui", "Atocongo • Anaquel 04"),
            new ConteoFisico(null, "CAL-NIK-002", 11, LocalDateTime.now().minusHours(3), "Joseph Aaron Churampi", "Pasillo A - Estante 03"),
            new ConteoFisico(null, "CAL-ADI-003", 11, LocalDateTime.now().minusHours(3), "Jose Armando Tunqui", "Atocongo • Anaquel 04"),
            new ConteoFisico(null, "ROP-MAR-103", 22, LocalDateTime.now().minusHours(2), "Jose Armando Tunqui", "Atocongo • Anaquel 04"),
            new ConteoFisico(null, "ACC-ADI-201", 24, LocalDateTime.now().minusHours(2), "Jose Armando Tunqui", "Módulo Central Accesorios"),
            new ConteoFisico(null, "CAL-ADI-004", 20, LocalDateTime.now().minusHours(2), "Jose Armando Tunqui", "Pasillo B - Estante 01"),
            new ConteoFisico(null, "CAL-PUM-005", 7, LocalDateTime.now().minusHours(2), "Joseph Aaron Churampi", "Pasillo A - Estante 04"),
            new ConteoFisico(null, "CAL-PUM-006", 17, LocalDateTime.now().minusHours(1), "Joseph Aaron Churampi", "Pasillo B - Estante 02")
        );
        for (ConteoFisico c : conteos) {
            conteoDAO.buscarPorSku(c.getSku()).ifPresentOrElse(
                existente -> {
                    existente.setConteoFisico(c.getConteoFisico());
                    existente.setEscaneadoPor(c.getEscaneadoPor());
                    existente.setFechaConteo(c.getFechaConteo());
                    existente.setUbicacion(c.getUbicacion());
                    conteoDAO.guardar(existente);
                },
                () -> conteoDAO.guardar(c)
            );
        }
    }

    private void cargarCiclos() {
        List<CicloConteo> ciclos = Arrays.asList(
            new CicloConteo("ciclo-calzado-09", "Calzado", 9, "Ciclo 1: Calzado Deportivo y Urbano", "Auditoría mensual prioritaria de calzado de alta rotación (Zapatillas fútbol, running y urbanas).", "En Proceso", "3 Conteos Fraccionados al Mes (Días 9, 19, 29)", "Jose Armando Tunqui", "2026-10-09"),
            new CicloConteo("ciclo-ropa-19", "Ropa", 19, "Ciclo 2: Ropa y Textil Deportivo", "Auditoría de camisetas oficiales de clubes/selecciones, polerones, casacas y shorts.", "Pendiente", "3 Conteos Fraccionados al Mes (Días 9, 19, 29)", "Joseph Aaron Churampi", "2026-10-19"),
            new CicloConteo("ciclo-accesorios-28", "Accesorios", 29, "Ciclo 3: Accesorios y Balonería", "Auditoría de balones, canilleras, guantes, medias técnicas, mochilas y gorras.", "Pendiente", "3 Conteos Fraccionados al Mes (Días 9, 19, 29)", "Joseph Aaron Churampi", "2026-10-29")
        );
        for (CicloConteo c : ciclos) {
            cicloDAO.guardar(c);
        }
    }

    private void cargarAuditorias() {
        List<AuditoriaMerma> auditorias = Arrays.asList(
            AuditoriaMerma.builder()
                .sku("CAL-NIK-001")
                .linea("CALZADO")
                .descripcionProducto("Zapatillas Nike Mercurial Vapor 15 Club")
                .ubicacion("Atocongo • Anaquel 04")
                .stockSap(14)
                .conteoFisico(12)
                .diferencia(-2)
                .tipo("Faltante (-2)")
                .causal("Hurto en Piso de Venta")
                .comentario("Caja vacía en góndola principal")
                .validadoPor("Joseph Aaron Churampi")
                .fechaValidacion(LocalDateTime.now().minusHours(2))
                .estadoAprobado(false)
                .build(),
            AuditoriaMerma.builder()
                .sku("CAL-ADI-003")
                .linea("CALZADO")
                .descripcionProducto("Zapatillas Adidas Predator Accuracy.3")
                .ubicacion("Atocongo • Anaquel 04")
                .stockSap(12)
                .conteoFisico(11)
                .diferencia(-1)
                .tipo("Faltante (-1)")
                .causal("Picking Traspapelado")
                .comentario("Ubicado en pasillo secundario")
                .validadoPor("Joseph Aaron Churampi")
                .fechaValidacion(LocalDateTime.now().minusHours(1))
                .estadoAprobado(false)
                .build(),
            AuditoriaMerma.builder()
                .sku("ROP-MAR-103")
                .linea("ROPA")
                .descripcionProducto("Camiseta Oficial Universitario 2026")
                .ubicacion("Atocongo • Anaquel 04")
                .stockSap(20)
                .conteoFisico(22)
                .diferencia(2)
                .tipo("Sobrante (+2)")
                .causal("Error de Recepción")
                .comentario("Doble ingreso en guía de remisión")
                .validadoPor("Joseph Aaron Churampi")
                .fechaValidacion(LocalDateTime.now().minusMinutes(40))
                .estadoAprobado(false)
                .build(),
            AuditoriaMerma.builder()
                .sku("ACC-ADI-201")
                .linea("ACCESORIOS")
                .descripcionProducto("Balón de Fútbol Adidas Club N°5 Oficial")
                .ubicacion("Módulo Central Accesorios")
                .stockSap(23)
                .conteoFisico(24)
                .diferencia(1)
                .tipo("Sobrante (+1)")
                .causal("Sobrante por Devolución de Cliente")
                .comentario("Balón devuelto en caja sin registrar en MB52")
                .validadoPor("Joseph Aaron Churampi")
                .fechaValidacion(LocalDateTime.now().minusMinutes(25))
                .estadoAprobado(false)
                .build()
        );

        for (AuditoriaMerma a : auditorias) {
            auditoriaDAO.guardar(a);
        }
    }

    private void cargarUsuarios() {
        List<Usuario> usuarios = Arrays.asList(
            new Usuario("user-01", "Joseph Aaron Churampi", "jchurampi", "marathon2026", "Jefe de Inventario", "Marathon Atocongo", "U21212777"),
            new Usuario("user-02", "Jose Armando Tunqui", "jtunqui", "marathon2026", "Auditor Principal Calzado", "Marathon Atocongo", "U23331844"),
            new Usuario("user-03", "Asesor Comercial / Vendedor", "asesor", "123456", "Personal de Tienda", "Marathon Atocongo", "OPER-01")
        );
        for (Usuario u : usuarios) {
            usuarioDAO.guardar(u);
        }
    }

    private void cargarTrazabilidadInicial() {
        movimientoDAO.guardar(MovimientoTrazabilidad.builder()
                .sku("CAL-NIK-001")
                .tipoBadge("Ajustado")
                .titulo("Ajuste de Auditoría Física de Inventario")
                .descripcion("Confirmado por Joseph Churampi - Ajuste de merma en sistema SAP")
                .fechaHora(LocalDateTime.now().minusHours(1))
                .usuario("Joseph Aaron Churampi")
                .stockSap(14)
                .conteoFisico(12)
                .diferencia(-2)
                .build());

        movimientoDAO.guardar(MovimientoTrazabilidad.builder()
                .sku("CAL-NIK-001")
                .tipoBadge("Conteo Físico")
                .titulo("Escaneo Físico en Terminal de Conteo")
                .descripcion("Registrado por Jose Armando Tunqui - Conteo total de 12 unidades en anaquel")
                .fechaHora(LocalDateTime.now().minusHours(3))
                .usuario("Jose Armando Tunqui")
                .stockSap(14)
                .conteoFisico(12)
                .diferencia(-2)
                .build());

        movimientoDAO.guardar(MovimientoTrazabilidad.builder()
                .sku("CAL-NIK-001")
                .tipoBadge("MB52 Sync")
                .titulo("Carga Inicial de Stock Teórico SAP")
                .descripcion("Sincronizado vía MB52 Excel - Carga inicial de 14 unidades en Sede Atocongo")
                .fechaHora(LocalDateTime.now().minusHours(7))
                .usuario("Sistema Automático SAP")
                .stockSap(14)
                .conteoFisico(0)
                .diferencia(-14)
                .build());
    }

    private void cargarConfiguracionInicial() {
        ConfiguracionCiclo config = ConfiguracionCiclo.builder()
                .frecuencia("3 Conteos Fraccionados al Mes (Días 9, 19, 29)")
                .categoriasMuestreo("Calzado Deportivo,Ropa & Textil,Accesorios")
                .reglaMuestreo("Filtro activo: Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma).")
                .responsableCalzado("Jose Armando Tunqui")
                .rolResponsableCalzado("Auditor Principal Calzado")
                .responsableJefe("Joseph Aaron Churampi")
                .rolResponsableJefe("Jefe de Inventario")
                .build();
        configuracionCicloDAO.guardar(config);
    }
}
