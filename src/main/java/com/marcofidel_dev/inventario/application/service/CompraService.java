package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.Compra;
import com.marcofidel_dev.inventario.domain.entity.CompraItem;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.repository.CompraRepository;
import com.marcofidel_dev.inventario.infrastructure.security.Audited;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
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

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<Compra> listarTodos() {
        log.debug("Listando todas las compras");
        return compraRepository.findByOrderByFechaDesc();
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorId(Long id) {
        log.debug("Buscando compra por ID: {}", id);
        return compraRepository.findById(id);
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<Compra> filtrarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Filtrando compras entre {} y {}", fechaInicio, fechaFin);
        return compraRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.CREATE, entity = "Compra")
    @Transactional
    public Compra guardar(Compra compra) {
        log.info("Guardando compra de proveedor: {}", compra.getProveedor());

        if (compra.getId() == null && compra.getFecha() == null) {
            compra.setFecha(LocalDate.now());
        }

        /*
         * FIX: Hibernate 7 strict detached-entity handling.
         *
         * The Producto objects inside CompraItems are DETACHED entities — they were
         * loaded in a previous transaction (when the form loaded the product list).
         * Passing detached references through a cascade chain causes
         * StaleObjectStateException in Hibernate 7.
         *
         * Solution: re-load each Producto WITHIN this transaction (making them
         * MANAGED), increment the stock on the managed instance, and replace the
         * detached reference on the CompraItem. Hibernate will then flush the
         * stock change atomically when this transaction commits — no separate
         * incrementarStock() call needed.
         */
        for (CompraItem item : compra.getItems()) {
            Long productoId = item.getProducto().getId();
            Producto managed = productoService.buscarPorId(productoId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con ID: " + productoId));

            managed.incrementarStock(item.getCantidad());
            item.setProducto(managed);

            log.info("Stock a incrementar: '{}' +{} (nuevo stock: {})",
                    managed.getNombre(), item.getCantidad(), managed.getStockActual());
        }

        compra.recalcularTotal();
        return compraRepository.save(compra);
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.DELETE, entity = "Compra")
    @Transactional
    public void eliminar(Long id) {
        log.warn("Eliminando compra con ID: {} - ADVERTENCIA: el stock ya fue incrementado", id);
        compraRepository.deleteById(id);
    }
}
