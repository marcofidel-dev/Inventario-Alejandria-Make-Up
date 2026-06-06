package com.marcofidel_dev.inventario.ui.model;

import com.marcofidel_dev.inventario.domain.entity.Producto;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;

import java.math.BigDecimal;

/** In-memory cart item. Not persisted — if the app crashes, the cart is intentionally lost. */
public class CarritoItem {

    private final Producto producto;
    private final IntegerProperty cantidad;
    private final ObjectProperty<BigDecimal> precioUnitario;
    private final ObjectBinding<BigDecimal> subtotal;

    public CarritoItem(Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.precioUnitario = new SimpleObjectProperty<>(MoneyCOP.normalize(precioUnitario));

        this.subtotal = new ObjectBinding<>() {
            {
                super.bind(CarritoItem.this.cantidad, CarritoItem.this.precioUnitario);
            }
            @Override
            protected BigDecimal computeValue() {
                return MoneyCOP.multiply(
                        CarritoItem.this.precioUnitario.get(),
                        CarritoItem.this.cantidad.get());
            }
        };
    }

    public Producto getProducto() { return producto; }

    public IntegerProperty cantidadProperty() { return cantidad; }
    public int getCantidad() { return cantidad.get(); }
    public void setCantidad(int v) { cantidad.set(v); }

    public ObjectProperty<BigDecimal> precioUnitarioProperty() { return precioUnitario; }
    public BigDecimal getPrecioUnitario() { return precioUnitario.get(); }

    public ObjectBinding<BigDecimal> subtotalProperty() { return subtotal; }
    public BigDecimal getSubtotal() { return subtotal.get(); }
}
