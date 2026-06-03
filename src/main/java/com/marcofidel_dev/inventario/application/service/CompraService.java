package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.domain.entity.Compra;
import com.marcofidel_dev.inventario.domain.entity.CompraItem;
import com.marcofidel_dev.inventario.infrastructure.repository.CompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoService productoService;

    @Transactional(readOnly = true)
    public List<Compra> listarTodos() {
        log.debug("Listando todas las compras");
        return compraRepository.findByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorId(Long id) {
        log.debug("Buscando compra por ID: {}", id);
        return compraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Compra> filtrarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Filtrando compras entre {} y {}", fechaInicio, fechaFin);
        return compraRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Transactional
    public Compra guardar(Compra compra) {
        log.info("Guardando compra de proveedor: {}", compra.getProveedor());

        // Si es una compra nueva, establecer fecha actual si no tiene
        if (compra.getId() == null && compra.getFecha() == null) {
            compra.setFecha(LocalDate.now());
        }

        // Recalcular total
        compra.recalcularTotal();

        // Guardar la compra
        Compra compraSaved = compraRepository.save(compra);

        // Incrementar stock de los productos
        for (CompraItem item : compraSaved.getItems()) {
            productoService.incrementarStock(item.getProducto().getId(), item.getCantidad());
            log.info("Stock incrementado: Producto ID {}, cantidad: {}",
                item.getProducto().getId(), item.getCantidad());
        }

        return compraSaved;
    }

    @Transactional
    public void eliminar(Long id) {
        log.warn("Eliminando compra con ID: {} - ADVERTENCIA: el stock ya fue incrementado", id);
        compraRepository.deleteById(id);
    }
}

