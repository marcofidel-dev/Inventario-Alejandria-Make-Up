package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.ReporteVentasServiceImpl;
import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.AnalisisABCDTO;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.TopProductoProjection;
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
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalisisABCTest {

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

    /**
     * Dataset conocido:
     *   Ingresos = [60, 20, 10, 5, 3, 2] → total 100
     *   Acumulado = [60, 80, 90, 95, 98, 100]
     *   A (≤80%): productos 1 y 2
     *   B (≤95%): productos 3 y 4
     *   C (>95%): productos 5 y 6
     */
    @Test
    void getAnalisisABC_clasificaProductosCorrectamente() {
        List<TopProductoProjection> projs = List.of(
                mockProj(1L, "Producto1", "P001", 60, new BigDecimal("60")),
                mockProj(2L, "Producto2", "P002", 20, new BigDecimal("20")),
                mockProj(3L, "Producto3", "P003", 10, new BigDecimal("10")),
                mockProj(4L, "Producto4", "P004",  5, new BigDecimal("5")),
                mockProj(5L, "Producto5", "P005",  3, new BigDecimal("3")),
                mockProj(6L, "Producto6", "P006",  2, new BigDecimal("2"))
        );

        when(saleRepository.findTopProductos(any(), any(), eq(Integer.MAX_VALUE))).thenReturn(projs);

        AnalisisABCDTO result = service.getAnalisisABC(LocalDate.now().minusDays(29), LocalDate.now());

        assertNotNull(result);
        assertEquals(new BigDecimal("100"), result.totalIngresos());

        assertEquals(2, result.categoriaA().size(), "Categoría A debe tener 2 productos");
        assertEquals(2, result.categoriaB().size(), "Categoría B debe tener 2 productos");
        assertEquals(2, result.categoriaC().size(), "Categoría C debe tener 2 productos");

        assertEquals("Producto1", result.categoriaA().get(0).nombre());
        assertEquals("Producto2", result.categoriaA().get(1).nombre());
        assertEquals("Producto3", result.categoriaB().get(0).nombre());
        assertEquals("Producto4", result.categoriaB().get(1).nombre());
        assertEquals("Producto5", result.categoriaC().get(0).nombre());
        assertEquals("Producto6", result.categoriaC().get(1).nombre());
    }

    @Test
    void getAnalisisABC_unSoloProducto_caeEnCategoriaCPorAcumulado100() {
        // Un único producto → acumulado = 100% → > 95% → categoría C
        List<TopProductoProjection> projs = List.of(
                mockProj(1L, "UnicoProducto", "U001", 100, new BigDecimal("100"))
        );
        when(saleRepository.findTopProductos(any(), any(), eq(Integer.MAX_VALUE))).thenReturn(projs);

        AnalisisABCDTO result = service.getAnalisisABC(LocalDate.now().minusDays(29), LocalDate.now());

        assertEquals(0, result.categoriaA().size());
        assertEquals(0, result.categoriaB().size());
        assertEquals(1, result.categoriaC().size());
    }

    @Test
    void getAnalisisABC_sinVentas_listasTotalesVacias() {
        when(saleRepository.findTopProductos(any(), any(), eq(Integer.MAX_VALUE)))
                .thenReturn(List.of());

        AnalisisABCDTO result = service.getAnalisisABC(LocalDate.now().minusDays(29), LocalDate.now());

        assertTrue(result.categoriaA().isEmpty());
        assertTrue(result.categoriaB().isEmpty());
        assertTrue(result.categoriaC().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalIngresos());
    }

    @Test
    void getAnalisisABC_productosConIngresosIguales_seDistribuyenCorrectamente() {
        // 4 productos con igual ingreso ($25 c/u), total $100
        // Acumulado: 25%, 50%, 75%, 100%
        // A (≤80%): 25, 50, 75 → 3 productos
        // B (≤95%): ninguno
        // C (>95%): 100% → 1 producto
        List<TopProductoProjection> projs = List.of(
                mockProj(1L, "A", "PA", 10, new BigDecimal("25")),
                mockProj(2L, "B", "PB", 10, new BigDecimal("25")),
                mockProj(3L, "C", "PC", 10, new BigDecimal("25")),
                mockProj(4L, "D", "PD", 10, new BigDecimal("25"))
        );
        when(saleRepository.findTopProductos(any(), any(), eq(Integer.MAX_VALUE))).thenReturn(projs);

        AnalisisABCDTO result = service.getAnalisisABC(LocalDate.now().minusDays(29), LocalDate.now());

        assertEquals(3, result.categoriaA().size(), "Productos con acumulado 25/50/75% son A");
        assertEquals(0, result.categoriaB().size(), "Ningún producto cae entre 80-95%");
        assertEquals(1, result.categoriaC().size(), "Producto D con acumulado 100% es C");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private TopProductoProjection mockProj(Long id, String nombre, String codigo,
                                           int unidades, BigDecimal ingreso) {
        TopProductoProjection p = mock(TopProductoProjection.class);
        when(p.getProductoId()).thenReturn(id);
        when(p.getNombreProducto()).thenReturn(nombre);
        when(p.getCodigoProducto()).thenReturn(codigo);
        when(p.getUnidadesVendidas()).thenReturn(unidades);
        when(p.getIngresoTotal()).thenReturn(ingreso);
        when(p.getUtilidadTotal()).thenReturn(ingreso.multiply(new BigDecimal("0.4")));
        return p;
    }
}
