package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.application.service.DashboardService;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.KPIProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.ProductoStockCriticoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CashSessionRepository cashSessionRepository;
    @Mock private AnalyticsCache cache;

    @InjectMocks private DashboardService dashboardService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void configurarCache() {
        // Hace que el cache ejecute el supplier directamente
        when(cache.compute(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
    }

    // ─── KPIs del día ────────────────────────────────────────────────────────

    @Test
    void getKPIsDelDia_conVentasDelDia_calculaCorrectamente() {
        KPIProjection hoy = mockKPI("150000", 5, "40000", "30000", 10);
        KPIProjection ayer = mockKPI("100000", 3, "25000", "33333.33", 7);
        KPIProjection semPas = mockKPI("120000", 4, "30000", "30000", 8);

        when(saleRepository.findKPIs(any(), any()))
                .thenReturn(hoy)
                .thenReturn(ayer)
                .thenReturn(semPas);
        when(saleRepository.findMejorVendedor(any(), any())).thenReturn(Optional.of("Alejandrina"));

        KPIsDelDiaDTO result = dashboardService.getKPIsDelDia();

        assertNotNull(result);
        assertEquals(new BigDecimal("150000"), result.totalVendido());
        assertEquals(5, result.cantidadVentas());
        assertEquals("Alejandrina", result.mejorVendedor());
        // Porcentaje vs ayer: (150000 - 100000) / 100000 * 100 = 50%
        assertEquals(new BigDecimal("50.00"), result.porcentajeVsAyer());
        // Porcentaje vs semana: (150000 - 120000) / 120000 * 100 = 25%
        assertEquals(new BigDecimal("25.00"), result.porcentajeVsMismoDiaSemanaPasada());
    }

    @Test
    void getKPIsDelDia_sinVentasAyer_retornaCeroPorcentaje() {
        KPIProjection hoy = mockKPI("50000", 2, "10000", "25000", 4);
        KPIProjection vacio = mockKPI("0", 0, "0", "0", 0);

        when(saleRepository.findKPIs(any(), any()))
                .thenReturn(hoy)
                .thenReturn(vacio)
                .thenReturn(vacio);
        when(saleRepository.findMejorVendedor(any(), any())).thenReturn(Optional.of("—"));

        KPIsDelDiaDTO result = dashboardService.getKPIsDelDia();

        assertEquals(BigDecimal.ZERO, result.porcentajeVsAyer());
        assertEquals(BigDecimal.ZERO, result.porcentajeVsMismoDiaSemanaPasada());
    }

    // ─── KPIs rango ──────────────────────────────────────────────────────────

    @Test
    void getKPIsRango_calculaMargenCorrectamente() {
        KPIProjection kpi = mockKPI("200000", 10, "80000", "20000", 30);
        when(saleRepository.findKPIs(any(), any())).thenReturn(kpi);

        KPIsRangoDTO result = dashboardService.getKPIsRango(
                java.time.LocalDate.now().minusDays(29), java.time.LocalDate.now());

        assertNotNull(result);
        assertEquals(new BigDecimal("200000"), result.totalVendido());
        // margen = 80000 / 200000 * 100 = 40.00%
        assertEquals(new BigDecimal("40.00"), result.margenPorcentaje());
    }

    // ─── Stock crítico ────────────────────────────────────────────────────────

    @Test
    void getProductosStockCritico_retornaListaConDiferencia() {
        ProductoStockCriticoProjection proj = mock(ProductoStockCriticoProjection.class);
        when(proj.getProductoId()).thenReturn(1L);
        when(proj.getNombre()).thenReturn("Labial Rojo");
        when(proj.getCodigoProducto()).thenReturn("LIP-001");
        when(proj.getStockActual()).thenReturn(2);
        when(proj.getStockMinimo()).thenReturn(5);

        when(productoRepository.findStockCritico()).thenReturn(List.of(proj));

        List<ProductoStockCriticoDTO> result = dashboardService.getProductosStockCritico();

        assertEquals(1, result.size());
        assertEquals(-3, result.get(0).diferencia()); // 2 - 5 = -3
        assertEquals("Labial Rojo", result.get(0).nombre());
    }

    // ─── Valoración inventario ────────────────────────────────────────────────

    @Test
    void getValoracionInventario_calculaUtilPotencialYMargen() {
        // aCosto=500000, aVenta=800000, totalProd=20, totalUnid=100
        Object[] raw = { "500000", "800000", 20, 100 };
        when(productoRepository.findValoracionRaw()).thenReturn(Collections.singletonList(raw));

        ValoracionInventarioDTO result = dashboardService.getValoracionInventario();

        assertEquals(new BigDecimal("500000"), result.valorACosto());
        assertEquals(new BigDecimal("800000"), result.valorAPrecioVenta());
        // utilidad potencial = 800000 - 500000 = 300000
        assertEquals(new BigDecimal("300000"), result.utilidadPotencial());
        // margen = 300000 / 800000 * 100 = 37.50%
        assertEquals(new BigDecimal("37.50"), result.margenPromedio());
        assertEquals(20, result.totalProductos());
        assertEquals(100, result.totalUnidades());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private KPIProjection mockKPI(String total, int cant, String util, String ticket, int prods) {
        KPIProjection kpi = mock(KPIProjection.class);
        when(kpi.getTotalVendido()).thenReturn(new BigDecimal(total));
        when(kpi.getCantidadVentas()).thenReturn(cant);
        when(kpi.getUtilidadBruta()).thenReturn(new BigDecimal(util));
        when(kpi.getTicketPromedio()).thenReturn(new BigDecimal(ticket));
        when(kpi.getCantidadProductosVendidos()).thenReturn(prods);
        return kpi;
    }
}
