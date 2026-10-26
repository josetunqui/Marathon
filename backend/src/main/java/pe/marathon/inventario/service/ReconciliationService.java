package pe.marathon.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.marathon.inventario.dao.AuditoriaDAO;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.ItemReconciliacionDTO;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.dto.ResumenKpiDTO;
import pe.marathon.inventario.model.AuditoriaMerma;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.Producto;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ProductoDAO productoDAO;
    private final ConteoFisicoDAO conteoFisicoDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ReporteReconciliacionDTO generarReporte(String linea) {
        // 1. Obtener productos filtrados o todos
        List<Producto> productos;
        if (linea == null || linea.trim().isEmpty() || "ALL".equalsIgnoreCase(linea)) {
            productos = productoDAO.obtenerTodos();
        } else {
            productos = productoDAO.buscarPorLinea(linea);
        }

        // 2. Mapear conteos físicos por SKU
        List<ConteoFisico> conteos = conteoFisicoDAO.obtenerTodos();
        Map<String, ConteoFisico> conteoMap = new HashMap<>();
        for (ConteoFisico cf : conteos) {
            conteoMap.put(cf.getSku().toUpperCase(), cf);
        }

        // 3. Mapear auditorías de merma
        List<AuditoriaMerma> auditorias = auditoriaDAO.obtenerTodas();
        Map<String, AuditoriaMerma> auditMap = new HashMap<>();
        for (AuditoriaMerma aud : auditorias) {
            auditMap.put(aud.getSku().toUpperCase(), aud);
        }

        List<ItemReconciliacionDTO> items = new ArrayList<>();
        int conformes = 0;
        int faltantes = 0;
        int sobrantes = 0;
        int noContados = 0;
        int totalFisico = 0;
        int totalSap = 0;
        double valorSap = 0.0;
        double valorFisico = 0.0;
        double valorNetoDiferencia = 0.0;
        double costoNetoDiferencia = 0.0;

        for (Producto prod : productos) {
            String skuKey = prod.getSku().toUpperCase();
            ConteoFisico cf = conteoMap.get(skuKey);
            AuditoriaMerma aud = auditMap.get(skuKey);

            int sap = prod.getStockSap() != null ? prod.getStockSap() : 0;
            double precio = prod.getPrecioUnitario() != null ? prod.getPrecioUnitario() : 0.0;
            double costo = prod.getCostoUnitario() != null ? prod.getCostoUnitario() : 0.0;

            totalSap += sap;
            valorSap += sap * precio;

            if (cf != null) {
                int fis = cf.getConteoFisico();
                int diff = fis - sap;
                totalFisico += fis;
                valorFisico += fis * precio;

                double valDiff = diff * precio;
                double costDiff = diff * costo;
                valorNetoDiferencia += valDiff;
                costoNetoDiferencia += costDiff;

                String estado;
                String badgeClass;
                if (diff == 0) {
                    estado = "CONFORME";
                    badgeClass = "badge-conforme";
                    conformes++;
                } else if (diff < 0) {
                    estado = "FALTANTE";
                    badgeClass = "badge-faltante";
                    faltantes++;
                } else {
                    estado = "SOBRANTE";
                    badgeClass = "badge-sobrante";
                    sobrantes++;
                }

                boolean validado = (aud != null && Boolean.TRUE.equals(aud.getEstadoAprobado()));

                items.add(ItemReconciliacionDTO.builder()
                        .sku(prod.getSku())
                        .ean(prod.getEan())
                        .descripcion(prod.getDescripcion())
                        .linea(prod.getLinea())
                        .marca(prod.getMarca())
                        .talla(prod.getTalla())
                        .color(prod.getColor())
                        .ubicacion(prod.getUbicacion())
                        .stockSap(sap)
                        .conteoFisico(fis)
                        .tieneConteo(true)
                        .diferencia(diff)
                        .precioUnitario(precio)
                        .costoUnitario(costo)
                        .valorDiferencia(valDiff)
                        .costoDiferencia(costDiff)
                        .estado(estado)
                        .badgeClass(badgeClass)
                        .estaValidado(validado)
                        .auditoria(aud)
                        .build());
            } else {
                noContados++;
                items.add(ItemReconciliacionDTO.builder()
                        .sku(prod.getSku())
                        .ean(prod.getEan())
                        .descripcion(prod.getDescripcion())
                        .linea(prod.getLinea())
                        .marca(prod.getMarca())
                        .talla(prod.getTalla())
                        .color(prod.getColor())
                        .ubicacion(prod.getUbicacion())
                        .stockSap(sap)
                        .conteoFisico(null)
                        .tieneConteo(false)
                        .diferencia(-sap)
                        .precioUnitario(precio)
                        .costoUnitario(costo)
                        .valorDiferencia(-sap * precio)
                        .costoDiferencia(-sap * costo)
                        .estado("NO CONTADO")
                        .badgeClass("badge-no-contado")
                        .estaValidado(false)
                        .auditoria(null)
                        .build());
            }
        }

        int totalItems = productos.size();
        double iraPorcentaje = totalItems > 0 ? ((double) conformes / totalItems) * 100.0 : 0.0;
        iraPorcentaje = Math.round(iraPorcentaje * 10.0) / 10.0;

        String iraClase = iraPorcentaje >= 95 ? "text-success" : (iraPorcentaje >= 85 ? "text-warning" : "text-danger");
        String iraCalificacion = iraPorcentaje >= 95 ? "Óptimo (>= 95%)" : (iraPorcentaje >= 85 ? "Alerta" : "Crítico (< 85%)");

        ResumenKpiDTO kpis = ResumenKpiDTO.builder()
                .totalSkus(totalItems)
                .conformes(conformes)
                .faltantes(faltantes)
                .sobrantes(sobrantes)
                .noContados(noContados)
                .totalSap(totalSap)
                .totalFisico(totalFisico)
                .valorSap(Math.round(valorSap * 100.0) / 100.0)
                .valorFisico(Math.round(valorFisico * 100.0) / 100.0)
                .valorNetoDiferencia(Math.round(valorNetoDiferencia * 100.0) / 100.0)
                .costoNetoDiferencia(Math.round(costoNetoDiferencia * 100.0) / 100.0)
                .ira(iraPorcentaje)
                .iraClase(iraClase)
                .iraCalificacion(iraCalificacion)
                .build();

        return ReporteReconciliacionDTO.builder()
                .fechaGeneracion(LocalDateTime.now())
                .sede("Marathon Sede Atocongo")
                .lineaFiltro(linea != null ? linea : "ALL")
                .kpis(kpis)
                .items(items)
                .build();
    }
}
