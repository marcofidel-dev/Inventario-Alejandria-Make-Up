package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.ReporteVentasServiceImpl;
import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

/**
 * Tests funcionales del módulo Reporte de Ventas.
 * Cubre los flujos no testeados en ReporteVentasServiceTest y AnalisisABCTest:
 *   - generarReporte: KPI null, series diarias, top productos con margen, ticket promedio
 *   - listarVentasFiltradas: incluirAnuladas, filtro método, filtro usuario, resolución nombre
 *   - getDesglosePorMetodo: método único (100%), lista vacía
 *   - getDesglosePorUsuario: caso base, lista vacía
 *   - FiltroReporteDTO: factory methods estesMes / ultimos30Dias
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Reporte de Ventas — tests funcionales")
class ReporteVentasFuncionalTest {

    @Mock private SaleRepository saleRepository;
    @Mock private UserRepository userRepository;
    @Mock private AnalyticsCache cache;

    @InjectMocks private ReporteVentasServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void bypassCache() {
        when(cache.compute(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
    }

    // =========================================================================
    // generarReporte
    // =========================================================================

    @Nested
    @DisplayName("generarReporte")
    class GenerarReporte {

        @Test
        @DisplayName("KPI null del repo → no lanza NPE y retorna ceros")
        void kpiNull_noLanzaNPERetornaCeros() {
            when(saleRepository.findKPIs(any(), any())).thenReturn(null);
            when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
            when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

            ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.totalVendido());
            assertEquals(0, result.cantidadVentas());
            assertEquals(BigDecimal.ZERO, result.margenPorcentaje());
        }

        @Test
        @DisplayName("Serie diaria → ventasDiarias se puebla con fecha, total y utilidad")
        void conSerieDiaria_ventasDiariasPopulada() {
            stubKpiVacio();
            VentaDiariaProjection dia = mockDiaria("2026-06-01", "50000", "15000", 3);
            when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of(dia));
            when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

            ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

            assertEquals(1, result.ventasDiarias().size());
            VentaDiariaDTO dto = result.ventasDiarias().get(0);
            assertEquals(LocalDate.of(2026, 6, 1), dto.fecha());
            assertEquals(new BigDecimal("50000"), dto.totalVendido());
            assertEquals(new BigDecimal("15000"), dto.utilidad());
            assertEquals(3, dto.cantidadVentas());
        }

        @Test
        @DisplayName("Top productos → márgenes calculados correctamente en el DTO")
        void conTopProductos_margenCalculado() {
            stubKpiVacio();
            when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

            TopProductoProjection prod = mockTopProducto(1L, "P001", "Labial Rojo",
                    10, new BigDecimal("100000"), new BigDecimal("40000"));
            when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of(prod));

            ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

            assertEquals(1, result.topProductos().size());
            TopProductoDTO top = result.topProductos().get(0);
            assertEquals("Labial Rojo", top.nombreProducto());
            assertEquals(new BigDecimal("100000"), top.ingresoTotal());
            assertEquals(new BigDecimal("40000"), top.utilidadTotal());
            // margen = 40000 / 100000 * 100 = 40.00%
            assertEquals(new BigDecimal("40.00"), top.margenPorcentaje());
        }

        @Test
        @DisplayName("Ticket promedio del KPI se incluye en el DTO de resultado")
        void ticketPromedio_incluidoEnDTO() {
            KPIProjection kpi = mockKPI("200000", 8, "60000", "25000");
            when(saleRepository.findKPIs(any(), any())).thenReturn(kpi);
            when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
            when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

            ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

            assertEquals(new BigDecimal("25000"), result.ticketPromedio());
        }

        @Test
        @DisplayName("Desglose por usuario se incluye en el DTO de resultado")
        void desgloseUsuario_incluidoEnDTO() {
            stubKpiVacio();
            when(saleRepository.findVentasDiarias(any(), any())).thenReturn(List.of());
            when(saleRepository.findTopProductos(any(), any(), anyInt())).thenReturn(List.of());
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());

            DesgloseUsuarioProjection user = mockDesgloseUsuario(1L, "Ana López", 5,
                    new BigDecimal("100000"), new BigDecimal("20000"));
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of(user));

            ReporteVentasPeriodoDTO result = service.generarReporte(FiltroReporteDTO.ultimos30Dias());

            assertEquals(1, result.desglosePorUsuario().size());
            DesgloseUsuarioDTO dto = result.desglosePorUsuario().get(0);
            assertEquals("Ana López", dto.nombreUsuario());
            assertEquals(5, dto.cantidadVentas());
            assertEquals(new BigDecimal("100000"), dto.totalVendido());
        }
    }

    // =========================================================================
    // listarVentasFiltradas
    // =========================================================================

    @Nested
    @DisplayName("listarVentasFiltradas")
    class ListarVentasFiltradas {

        @Test
        @DisplayName("incluirAnuladas=true → pasa estado null al repositorio")
        void incluirAnuladas_pasaEstadoNullAlRepo() {
            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(userRepository.findAll()).thenReturn(List.of());

            FiltroReporteDTO filtro = new FiltroReporteDTO(
                    LocalDate.now().minusDays(6), LocalDate.now(),
                    null, null, null, null, true);

            service.listarVentasFiltradas(filtro);

            ArgumentCaptor<String> estadoCaptor = ArgumentCaptor.forClass(String.class);
            verify(saleRepository).findFiltradas(any(), any(),
                    estadoCaptor.capture(), any(), any(), any());
            assertNull(estadoCaptor.getValue(), "Con incluirAnuladas=true, estado debe ser null");
        }

        @Test
        @DisplayName("incluirAnuladas=false → pasa 'COMPLETADA' al repositorio")
        void soloCompletadas_pasaEstadoCompletadaAlRepo() {
            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(userRepository.findAll()).thenReturn(List.of());

            FiltroReporteDTO filtro = new FiltroReporteDTO(
                    LocalDate.now().minusDays(6), LocalDate.now(),
                    null, null, null, null, false);

            service.listarVentasFiltradas(filtro);

            ArgumentCaptor<String> estadoCaptor = ArgumentCaptor.forClass(String.class);
            verify(saleRepository).findFiltradas(any(), any(),
                    estadoCaptor.capture(), any(), any(), any());
            assertEquals("COMPLETADA", estadoCaptor.getValue());
        }

        @Test
        @DisplayName("Filtro por método de pago → nombre del enum pasado al repo")
        void filtroMetodoPago_pasaNombreEnumAlRepo() {
            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(userRepository.findAll()).thenReturn(List.of());

            FiltroReporteDTO filtro = new FiltroReporteDTO(
                    LocalDate.now().minusDays(6), LocalDate.now(),
                    null, null, null, PaymentMethod.TRANSFERENCIA, false);

            service.listarVentasFiltradas(filtro);

            ArgumentCaptor<String> metodoCap = ArgumentCaptor.forClass(String.class);
            verify(saleRepository).findFiltradas(any(), any(), any(), any(), any(), metodoCap.capture());
            assertEquals("TRANSFERENCIA", metodoCap.getValue());
        }

        @Test
        @DisplayName("Filtro por userId → se pasa el id al repositorio")
        void filtroUserId_pasaIdAlRepo() {
            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(userRepository.findAll()).thenReturn(List.of());

            FiltroReporteDTO filtro = new FiltroReporteDTO(
                    LocalDate.now().minusDays(6), LocalDate.now(),
                    42L, null, null, null, false);

            service.listarVentasFiltradas(filtro);

            ArgumentCaptor<Long> userIdCap = ArgumentCaptor.forClass(Long.class);
            verify(saleRepository).findFiltradas(any(), any(), any(), userIdCap.capture(), any(), any());
            assertEquals(42L, userIdCap.getValue());
        }

        @Test
        @DisplayName("Venta con cliente → clienteNombre toma el nombre del cliente")
        void ventaConCliente_nombreClienteResuelto() {
            Customer cliente = new Customer();
            cliente.setName("María García");

            Sale venta = crearVenta(5L, "80000", PaymentMethod.EFECTIVO, SaleStatus.COMPLETADA);
            venta.setCustomer(cliente);

            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(venta));
            when(userRepository.findAll()).thenReturn(List.of());

            List<VentaResumenDTO> result = service.listarVentasFiltradas(FiltroReporteDTO.ultimos30Dias());

            assertEquals("María García", result.get(0).clienteNombre());
        }

        @Test
        @DisplayName("Venta sin cliente → clienteNombre es 'Ocasional'")
        void ventaSinCliente_esOcasional() {
            Sale venta = crearVenta(6L, "30000", PaymentMethod.NEQUI, SaleStatus.COMPLETADA);
            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(venta));
            when(userRepository.findAll()).thenReturn(List.of());

            List<VentaResumenDTO> result = service.listarVentasFiltradas(FiltroReporteDTO.ultimos30Dias());

            assertEquals("Ocasional", result.get(0).clienteNombre());
        }

        @Test
        @DisplayName("Nombre de usuario se resuelve desde userRepository (fullName)")
        void nombreUsuario_resueltoDesdUserRepository() {
            User usuario = new User();
            usuario.setId(7L);
            usuario.setUsername("ana");
            usuario.setFullName("Ana López");

            Sale venta = crearVenta(7L, "50000", PaymentMethod.EFECTIVO, SaleStatus.COMPLETADA);
            venta.setUserId(7L);

            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(venta));
            when(userRepository.findAll()).thenReturn(List.of(usuario));

            List<VentaResumenDTO> result = service.listarVentasFiltradas(FiltroReporteDTO.ultimos30Dias());

            assertEquals("Ana López", result.get(0).usuarioNombre());
        }

        @Test
        @DisplayName("Nombre de usuario usa username si fullName es null")
        void nombreUsuario_usaUsernameComoFallback() {
            User usuario = new User();
            usuario.setId(8L);
            usuario.setUsername("colab01");
            usuario.setFullName(null);

            Sale venta = crearVenta(8L, "40000", PaymentMethod.EFECTIVO, SaleStatus.COMPLETADA);
            venta.setUserId(8L);

            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(venta));
            when(userRepository.findAll()).thenReturn(List.of(usuario));

            List<VentaResumenDTO> result = service.listarVentasFiltradas(FiltroReporteDTO.ultimos30Dias());

            assertEquals("colab01", result.get(0).usuarioNombre());
        }

        @Test
        @DisplayName("userId sin entrada en userRepository → nombre genérico 'Usuario N'")
        void usuarioDesconocido_nombreGenerico() {
            Sale venta = crearVenta(9L, "20000", PaymentMethod.EFECTIVO, SaleStatus.COMPLETADA);
            venta.setUserId(99L);

            when(saleRepository.findFiltradas(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(venta));
            when(userRepository.findAll()).thenReturn(List.of());

            List<VentaResumenDTO> result = service.listarVentasFiltradas(FiltroReporteDTO.ultimos30Dias());

            assertEquals("Usuario 99", result.get(0).usuarioNombre());
        }
    }

    // =========================================================================
    // getDesglosePorMetodo
    // =========================================================================

    @Nested
    @DisplayName("getDesglosePorMetodo")
    class DesglosePorMetodo {

        @Test
        @DisplayName("Único método de pago → porcentaje es 100%")
        void unicoMetodo_porcentaje100() {
            DesglosePagoProjection ef = mockDesglosePago("EFECTIVO", "120000", 6);
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of(ef));

            LocalDate hoy = LocalDate.now();
            List<DesglosePagoDTO> result = service.getDesglosePorMetodo(hoy.minusDays(29), hoy);

            assertEquals(1, result.size());
            assertEquals(new BigDecimal("100.00"), result.get(0).porcentaje());
            assertEquals(new BigDecimal("120000"), result.get(0).total());
            assertEquals(6, result.get(0).cantidadVentas());
        }

        @Test
        @DisplayName("Sin ventas → lista vacía sin excepción")
        void sinVentas_listaVacia() {
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of());

            LocalDate hoy = LocalDate.now();
            List<DesglosePagoDTO> result = service.getDesglosePorMetodo(hoy.minusDays(29), hoy);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Tres métodos → porcentajes suman exactamente 100%")
        void tresMetodos_porcentajesSuman100() {
            DesglosePagoProjection ef = mockDesglosePago("EFECTIVO",      "50000", 5);
            DesglosePagoProjection tr = mockDesglosePago("TRANSFERENCIA", "30000", 3);
            DesglosePagoProjection nq = mockDesglosePago("NEQUI",         "20000", 2);
            when(saleRepository.findDesglosePorMetodo(any(), any())).thenReturn(List.of(ef, tr, nq));

            LocalDate hoy = LocalDate.now();
            List<DesglosePagoDTO> result = service.getDesglosePorMetodo(hoy.minusDays(29), hoy);

            BigDecimal suma = result.stream()
                    .map(DesglosePagoDTO::porcentaje)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(new BigDecimal("100.00"), suma);
        }
    }

    // =========================================================================
    // getDesglosePorUsuario
    // =========================================================================

    @Nested
    @DisplayName("getDesglosePorUsuario")
    class DesglosePorUsuario {

        @Test
        @DisplayName("Caso base → DTO contiene id, nombre, ventas, total y ticket")
        void casoBase_DTOCompleto() {
            DesgloseUsuarioProjection proj = mockDesgloseUsuario(
                    3L, "Carlos Ruiz", 8, new BigDecimal("160000"), new BigDecimal("20000"));
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of(proj));

            LocalDate hoy = LocalDate.now();
            List<DesgloseUsuarioDTO> result = service.getDesglosePorUsuario(hoy.minusDays(29), hoy);

            assertEquals(1, result.size());
            DesgloseUsuarioDTO dto = result.get(0);
            assertEquals(3L, dto.userId());
            assertEquals("Carlos Ruiz", dto.nombreUsuario());
            assertEquals(8, dto.cantidadVentas());
            assertEquals(new BigDecimal("160000"), dto.totalVendido());
            assertEquals(new BigDecimal("20000"), dto.ticketPromedio());
        }

        @Test
        @DisplayName("Sin registros → lista vacía sin excepción")
        void sinRegistros_listaVacia() {
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of());

            LocalDate hoy = LocalDate.now();
            List<DesgloseUsuarioDTO> result = service.getDesglosePorUsuario(hoy.minusDays(29), hoy);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Múltiples usuarios → orden preservado del repositorio")
        void multipleUsuarios_ordenPreservado() {
            DesgloseUsuarioProjection u1 = mockDesgloseUsuario(1L, "Top Vendedor",     12, new BigDecimal("240000"), new BigDecimal("20000"));
            DesgloseUsuarioProjection u2 = mockDesgloseUsuario(2L, "Segundo Vendedor",  6, new BigDecimal("90000"),  new BigDecimal("15000"));
            when(saleRepository.findDesglosePorUsuario(any(), any())).thenReturn(List.of(u1, u2));

            LocalDate hoy = LocalDate.now();
            List<DesgloseUsuarioDTO> result = service.getDesglosePorUsuario(hoy.minusDays(29), hoy);

            assertEquals(2, result.size());
            assertEquals("Top Vendedor",    result.get(0).nombreUsuario());
            assertEquals("Segundo Vendedor", result.get(1).nombreUsuario());
        }
    }

    // =========================================================================
    // FiltroReporteDTO factories
    // =========================================================================

    @Nested
    @DisplayName("FiltroReporteDTO — factories")
    class FiltroFactories {

        @Test
        @DisplayName("ultimos30Dias() → desde = hoy - 29 días, hasta = hoy")
        void ultimos30Dias_rangoEsCorrecto() {
            LocalDate hoy = LocalDate.now();
            FiltroReporteDTO f = FiltroReporteDTO.ultimos30Dias();

            assertEquals(hoy.minusDays(29), f.desde());
            assertEquals(hoy, f.hasta());
            assertFalse(f.incluirAnuladas());
        }

        @Test
        @DisplayName("estesMes() → desde = primer día del mes, hasta = hoy")
        void estesMes_rangoEsCorrecto() {
            LocalDate hoy = LocalDate.now();
            FiltroReporteDTO f = FiltroReporteDTO.estesMes();

            assertEquals(hoy.withDayOfMonth(1), f.desde());
            assertEquals(hoy, f.hasta());
            assertNull(f.usuarioId());
            assertNull(f.metodoPago());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void stubKpiVacio() {
        KPIProjection kpi = mockKPI("0", 0, "0", "0");
        when(saleRepository.findKPIs(any(), any())).thenReturn(kpi);
    }

    private KPIProjection mockKPI(String total, int cant, String util, String ticket) {
        KPIProjection kpi = mock(KPIProjection.class);
        when(kpi.getTotalVendido()).thenReturn(new BigDecimal(total));
        when(kpi.getCantidadVentas()).thenReturn(cant);
        when(kpi.getUtilidadBruta()).thenReturn(new BigDecimal(util));
        when(kpi.getTicketPromedio()).thenReturn(new BigDecimal(ticket));
        when(kpi.getCantidadProductosVendidos()).thenReturn(0);
        return kpi;
    }

    private VentaDiariaProjection mockDiaria(String fecha, String total, String util, int cant) {
        VentaDiariaProjection p = mock(VentaDiariaProjection.class);
        when(p.getFecha()).thenReturn(fecha);
        when(p.getTotalVendido()).thenReturn(new BigDecimal(total));
        when(p.getUtilidad()).thenReturn(new BigDecimal(util));
        when(p.getCantidadVentas()).thenReturn(cant);
        return p;
    }

    private TopProductoProjection mockTopProducto(Long id, String codigo, String nombre,
                                                   int unidades, BigDecimal ingreso, BigDecimal utilidad) {
        TopProductoProjection p = mock(TopProductoProjection.class);
        when(p.getProductoId()).thenReturn(id);
        when(p.getCodigoProducto()).thenReturn(codigo);
        when(p.getNombreProducto()).thenReturn(nombre);
        when(p.getUnidadesVendidas()).thenReturn(unidades);
        when(p.getIngresoTotal()).thenReturn(ingreso);
        when(p.getUtilidadTotal()).thenReturn(utilidad);
        return p;
    }

    private DesglosePagoProjection mockDesglosePago(String metodo, String total, int cant) {
        DesglosePagoProjection p = mock(DesglosePagoProjection.class);
        when(p.getMetodoPago()).thenReturn(metodo);
        when(p.getTotal()).thenReturn(new BigDecimal(total));
        when(p.getCantidadVentas()).thenReturn(cant);
        return p;
    }

    private DesgloseUsuarioProjection mockDesgloseUsuario(Long id, String nombre, int ventas,
                                                           BigDecimal total, BigDecimal ticket) {
        DesgloseUsuarioProjection p = mock(DesgloseUsuarioProjection.class);
        when(p.getUserId()).thenReturn(id);
        when(p.getNombreUsuario()).thenReturn(nombre);
        when(p.getCantidadVentas()).thenReturn(ventas);
        when(p.getTotalVendido()).thenReturn(total);
        when(p.getTicketPromedio()).thenReturn(ticket);
        return p;
    }

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
}
