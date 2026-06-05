package com.marcofidel_dev.inventario.infrastructure.repository.projection;

public interface SinRotacionProjection {
    Long getProductoId();
    String getNombre();
    int getStockActual();
    String getUltimaVenta();
}
