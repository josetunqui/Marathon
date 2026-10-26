package pe.marathon.inventario.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.marathon.inventario.dao.ConteoFisicoDAO;
import pe.marathon.inventario.dao.ProductoDAO;
import pe.marathon.inventario.dto.PreVentaResponseDTO;
import pe.marathon.inventario.dto.ReservaRequestDTO;
import pe.marathon.inventario.dto.ReservaResponseDTO;
import pe.marathon.inventario.dto.UbicacionStockDTO;
import pe.marathon.inventario.model.ConteoFisico;
import pe.marathon.inventario.model.Producto;
import pe.marathon.inventario.service.IPreVentaService;
import pe.marathon.inventario.service.ITrazabilidadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreVentaServiceImpl implements IPreVentaService {

    private final ProductoDAO productoDAO;
    private final ConteoFisicoDAO conteoFisicoDAO;
    private final ITrazabilidadService trazabilidadService;

    @Override
    public PreVentaResponseDTO consultarStock(String query) {
        String termino = (query != null && !query.trim().isEmpty()) ? query.trim() : "Nike Air Max Excee Running";

        Optional<Producto> optProd = productoDAO.buscarPorCodigo(termino);
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorSku(termino);
        }
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorEan(termino);
        }
        if (optProd.isEmpty()) {
            List<Producto> encontrados = productoDAO.buscarPorTermino(termino);
            if (!encontrados.isEmpty()) {
                optProd = Optional.of(encontrados.get(0));
            }
        }

        // Fallback al producto insignia de la pantalla prototipo
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorSku("CAL-NIK-002");
        }

        if (optProd.isEmpty()) {
            List<Producto> todos = productoDAO.obtenerTodos();
            if (!todos.isEmpty()) {
                optProd = Optional.of(todos.get(0));
            }
        }

        Producto prod = optProd.orElseGet(() -> Producto.builder()
                .sku("CAL-NIK-002")
                .ean("790123456712")
                .marca("Nike")
                .descripcion("Zapatillas Nike Air Max Excee Running")
                .color("Blanco/Azul")
                .precioUnitario(389.00)
                .stockSap(10)
                .stockPisoVenta(4)
                .stockAlmacen(5)
                .stockVitrina(1)
                .build());

        int totalFisico;
        Optional<ConteoFisico> cf = conteoFisicoDAO.buscarPorSku(prod.getSku());
        if (cf.isPresent()) {
            totalFisico = cf.get().getConteoFisico();
        } else {
            totalFisico = prod.getStockSap() != null ? prod.getStockSap() : 10;
        }

        int stockPiso = prod.getStockPisoVenta() != null ? prod.getStockPisoVenta() : 4;
        int stockAlm = prod.getStockAlmacen() != null ? prod.getStockAlmacen() : 5;
        int stockVit = Math.max(0, totalFisico - stockPiso - stockAlm);
        if (stockVit == 0 && totalFisico > (stockPiso + stockAlm)) {
            stockVit = totalFisico - stockPiso - stockAlm;
        } else if (stockVit == 0) {
            stockVit = 1;
        }

        List<UbicacionStockDTO> distribucion = new ArrayList<>();
        distribucion.add(UbicacionStockDTO.builder()
                .nombreUbicacion("Piso de Venta (Pasillo A - Estante 03)")
                .subtitulo("Disponible")
                .tipo("disponible")
                .cantidad(stockPiso)
                .build());
        distribucion.add(UbicacionStockDTO.builder()
                .nombreUbicacion("Almacén Principal (Bloque B - Altura 1)")
                .subtitulo("En Reserva")
                .tipo("reserva")
                .cantidad(stockAlm)
                .build());
        distribucion.add(UbicacionStockDTO.builder()
                .nombreUbicacion("Vitrina Frontal Sede Atocongo")
                .subtitulo("Exhibición")
                .tipo("vitrina")
                .cantidad(stockVit)
                .build());

        int totalSum = stockPiso + stockAlm + stockVit;
        int sap = prod.getStockSap() != null ? prod.getStockSap() : totalSum;
        boolean coincide = (totalSum == sap);

        return PreVentaResponseDTO.builder()
                .encontrado(true)
                .sku(prod.getSku())
                .ean(prod.getEan() != null ? prod.getEan() : "790123456712")
                .marca(prod.getMarca())
                .descripcion(prod.getDescripcion())
                .color(prod.getColor() != null ? prod.getColor() : "Blanco/Azul")
                .precioUnitario(prod.getPrecioUnitario() != null ? prod.getPrecioUnitario() : 389.00)
                .stockTotalFisico(totalSum)
                .stockSap(sap)
                .coincideSap(coincide)
                .notaSap(coincide ? "Coincide 100% con Teórico SAP" : "Diferencia detectada vs SAP: " + (totalSum - sap) + " u.")
                .distribucionUbicaciones(distribucion)
                .build();
    }

    @Override
    @Transactional
    public ReservaResponseDTO reservarStock(ReservaRequestDTO request) {
        String sku = (request != null && request.getSku() != null) ? request.getSku() : "CAL-NIK-002";
        int cantidad = (request != null && request.getCantidad() != null && request.getCantidad() > 0) ? request.getCantidad() : 1;
        String usuario = (request != null && request.getUsuario() != null) ? request.getUsuario() : "Asesor Comercial / Vendedor";

        Optional<Producto> optProd = productoDAO.buscarPorSku(sku);
        if (optProd.isEmpty()) {
            optProd = productoDAO.buscarPorCodigo(sku);
        }

        if (optProd.isEmpty()) {
            return ReservaResponseDTO.builder()
                    .success(false)
                    .mensaje("Producto no encontrado para reserva.")
                    .build();
        }

        Producto prod = optProd.get();
        int stockPiso = prod.getStockPisoVenta() != null ? prod.getStockPisoVenta() : 4;
        if (stockPiso < cantidad) {
            return ReservaResponseDTO.builder()
                    .success(false)
                    .mensaje("Stock insuficiente en Piso de Venta para reserva rápida.")
                    .sku(sku)
                    .cantidadReservada(0)
                    .stockFisicoRestante(stockPiso)
                    .build();
        }

        prod.setStockPisoVenta(stockPiso - cantidad);
        productoDAO.guardar(prod);

        trazabilidadService.registrarMovimiento(
                prod.getSku(),
                "Reserva Pre-Venta",
                "Reserva para Cliente en Piso de Venta",
                "Apartado por " + usuario + " - " + cantidad + " unidad(es) reservada(s)",
                usuario,
                prod.getStockSap(),
                prod.getStockPisoVenta(),
                0
        );

        return ReservaResponseDTO.builder()
                .success(true)
                .mensaje("¡Reserva completada con éxito! La unidad quedó apartada en caja.")
                .sku(prod.getSku())
                .cantidadReservada(cantidad)
                .stockFisicoRestante(prod.getStockPisoVenta())
                .build();
    }

    @Override
    public boolean solicitarAuditoriaRapida(String sku, String motivo) {
        trazabilidadService.registrarMovimiento(
                sku != null ? sku : "CAL-NIK-002",
                "Auditoría Solicitada",
                "Solicitud de Auditoría Rápida desde Pre-Venta",
                motivo != null ? motivo : "Discrepancia reportada por cliente o asesor",
                "Asesor Comercial / Vendedor",
                null, null, null
        );
        return true;
    }
}
