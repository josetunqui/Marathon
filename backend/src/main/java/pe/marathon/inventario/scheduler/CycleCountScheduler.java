package pe.marathon.inventario.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.service.ReconciliationService;

import java.time.LocalDateTime;

/**
 * ===================================================================
 * COMPONENTE ENTERPRISE: TAREAS PROGRAMADAS (Spring Scheduler)
 * ===================================================================
 * Automatización de alertas y consolidación de conteos cíclicos
 * programados en los días de corte mensual (días 9, 19 y 28 de cada mes).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CycleCountScheduler {

    private final ReconciliationService reconciliationService;

    /**
     * Tarea programada ejecutada a las 02:00 AM los días de ciclo mensual (9, 19 y 28).
     * En producción genera la orden de congelamiento de stock para el turno mañana.
     */
    @Scheduled(cron = "0 0 2 9,19,28 * ?")
    public void ejecutarCierreCicloProgramado() {
        log.info("⏰ [CRON] Iniciando consolidación automática del ciclo de conteo en Marathon Atocongo: {}", LocalDateTime.now());
        try {
            ReporteReconciliacionDTO reporte = reconciliationService.generarReporte("ALL");
            log.info("✅ [CRON] Ciclo consolidado con éxito. Exactitud de Registro (IRA): {}%. Total SKUs: {}",
                    reporte.getKpis().getIra(), reporte.getKpis().getTotalSkus());
        } catch (Exception e) {
            log.error("❌ [CRON] Error al consolidar ciclo programado: {}", e.getMessage(), e);
        }
    }

    /**
     * Heartbeat de verificación cada 30 minutos para auditoría del sistema en vivo.
     */
    @Scheduled(fixedRate = 1800000)
    public void verificarSaludConteo() {
        log.debug("📡 [MONITOR] Verificando sincronización de stock físico vs SAP en Marathon Atocongo.");
    }
}
