package com.marcofidel_dev.inventario.domain.entity;

import com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Convert(converter = MoneyConverter.class)
    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 0)
    private BigDecimal costoUnitario;

    // Constructor conveniente
    public CompraItem(Producto producto, Integer cantidad, BigDecimal costoUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }
}

