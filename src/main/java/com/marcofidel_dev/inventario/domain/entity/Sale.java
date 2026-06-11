package com.marcofidel_dev.inventario.domain.entity;

import com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cash_session_id", nullable = false)
    private Long cashSessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    @NotNull
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @NotNull
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal subtotal;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "discount_amount", precision = 10, scale = 0)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal total;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SaleStatus status;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "voided_by_user_id")
    private Long voidedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "virtual_sale", columnDefinition = "boolean not null default false")
    private boolean virtualSale = false;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SaleItem> items = new ArrayList<>();

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }
}
