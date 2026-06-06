package com.marcofidel_dev.inventario.domain.entity;

import com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    // Price snapshot at the moment of sale — never query producto.precioVenta for historical reports
    @NotNull
    @Convert(converter = MoneyConverter.class)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal unitPrice;

    // Cost snapshot for profit margin calculations — key for accurate margin reports
    @NotNull
    @Convert(converter = MoneyConverter.class)
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 0)
    private BigDecimal unitCost;

    @Convert(converter = MoneyConverter.class)
    @Column(precision = 10, scale = 0)
    private BigDecimal subtotal;

    public SaleItem(Producto producto, Integer quantity, BigDecimal unitPrice, BigDecimal unitCost) {
        this.producto = producto;
        this.quantity = quantity;
        this.unitPrice = MoneyCOP.normalize(unitPrice);
        this.unitCost = MoneyCOP.normalize(unitCost);
        this.subtotal = MoneyCOP.multiply(unitPrice, quantity);
    }
}
