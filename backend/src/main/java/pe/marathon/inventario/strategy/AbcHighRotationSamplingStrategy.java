package pe.marathon.inventario.strategy;

import org.springframework.stereotype.Component;
import pe.marathon.inventario.model.Producto;

import java.util.Arrays;
import java.util.List;

@Component
public class AbcHighRotationSamplingStrategy implements SamplingStrategy {

    private static final List<String> TOP_MARCAS = Arrays.asList("nike", "adidas", "puma", "marathon");

    @Override
    public boolean aplicaMuestreo(Producto producto, String lineasActivas) {
        if (producto == null) return false;
        
        // Verifica si la línea está dentro de las categorías activas
        if (lineasActivas != null && !lineasActivas.isEmpty()) {
            boolean lineaValida = Arrays.stream(lineasActivas.split(","))
                    .map(String::trim)
                    .anyMatch(l -> l.equalsIgnoreCase(producto.getLinea()) || producto.getLinea().toLowerCase().contains(l.toLowerCase()));
            if (!lineaValida) return false;
        }

        // Filtro ABC: Marcas top o productos de alta rotación
        String marca = producto.getMarca() != null ? producto.getMarca().toLowerCase() : "";
        return TOP_MARCAS.contains(marca) || (producto.getStockSap() != null && producto.getStockSap() >= 10);
    }

    @Override
    public String getNombreEstrategia() {
        return "Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma, Marathon)";
    }
}
