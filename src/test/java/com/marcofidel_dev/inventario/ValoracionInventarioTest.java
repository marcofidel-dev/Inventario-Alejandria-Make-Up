package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.ValoracionInventarioDTO;
import com.marcofidel_dev.inventario.application.service.DashboardService;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValoracionInventarioTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CashSessionRepository cashSessionRepository;
    @Mock private AnalyticsCache cache;

    @InjectMocks private DashboardService dashboardService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void configurarCache() {
        when(cache.compute(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
    }

    @Test
    void getValoracionInventario_calculaUtilPotencialCorrectamente() {
        // Inventario: 50 productos, 200 unidades
        // Valor a costo: 1.000.000, Valor a venta: 1.500.000
        Object[] raw = { "1000000", "1500000", 50, 200 };
        when(productoRepository.findValoracionRaw()).thenReturn(raw);

        ValoracionInventarioDTO dto = dashboardService.getValoracionInventario();

        assertEquals(new BigDecimal("1000000.00"), dto.valorACosto());
        assertEquals(new BigDecimal("1500000.00"), dto.valorAPrecioVenta());
        assertEquals(new BigDecimal("500000.00"),  dto.utilidadPotencial());
        // margen = 500000 / 1500000 * 100 = 33.33%
        assertEquals(new BigDecimal("33.33"), dto.margenPromedio());
        assertEquals(50, dto.totalProductos());
        assertEquals(200, dto.totalUnidades());
    }

    @Test
    void getValoracionInventario_inventarioVacio_retornaCeros() {
        Object[] rawVacio = { null, null, null, null };
        when(productoRepository.findValoracionRaw()).thenReturn(rawVacio);

        ValoracionInventarioDTO dto = dashboardService.getValoracionInventario();

        assertEquals(BigDecimal.ZERO, dto.valorACosto());
        assertEquals(BigDecimal.ZERO, dto.valorAPrecioVenta());
        assertEquals(BigDecimal.ZERO, dto.utilidadPotencial());
        assertEquals(BigDecimal.ZERO, dto.margenPromedio());
    }

    @Test
    void getValoracionInventario_costoIgualAVenta_margenCero() {
        // Costo = Venta → utilidad potencial = 0 → margen = 0
        Object[] raw = { "500000", "500000", 10, 50 };
        when(productoRepository.findValoracionRaw()).thenReturn(raw);

        ValoracionInventarioDTO dto = dashboardService.getValoracionInventario();

        assertEquals(BigDecimal.ZERO, dto.utilidadPotencial().stripTrailingZeros());
        // margen: cuando aCosto == aVenta, potencial=0, potencial/aVenta*100 = 0
        assertEquals(BigDecimal.ZERO, dto.margenPromedio().stripTrailingZeros());
    }

    @Test
    void getValoracionInventario_conProductosYUnidades_retornaCuentasCorrectas() {
        Object[] raw = { "750000", "1200000", 30, 150 };
        when(productoRepository.findValoracionRaw()).thenReturn(raw);

        ValoracionInventarioDTO dto = dashboardService.getValoracionInventario();

        assertEquals(30,  dto.totalProductos());
        assertEquals(150, dto.totalUnidades());
    }
}
