package com.marcofidel_dev.inventario.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "opening_date", nullable = false)
    private LocalDateTime openingDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "initial_cash", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialCash;

    @Column(name = "opening_notes")
    private String openingNotes;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "declared_cash", precision = 10, scale = 2)
    private BigDecimal declaredCash;

    @Column(name = "expected_cash", precision = 10, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "cash_difference", precision = 10, scale = 2)
    private BigDecimal cashDifference;

    @Column(name = "closing_notes")
    private String closingNotes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CashSessionStatus status;
}
