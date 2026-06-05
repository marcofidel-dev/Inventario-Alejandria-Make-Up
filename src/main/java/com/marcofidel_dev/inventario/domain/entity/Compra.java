package com.marcofidel_dev.inventario.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    private String proveedor;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "total_costo", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalCosto = BigDecimal.ZERO;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CompraItem> items = new ArrayList<>();

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de utilidad
    public void addItem(CompraItem item) {
        items.add(item);
        item.setCompra(this);
        recalcularTotal();
    }

    public void removeItem(CompraItem item) {
        items.remove(item);
        item.setCompra(null);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.totalCosto = items.stream()
                .map(item -> item.getCostoUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(3, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(3, RoundingMode.HALF_UP);
    }
}

