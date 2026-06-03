package com.marcofidel_dev.inventario.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcofidel_dev.inventario.domain.entity.Producto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoService productoService;

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosDashboard() {
        Map<String, Object> datos = new HashMap<>();

        // Contadores
        long totalProductos = productoService.listarActivos().size();
        List<Producto> productosStockBajo = productoService.listarConStockBajo();

        datos.put("totalProductos", totalProductos);
        datos.put("pedidosPendientes", 0);
        datos.put("productosStockBajo", productosStockBajo.size());

        // Listas
        datos.put("listaProductosStockBajo", productosStockBajo);
        datos.put("listaPedidosPendientes", List.of());

        return datos;
    }
}

