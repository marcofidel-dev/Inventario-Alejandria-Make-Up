package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.ReporteVentasServiceImpl;
import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReporteVentasServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private UserRepository userRepository;
    @Mock private AnalyticsCache cache;

    @InjectMocks private ReporteVentasServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void configurarCache() {
        when(cache.compute(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
    }

    // ─── generarReporte ──────────────────────────────────────────────────────

    @Test
    void generarReporte_calculaTotalesYMargen() {
        KPIProjection kpi = mock(KPIProjection.class);
        when(kpi.getTotalVendido()).thenReturn(new BigDecimal("300000"));
        when(kpi.getCantidadVentas()).thenReturn(15);
        when(kpi.getUtilidadBruta()).thenReturn(new BigDecimal("90000"));
        when(kpi.getTicketPromedio()).thenReturn(new BigDecimal("20000"));
        when(kpi.getCantidadProductosVendidos()).thenReturn(45);

        when(saleRepository.findKPIs(any(), any())).thenReturn(kpi);
        when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
        when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
        when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
        when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

        FiltroReporteDTO filtro = FiltroReporteDTO.ultimos30Dias();
        ReporteVentasPeriodoDTO result = service.generarReporte(filtro);

        assertEquals(new BigDecimal("300000.00"), result.totalVendido());
        assertEquals(15, result.cantidadVentas());
        assertEquals(new BigDecimal("90000.00"), result.utilidadTotal());
        // margen = 90000 / 300000 * 100 = 30%
        assertEquals(new BigDecimal("30.00"), result.margenPorcentaje());
    }

    @Test
    void generarReporte_sinVentas_retornaCeros() {
        KPIProjection kpiVacio = mock(KPIProjection.class);
        when(kpiVacio.getTotalVendido()).thenReturn(BigDecimal.ZERO);
        when(kpiVacio.getCantidadVentas()).thenReturn(0);
        when(kpiVacio.getUtilidadBruta()).thenReturn(BigDecimal.ZERO);
        when(kpiVacio.getTicketPromedio()).thenReturn(BigDecimal.ZERO);
        when(kpiVacio.getCantidadProductosVendidos()).thenReturn(0);

        when(saleRepository.findKPIs(any(), any())).thenReturn(kpiVacio);
        when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
        when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
        when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
        when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

        ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

        assertEquals(BigDecimal.ZERO, result.margenPorcentaje());
        assertEquals(0, result.cantidadVentas());
    }

    // ─── getDesglosePorMetodo ────────────────────────────────────────────────

    @Test
    void getDesglosePorMetodo_calculaPorcentajeCorrectoConDosMedios() {
        DesglosePagoProjection efectivo = mock(DesglosePagoProjection.class);
        when(efectivo.getMetodoPago()).thenReturn("EFECTIVO");
        when(efectivo.getTotal()).thenReturn(new BigDecimal("60000"));
        when(efectivo.getCantidadVentas()).thenReturn(3);

        DesglosePagoProjection transferencia = mock(DesglosePagoProjection.class);
        when(transferencia.getMetodoPago()).thenReturn("TRANSFERENCIA");
        when(transferencia.getTotal()).thenReturn(new BigDecimal("40000"));
        when(transferencia.getCantidadVentas()).thenReturn(2);

        when(saleRepository.findDesglosePorMetodo(any(), any()))
                .thenReturn(List.of(efectivo, transferencia));

        LocalDate hoy = LocalDate.now();
        List<DesglosePagoDTO> result = service.getDesglosePorMetodo(hoy.minusDays(29), hoy);

        assertEquals(2, result.size());
        DesglosePagoDTO ef = result.stream().filter(d -> "EFECTIVO".equals(d.metodoPago())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("60.00"), ef.porcentaje()); // 60000/100000*100
        DesglosePagoDTO tr = result.stream().filter(d -> "TRANSFERENCIA".equals(d.metodoPago())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("40.00"), tr.porcentaje());
    }

    // ─── listarVentasFiltradas ───────────────────────────────────────────────

    @Test
    void listarVentasFiltradas_sinFiltroProducto_retornaTodasLasVentas() {
        Sale venta = crearVenta(1L, "100000", PaymentMethod.EFECTIVO, SaleStatus.COMPLETADA);
        when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(venta));
        when(userRepository.findAll()).thenReturn(List.of());

        FiltroReporteDTO filtro = new FiltroReporteDTO(
                LocalDate.now().minusDays(6), LocalDate.now(),
                null, null, null, null, false);

        List<VentaResumenDTO> result = service.listarVentasFiltradas(filtro);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Ocasional", result.get(0).clienteNombre());
    }

    @Test
    void listarVentasFiltradas_conFiltroProducto_soloRetornaVentasConEseProducto() {
        Producto productoA = new Producto();
        productoA.setId(10L);
        Producto productoB = new Producto();
        productoB.setId(20L);

        SaleItem itemA = new SaleItem();
        itemA.setProducto(productoA);
        itemA.setUnitPrice(new BigDecimal("50000"));
        itemA.setUnitCost(new BigDecimal("20000"));
        itemA.setQuantity(1);

        SaleItem itemB = new SaleItem();
        itemB.setProducto(productoB);
        itemB.setUnitPrice(new BigDecimal("30000"));
        itemB.setUnitCost(new BigDecimal("15000"));
        itemB.setQuantity(1);

        Sale ventaConA = crearVentaConItems(1L, List.of(itemA));
        Sale ventaSinA = crearVentaConItems(2L, List.of(itemB));

        when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(ventaConA, ventaSinA));
        when(userRepository.findAll()).thenReturn(List.of());

        FiltroReporteDTO filtro = new FiltroReporteDTO(
                LocalDate.now().minusDays(6), LocalDate.now(),
                null, null, 10L, null, false);

        List<VentaResumenDTO> result = service.listarVentasFiltradas(filtro);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Sale crearVenta(Long id, String total, PaymentMethod metodo, SaleStatus status) {
        Sale s = new Sale();
        s.setId(id);
        s.setTotal(new BigDecimal(total));
        s.setPaymentMethod(metodo);
        s.setStatus(status);
        s.setSaleDate(LocalDateTime.now());
        s.setItems(List.of());
        return s;
    }

    private Sale crearVentaConItems(Long id, List<SaleItem> items) {
        Sale s = new Sale();
        s.setId(id);
        s.setTotal(new BigDecimal("50000"));
        s.setPaymentMethod(PaymentMethod.EFECTIVO);
        s.setStatus(SaleStatus.COMPLETADA);
        s.setSaleDate(LocalDateTime.now());
        s.setItems(items);
        return s;
    }
}
