package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.domain.entity.SaleItem;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaleMapper {

    private final UserRepository userRepository;

    public SaleResumenDTO toResumenDTO(Sale sale) {
        String customerName = sale.getCustomer() != null
                ? sale.getCustomer().getName()
                : "Ocasional";
        return new SaleResumenDTO(
                sale.getId(),
                sale.getSaleDate(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                customerName,
                sale.getItems().size(),
                sale.getStatus()
        );
    }

    public SaleDetalleColaboradorDTO toColaboradorDTO(Sale sale) {
        String customerName = sale.getCustomer() != null
                ? sale.getCustomer().getName()
                : "Ocasional";
        List<SaleItemViewDTO> items = sale.getItems().stream()
                .map(this::toItemViewDTO)
                .toList();
        return new SaleDetalleColaboradorDTO(
                sale.getId(),
                sale.getSaleDate(),
                sale.getSubtotal(),
                sale.getDiscountPercent(),
                sale.getDiscountAmount(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                customerName,
                sale.getNotes(),
                items
        );
    }

    public SaleDetalleAdminDTO toAdminDTO(Sale sale) {
        String customerName = sale.getCustomer() != null
                ? sale.getCustomer().getName()
                : "Ocasional";

        String sellerName = userRepository.findById(sale.getUserId())
                .map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                .orElse("Usuario " + sale.getUserId());

        List<SaleItemAdminViewDTO> items = sale.getItems().stream()
                .map(this::toItemAdminViewDTO)
                .toList();

        BigDecimal totalCost = items.stream()
                .map(i -> i.unitCost().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalProfit = sale.getTotal().subtract(totalCost).setScale(2, RoundingMode.HALF_UP);

        return new SaleDetalleAdminDTO(
                sale.getId(),
                sale.getSaleDate(),
                sale.getSubtotal(),
                sale.getDiscountPercent(),
                sale.getDiscountAmount(),
                sale.getTotal(),
                totalCost,
                totalProfit,
                sale.getPaymentMethod(),
                sale.getStatus(),
                customerName,
                sellerName,
                sale.getVoidReason(),
                sale.getNotes(),
                items
        );
    }

    private SaleItemViewDTO toItemViewDTO(SaleItem item) {
        return new SaleItemViewDTO(
                item.getProducto().getNombre(),
                item.getProducto().getCodigoProducto(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private SaleItemAdminViewDTO toItemAdminViewDTO(SaleItem item) {
        BigDecimal profit = item.getUnitPrice().subtract(item.getUnitCost())
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return new SaleItemAdminViewDTO(
                item.getProducto().getNombre(),
                item.getProducto().getCodigoProducto(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitCost(),
                item.getSubtotal(),
                profit
        );
    }
}
