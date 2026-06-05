package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.analytics.event.VentaRegistradaEvent;
import com.marcofidel_dev.inventario.application.dto.RegistrarVentaDTO;
import com.marcofidel_dev.inventario.application.dto.SaleItemInputDTO;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.domain.exception.DescuentoExcedidoException;
import com.marcofidel_dev.inventario.domain.exception.SinCajaAbiertaException;
import com.marcofidel_dev.inventario.domain.exception.StockInsuficienteException;
import com.marcofidel_dev.inventario.domain.exception.VentaSinItemsException;
import com.marcofidel_dev.inventario.infrastructure.repository.CustomerRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.security.Audited;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductoRepository productoRepository;
    private final CustomerRepository customerRepository;
    private final CashSessionService cashSessionService;
    private final SessionContext sessionContext;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${pos.descuento.max.colaborador:10}")
    private BigDecimal maxDescColaborador;

    @Transactional
    public Sale registrarVenta(RegistrarVentaDTO dto) {
        // 1. Active cash session required
        CashSession sesion = cashSessionService.getSesionActiva()
                .orElseThrow(SinCajaAbiertaException::new);

        // 2. At least one item required
        if (dto.items() == null || dto.items().isEmpty()) {
            throw new VentaSinItemsException();
        }

        // 3. Validate stock and load managed product instances within this transaction
        List<Producto> productos = new ArrayList<>();
        for (SaleItemInputDTO itemDto : dto.items()) {
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con ID: " + itemDto.productoId()));
            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El producto no está disponible: " + producto.getNombre());
            }
            if (producto.getStockActual() < itemDto.quantity()) {
                throw new StockInsuficienteException(producto.getNombre(), producto.getStockActual());
            }
            productos.add(producto);
        }

        // 4. Discount limit for COLABORADOR
        BigDecimal descuento = dto.discountPercent() != null
                ? dto.discountPercent().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (sessionContext.hasRole(Role.COLABORADOR) && descuento.compareTo(maxDescColaborador) > 0) {
            throw new DescuentoExcedidoException(maxDescColaborador);
        }

        // 5. Build Sale entity
        User user = sessionContext.getCurrentUser().orElseThrow();

        Sale sale = new Sale();
        sale.setCashSessionId(sesion.getId());
        sale.setUserId(user.getId());
        sale.setSaleDate(LocalDateTime.now());
        sale.setPaymentMethod(dto.paymentMethod());
        sale.setStatus(SaleStatus.COMPLETADA);
        sale.setDiscountPercent(descuento);
        sale.setNotes(dto.notes());

        if (dto.customerId() != null) {
            Customer customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Cliente no encontrado con ID: " + dto.customerId()));
            sale.setCustomer(customer);
        }

        // 6. Create items with price/cost snapshots and decrement stock atomically
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < dto.items().size(); i++) {
            SaleItemInputDTO itemDto = dto.items().get(i);
            Producto producto = productos.get(i);

            // Use manually provided price if present, otherwise snapshot current price
            BigDecimal unitPrice = itemDto.unitPrice() != null
                    ? itemDto.unitPrice().setScale(2, RoundingMode.HALF_UP)
                    : producto.getPrecioVenta().setScale(2, RoundingMode.HALF_UP);
            BigDecimal unitCost = producto.getCosto().setScale(2, RoundingMode.HALF_UP);

            SaleItem item = new SaleItem(producto, itemDto.quantity(), unitPrice, unitCost);
            sale.addItem(item);
            subtotal = subtotal.add(item.getSubtotal());

            // Decrement stock — producto is managed within this transaction, flushed on commit
            producto.decrementarStock(itemDto.quantity());
        }

        // 7. Calculate totals
        BigDecimal discountAmount = subtotal
                .multiply(descuento)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        sale.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setDiscountAmount(discountAmount);
        sale.setTotal(subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP));

        Sale saved = saleRepository.save(sale);

        // 8. Rich audit entry with sale details (spec requirement)
        auditService.log(user.getId(), AuditAction.SALE, "Sale",
                saved.getId().toString(), buildSaleAuditDetails(saved));

        log.info("Venta registrada: id={} total={} metodo={} usuario={}",
                saved.getId(), saved.getTotal(), saved.getPaymentMethod(), user.getUsername());

        eventPublisher.publishEvent(new VentaRegistradaEvent(this, saved));
        return saved;
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.VOID_SALE, entity = "Sale")
    @Transactional
    public Sale anularVenta(Long ventaId, String motivo) {
        Sale sale = saleRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + ventaId));

        if (sale.getStatus() == SaleStatus.ANULADA) {
            throw new IllegalStateException("La venta ya está anulada");
        }

        User user = sessionContext.getCurrentUser().orElseThrow();

        // Revert stock — re-fetch managed instances within this transaction (same pattern as CompraService)
        for (SaleItem item : sale.getItems()) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado al revertir stock: " + item.getProducto().getId()));
            producto.incrementarStock(item.getQuantity());
        }

        sale.setStatus(SaleStatus.ANULADA);
        sale.setVoidReason(motivo);
        sale.setVoidedByUserId(user.getId());

        log.info("Venta anulada: id={} motivo='{}' por usuario={}", ventaId, motivo, user.getUsername());
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<Sale> listarVentasMiSesion() {
        return cashSessionService.getSesionActiva()
                .map(s -> saleRepository.findByCashSessionIdOrderBySaleDateDesc(s.getId()))
                .orElse(List.of());
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<Sale> listarVentasPorRango(LocalDate desde, LocalDate hasta) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
    }

    @Transactional(readOnly = true)
    public Optional<Sale> getVentaDetalle(Long id) {
        return saleRepository.findById(id);
    }

    private String buildSaleAuditDetails(Sale sale) {
        String items = sale.getItems().stream()
                .map(i -> String.format("{\"sku\":\"%s\",\"cant\":%d,\"precio\":%s}",
                        i.getProducto().getCodigoProducto() != null
                                ? i.getProducto().getCodigoProducto()
                                : i.getProducto().getNombre(),
                        i.getQuantity(),
                        i.getUnitPrice().toPlainString()))
                .collect(Collectors.joining(","));
        return String.format("{\"ventaId\":%d,\"total\":%s,\"metodoPago\":\"%s\",\"items\":[%s]}",
                sale.getId(), sale.getTotal().toPlainString(), sale.getPaymentMethod(), items);
    }
}
