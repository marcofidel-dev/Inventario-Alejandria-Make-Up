package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.Producto;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Restricted product view for COLABORADOR users.
 * Cost, margin, and supplier data are intentionally absent.
 */
@Data
public class ProductoColaboradorDTO {

    private Long id;
    private Producto.TipoProducto tipo;
    private String nombre;
    private String marca;
    private String codigoProducto;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean activo;

    public static ProductoColaboradorDTO from(Producto p) {
        ProductoColaboradorDTO dto = new ProductoColaboradorDTO();
        dto.setId(p.getId());
        dto.setTipo(p.getTipo());
        dto.setNombre(p.getNombre());
        dto.setMarca(p.getMarca());
        dto.setCodigoProducto(p.getCodigoProducto());
        dto.setPrecioVenta(p.getPrecioVenta());
        dto.setStockActual(p.getStockActual());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setActivo(p.getActivo());
        return dto;
    }
}
