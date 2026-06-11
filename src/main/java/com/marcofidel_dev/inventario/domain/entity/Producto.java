package com.marcofidel_dev.inventario.domain.entity;

import com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProducto tipo;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nombre;

    @Size(max = 100)
    private String marca;

    @Size(max = 50)
    @Column(name = "codigo_producto", unique = true)
    private String codigoProducto;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal costo;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Convert(converter = MoneyConverter.class)
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 0)
    private BigDecimal precioVenta;

    @NotNull
    @Min(0)
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @NotNull
    @Min(0)
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    // Campos opcionales específicos
    @Size(max = 50)
    @Column(name = "tono_color")
    private String tonoColor;

    //@Column(name = "fecha_vencimiento")
    //private LocalDate fechaVencimiento;

    @Size(max = 50)
    private String color;

    @Size(max = 50)
    private String tamanio;

    @Size(max = 100)
    private String material;

    @Size(max = 20)
    private String talla;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de utilidad
    public boolean isStockBajo() {
        return stockActual <= stockMinimo;
    }

    public void incrementarStock(int cantidad) {
        this.stockActual += cantidad;
    }

    public void decrementarStock(int cantidad) {
        if (this.stockActual >= cantidad) {
            this.stockActual -= cantidad;
        } else {
            throw new IllegalStateException("Stock insuficiente. Stock actual: " + stockActual);
        }
    }

    public boolean tieneStockSuficiente(int cantidad) {
        return this.stockActual >= cantidad;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre != null ? nombre : "Sin nombre");

        if (marca != null && !marca.trim().isEmpty()) {
            sb.append(" - ").append(marca);
        }

        if (tipo != null) {
            sb.append(" | ").append(tipo);
        }

        if (codigoProducto != null && !codigoProducto.trim().isEmpty()) {
            sb.append(" | Cód: ").append(codigoProducto);
        }

        if (precioVenta != null) {
            sb.append(" | Precio: ").append(com.marcofidel_dev.inventario.shared.money.MoneyCOP.format(precioVenta));
        }

        if (stockActual != null) {
            sb.append(" | Stock: ").append(stockActual);
        }

        return sb.toString();
    }

    public enum TipoProducto {
        MAQUILLAJE("Maquillaje"),
        BOLSO("Bolso"),
        BISUTERIA("Bisutería"),
        CUIDADO_CORPORAL("Cuidado Corporal"),
        SKINCARE("Skincare");

        private final String label;

        TipoProducto(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}

