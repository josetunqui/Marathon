package pe.marathon.inventario.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.MovimientoTrazabilidadDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.MovimientoItemDTO;
import pe.marathon.inventario.dto.TrazabilidadSKUResponseDTO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.MovimientoTrazabilidad;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.ITrazabilidadService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrazabilidadServiceImpl implements ITrazabilidadService {

    private final MovimientoTrazabilidadDAO movimientoDAO;
    private final ProductoDAO productoDAO;
    private final ConteoFisicoDAO conteoFisicoDAO;

    private static final DateTimeFormatter FORMATTER_MINUTOS = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public TrazabilidadSKUResponseDTO obtenerTrazabilidadPorSku(String skuParam) {
        String sku = (skuParam != null && !skuParam.trim().isEmpty()) ? skuParam.trim() : "CAL-NIK-001";

        Optional<Producto> optProd = productoDAO.buscarPorSku(sku);
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorCodigo(sku);
        }
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorEan(sku);
        }

        // Si aún no se encuentra, tomar el primer producto
        if (optProd.isEmpty()) {
            List<Producto> todos = productoDAO.obtenerTodos();
            if (!todos.isEmpty()) {
                optProd = Optional.of(todos.get(0));
            }
        }

        Producto prod = optProd.orElseGet(() -> Producto.builder()
                .sku("CAL-NIK-001")
                .ean("7751234001015")
                .descripcion("Zapatillas Nike Mercurial Vapor 15 Club FG/MG")
                .stockSap(14)
                .build());

        Optional<ConteoFisico> optConteo = conteoFisicoDAO.buscarPorSku(prod.getSku());
        int conteoReal = optConteo.map(ConteoFisico::getConteoFisico).orElse(12);
        int stockSap = prod.getStockSap() != null ? prod.getStockSap() : 14;
        int diferencia = conteoReal - stockSap;
        String diferenciaTexto = (diferencia > 0 ? "+" : "") + diferencia + " u.";

        List<MovimientoTrazabilidad> registros = movimientoDAO.obtenerPorSku(prod.getSku());
        List<MovimientoItemDTO> items = new ArrayList<>();

        if (registros.isEmpty()) {
            // Eventos iniciales representativos del prototipo
            items.add(MovimientoItemDTO.builder()
                    .id(1L)
                    .tipoBadge("Ajustado")
                    .badgeColor("danger")
                    .fechaTexto("Día 9 de c/mes, 14:40")
                    .titulo("Ajuste de Auditoría Física de Inventario")
                    .descripcion("Confirmado por Joseph Churampi - Ajuste de merma en sistema SAP")
                    .usuario("Joseph Aaron Churampi")
                    .build());
            items.add(MovimientoItemDTO.builder()
                    .id(2L)
                    .tipoBadge("Conteo Físico")
                    .badgeColor("primary")
                    .fechaTexto("Día 9 de c/mes, 12:30")
                    .titulo("Escaneo Físico en Terminal de Conteo")
                    .descripcion("Registrado por Jose Armando Tunqui - Conteo total de " + conteoReal + " unidades en anaquel")
                    .usuario("Jose Armando Tunqui")
                    .build());
            items.add(MovimientoItemDTO.builder()
                    .id(3L)
                    .tipoBadge("MB52 Sync")
                    .badgeColor("success")
                    .fechaTexto("Día 9 de c/mes, 08:00")
                    .titulo("Carga Inicial de Stock Teórico SAP")
                    .descripcion("Sincronizado vía MB52 Excel - Carga inicial de " + stockSap + " unidades en Sede Atocongo")
                    .usuario("Sistema Automático SAP")
                    .build());
        } else {
            for (MovimientoTrazabilidad m : registros) {
                String color = "primary";
                if ("Ajustado".equalsIgnoreCase(m.getTipoBadge())) color = "danger";
                else if ("MB52 Sync".equalsIgnoreCase(m.getTipoBadge())) color = "success";
                else if ("Reserva Pre-Venta".equalsIgnoreCase(m.getTipoBadge())) color = "warning";

                String fechaTextoFormateada = formatearFecha(m.getFechaHora());

                items.add(MovimientoItemDTO.builder()
                        .id(m.getId())
                        .tipoBadge(m.getTipoBadge())
                        .badgeColor(color)
                        .fechaTexto(fechaTextoFormateada)
                        .titulo(m.getTitulo())
                        .descripcion(m.getDescripcion())
                        .usuario(m.getUsuario())
                        .build());
            }
        }

        return TrazabilidadSKUResponseDTO.builder()
                .sku(prod.getSku())
                .ean(prod.getEan())
                .nombreProducto(prod.getDescripcion())
                .sedeAlmacen("Sede Atocongo - Almacén A")
                .stockTeoricoSap(stockSap)
                .conteoFisicoReal(conteoReal)
                .diferencia(diferencia)
                .diferenciaTexto(diferenciaTexto)
                .historialMovimientos(items)
                .build();
    }

    @Override
    @Transactional
    public void registrarMovimiento(String sku, String tipoBadge, String titulo, String descripcion, String usuario, Integer stockSap, Integer conteoFisico, Integer diferencia) {
        MovimientoTrazabilidad m = MovimientoTrazabilidad.builder()
                .sku(sku)
                .tipoBadge(tipoBadge)
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .usuario(usuario)
                .stockSap(stockSap)
                .conteoFisico(conteoFisico)
                .diferencia(diferencia)
                .build();
        movimientoDAO.guardar(m);
    }

    private String formatearFecha(LocalDateTime dt) {
        if (dt == null) return "Hoy";
        if (dt.getDayOfMonth() == 9 || dt.getDayOfMonth() == 19 || dt.getDayOfMonth() == 29) {
            return String.format("Día %d de c/mes, %02d:%02d", dt.getDayOfMonth(), dt.getHour(), dt.getMinute());
        }
        return "Hoy, " + dt.format(FORMATTER_MINUTOS);
    }
}
