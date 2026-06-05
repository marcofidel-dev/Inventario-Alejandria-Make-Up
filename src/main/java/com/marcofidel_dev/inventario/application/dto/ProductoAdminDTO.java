package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.Producto;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Full product view for ADMIN users — includes cost and margin.
 */
@Data
public class ProductoAdminDTO {

    private Long id;
    private Producto.TipoProducto tipo;
    private String nombre;
    private String marca;
    private String codigoProducto;
    private BigDecimal costo;
    private BigDecimal precioVenta;
    private BigDecimal margen;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean activo;

    public static ProductoAdminDTO from(Producto p) {
        ProductoAdminDTO dto = new ProductoAdminDTO();
        dto.setId(p.getId());
        dto.setTipo(p.getTipo());
        dto.setNombre(p.getNombre());
        dto.setMarca(p.getMarca());
        dto.setCodigoProducto(p.getCodigoProducto());
        dto.setCosto(p.getCosto());
        dto.setPrecioVenta(p.getPrecioVenta());
        dto.setMargen(calcularMargen(p.getCosto(), p.getPrecioVenta()));
        dto.setStockActual(p.getStockActual());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setActivo(p.getActivo());
        return dto;
    }

    private static BigDecimal calcularMargen(BigDecimal costo, BigDecimal precioVenta) {
        if (costo == null || precioVenta == null || costo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precioVenta.subtract(costo)
                .divide(precioVenta, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
