package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.dto.RegistrarVentaDTO;
import com.marcofidel_dev.inventario.application.dto.SaleItemInputDTO;
import com.marcofidel_dev.inventario.application.service.AuditService;
import com.marcofidel_dev.inventario.application.service.CashSessionService;
import com.marcofidel_dev.inventario.application.service.SaleService;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.domain.exception.DescuentoExcedidoException;
import com.marcofidel_dev.inventario.domain.exception.SinCajaAbiertaException;
import com.marcofidel_dev.inventario.domain.exception.StockInsuficienteException;
import com.marcofidel_dev.inventario.domain.exception.VentaSinItemsException;
import com.marcofidel_dev.inventario.infrastructure.repository.CustomerRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CashSessionService cashSessionService;
    @Mock private SessionContext sessionContext;
    @Mock private AuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private SaleService saleService;

    private User colaborador;
    private User admin;
    private Producto producto;
    private CashSession sesionActiva;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(saleService, "maxDescColaborador", new BigDecimal("10"));

        colaborador = User.builder()
                .id(2L).username("alejandrina").role(Role.COLABORADOR).active(true).build();

        admin = User.builder()
                .id(1L).username("admin").role(Role.ADMIN).active(true).build();

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Labial Rojo Pasión");
        producto.setCodigoProducto("LIP-001");
        producto.setPrecioVenta(new BigDecimal("15000.00"));
        producto.setCosto(new BigDecimal("8000.00"));
        producto.setStockActual(5);
        producto.setStockMinimo(1);
        producto.setActivo(true);
        producto.setTipo(Producto.TipoProducto.MAQUILLAJE);

        sesionActiva = new CashSession();
        sesionActiva.setId(1L);
        sesionActiva.setUserId(2L);
        sesionActiva.setInitialCash(new BigDecimal("100000.00"));
        sesionActiva.setStatus(CashSessionStatus.ABIERTA);
    }

    // ─── Venta exitosa ──────────────────────────────────────────────────

    @Test
    void registrarVenta_conDatosValidos_registraYDescontaStock() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));
        when(sessionContext.hasRole(Role.COLABORADOR)).thenReturn(true);

        Sale salePersistida = new Sale();
        salePersistida.setId(1L);
        salePersistida.setTotal(new BigDecimal("30000.00"));
        salePersistida.setPaymentMethod(PaymentMethod.EFECTIVO);
        salePersistida.setSubtotal(new BigDecimal("30000.00"));
        salePersistida.setDiscountAmount(BigDecimal.ZERO);
        salePersistida.setStatus(SaleStatus.COMPLETADA);
        when(saleRepository.save(any(Sale.class))).thenReturn(salePersistida);

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 2, null)),
                null,
                PaymentMethod.EFECTIVO,
                BigDecimal.ZERO,
                null
        );

        Sale result = saleService.registrarVenta(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("30000.00"), result.getTotal());
        assertEquals(3, producto.getStockActual()); // 5 - 2 = 3
        verify(saleRepository).save(any(Sale.class));
        verify(auditService).log(any(), eq(AuditAction.SALE), anyString(), anyString(), anyString());
    }

    // ─── Sin caja abierta ────────────────────────────────────────────────

    @Test
    void registrarVenta_sinCajaAbierta_lanzaSinCajaAbiertaException() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.empty());

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 1, null)),
                null, PaymentMethod.EFECTIVO, BigDecimal.ZERO, null
        );

        assertThrows(SinCajaAbiertaException.class,
                () -> saleService.registrarVenta(dto));

        verify(saleRepository, never()).save(any());
    }

    // ─── Carrito vacío ───────────────────────────────────────────────────

    @Test
    void registrarVenta_sinItems_lanzaVentaSinItemsException() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(), null, PaymentMethod.EFECTIVO, BigDecimal.ZERO, null
        );

        assertThrows(VentaSinItemsException.class,
                () -> saleService.registrarVenta(dto));
    }

    // ─── Stock insuficiente ──────────────────────────────────────────────

    @Test
    void registrarVenta_stockInsuficiente_lanzaStockInsuficienteException() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 10, null)), // 10 > stock(5)
                null, PaymentMethod.EFECTIVO, BigDecimal.ZERO, null
        );

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> saleService.registrarVenta(dto));

        assertTrue(ex.getMessage().contains("Labial Rojo Pasión"));
        assertEquals(5, producto.getStockActual()); // stock no se tocó
    }

    // ─── Descuento excedido (COLABORADOR) ────────────────────────────────

    @Test
    void registrarVenta_descuentoExcedidoPorColaborador_lanzaDescuentoExcedidoException() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(sessionContext.hasRole(Role.COLABORADOR)).thenReturn(true);

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 1, null)),
                null,
                PaymentMethod.EFECTIVO,
                new BigDecimal("15"), // 15% > máximo 10%
                null
        );

        assertThrows(DescuentoExcedidoException.class,
                () -> saleService.registrarVenta(dto));

        assertEquals(5, producto.getStockActual()); // stock no se tocó
        verify(saleRepository, never()).save(any());
    }

    // ─── ADMIN puede dar más descuento ───────────────────────────────────

    @Test
    void registrarVenta_descuento15PorcentoPorAdmin_esPermitido() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(admin));
        when(sessionContext.hasRole(Role.COLABORADOR)).thenReturn(false);

        Sale saved = new Sale();
        saved.setId(2L);
        saved.setTotal(new BigDecimal("12750.00")); // 15000 - 15%
        saved.setSubtotal(new BigDecimal("15000.00"));
        saved.setDiscountAmount(new BigDecimal("2250.00"));
        saved.setPaymentMethod(PaymentMethod.TARJETA);
        saved.setStatus(SaleStatus.COMPLETADA);
        when(saleRepository.save(any(Sale.class))).thenReturn(saved);

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 1, null)),
                null,
                PaymentMethod.TARJETA,
                new BigDecimal("15"),
                null
        );

        Sale result = saleService.registrarVenta(dto);
        assertNotNull(result);
        verify(saleRepository).save(any(Sale.class));
    }

    // ─── Precio snapshot personalizado ──────────────────────────────────

    @Test
    void registrarVenta_conPrecioManual_usaPrecioDelDTO() {
        when(cashSessionService.getSesionActiva()).thenReturn(Optional.of(sesionActiva));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));
        when(sessionContext.hasRole(Role.COLABORADOR)).thenReturn(true);

        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s.setId(3L);
            // Verify price snapshot
            SaleItem item = s.getItems().get(0);
            assertEquals(new BigDecimal("14000.00"), item.getUnitPrice());
            return s;
        });

        RegistrarVentaDTO dto = new RegistrarVentaDTO(
                List.of(new SaleItemInputDTO(10L, 1, new BigDecimal("14000"))),
                null, PaymentMethod.EFECTIVO, BigDecimal.ZERO, null
        );

        saleService.registrarVenta(dto);
        verify(saleRepository).save(any(Sale.class));
    }
}
