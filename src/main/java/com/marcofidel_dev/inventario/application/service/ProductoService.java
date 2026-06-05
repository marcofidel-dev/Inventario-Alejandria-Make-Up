package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.security.Audited;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        log.debug("Listando todos los productos");
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        log.debug("Listando productos activos");
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        log.debug("Buscando producto por ID: {}", id);
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscar(String search) {
        log.debug("Buscando productos por: {}", search);
        if (search == null || search.trim().isEmpty()) {
            return listarActivos();
        }
        return productoRepository.buscarPorNombreOCodigoProducto(search.trim());
    }

    @Transactional(readOnly = true)
    public List<Producto> filtrarPorTipo(Producto.TipoProducto tipo) {
        log.debug("Filtrando productos por tipo: {}", tipo);
        return productoRepository.findByTipo(tipo);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarConStockBajo() {
        log.debug("Listando productos con stock bajo");
        return productoRepository.findProductosConStockBajo();
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.CREATE, entity = "Producto")
    @Transactional
    public Producto guardar(Producto producto) {
        log.info("Guardando producto: {}", producto.getNombre());

        // Validar código de producto único si está presente
        if (producto.getCodigoProducto() != null && !producto.getCodigoProducto().trim().isEmpty()) {
            Optional<Producto> existente = productoRepository.findByCodigoProducto(producto.getCodigoProducto());
            if (existente.isPresent() && !existente.get().getId().equals(producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con ese código: " + producto.getCodigoProducto());
            }
        }

        return productoRepository.save(producto);
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.DELETE, entity = "Producto")
    @Transactional
    public void eliminar(Long id) {
        log.info("Desactivando producto con ID: {}", id);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void incrementarStock(Long productoId, int cantidad) {
        log.info("Incrementando stock del producto ID: {} en {} unidades", productoId, cantidad);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.incrementarStock(cantidad);
        productoRepository.save(producto);
    }

    @Transactional
    public void decrementarStock(Long productoId, int cantidad) {
        log.info("Decrementando stock del producto ID: {} en {} unidades", productoId, cantidad);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.decrementarStock(cantidad);
        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public boolean validarStockSuficiente(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return producto.tieneStockSuficiente(cantidad);
    }
}

