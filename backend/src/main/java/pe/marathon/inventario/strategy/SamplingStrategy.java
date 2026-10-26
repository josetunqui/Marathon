package pe.marathon.inventario.strategy;

import pe.marathon.inventario.model.Producto;

/**
 * Principio Open/Closed (OCP): Permite extender nuevas reglas de muestreo
 * sin modificar la lógica principal del ciclo de conteo.
 */
public interface SamplingStrategy {
    boolean aplicaMuestreo(Producto producto, String lineasActivas);
    String getNombreEstrategia();
}
