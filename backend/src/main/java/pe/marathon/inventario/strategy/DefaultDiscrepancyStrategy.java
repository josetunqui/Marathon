package pe.marathon.inventario.strategy;

import org.springframework.stereotype.Component;

@Component
public class DefaultDiscrepancyStrategy implements DiscrepancyClassificationStrategy {

    @Override
    public String clasificar(int diferencia) {
        if (diferencia < -1) {
            return "FALTANTE_CRITICO";
        } else if (diferencia == -1) {
            return "FALTANTE_MENOR";
        } else if (diferencia == 0) {
            return "CONFORME";
        } else {
            return "SOBRANTE";
        }
    }

    @Override
    public String obtenerColorBadge(int diferencia) {
        if (diferencia < -1) {
            return "danger"; // Rojo (#DC2626)
        } else if (diferencia == -1) {
            return "warning"; // Naranja / Ámbar (#D97706)
        } else if (diferencia == 0) {
            return "success"; // Verde
        } else {
            return "primary"; // Azul (#2563EB)
        }
    }

    @Override
    public String sugerirCausalDefault(int diferencia) {
        if (diferencia < -1) {
            return "Hurto en Piso de Venta";
        } else if (diferencia == -1) {
            return "Picking Traspapelado";
        } else if (diferencia > 0) {
            return "Error de Recepción";
        }
        return "Stock Conforme";
    }
}
