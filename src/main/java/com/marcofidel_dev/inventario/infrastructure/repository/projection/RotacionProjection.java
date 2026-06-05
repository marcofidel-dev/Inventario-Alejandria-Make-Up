package com.marcofidel_dev.inventario.infrastructure.repository.projection;

public interface RotacionProjection {
    Long getProductoId();
    String getNombre();
    int getStockActual();
    int getUnidadesVendidas();
}
