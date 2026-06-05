package com.marcofidel_dev.inventario.infrastructure.repository.projection;

public interface ProductoStockCriticoProjection {
    Long getProductoId();
    String getNombre();
    String getCodigoProducto();
    int getStockActual();
    int getStockMinimo();
}
