package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.MovimientoTrazabilidadDAO;
import pe.marathon.inventario.model.MovimientoTrazabilidad;
import pe.marathon.inventario.repository.MovimientoTrazabilidadRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovimientoTrazabilidadDAOImpl implements MovimientoTrazabilidadDAO {

    private final MovimientoTrazabilidadRepository movimientoRepository;

    @Override
    public List<MovimientoTrazabilidad> obtenerTodos() {
        return movimientoRepository.findAllByOrderByFechaHoraDesc();
    }

    @Override
    public List<MovimientoTrazabilidad> obtenerPorSku(String sku) {
        return movimientoRepository.findBySkuOrderByFechaHoraDesc(sku);
    }

    @Override
    public MovimientoTrazabilidad guardar(MovimientoTrazabilidad movimiento) {
        return movimientoRepository.save(movimiento);
    }
}
