package pe.marathon.inventario.strategy;

/**
 * Estrategia para clasificar y evaluar la severidad de discrepancias entre SAP y Físico.
 */
public interface DiscrepancyClassificationStrategy {
    String clasificar(int diferencia);
    String obtenerColorBadge(int diferencia);
    String sugerirCausalDefault(int diferencia);
}
