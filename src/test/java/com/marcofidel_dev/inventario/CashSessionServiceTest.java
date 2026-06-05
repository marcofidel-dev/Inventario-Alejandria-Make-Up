package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.dto.CashSessionResumenDTO;
import com.marcofidel_dev.inventario.application.service.CashSessionService;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashSessionServiceTest {

    @Mock private CashSessionRepository cashSessionRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private UserRepository userRepository;
    @Mock private SessionContext sessionContext;

    @InjectMocks private CashSessionService cashSessionService;

    private User colaborador;

    @BeforeEach
    void setUp() {
        colaborador = User.builder()
                .id(2L).username("alejandrina").role(Role.COLABORADOR).active(true).build();
    }

    // ─── Abrir sesión ────────────────────────────────────────────────────

    @Test
    void abrirSesion_sinSesionPrevia_creaYRetorna() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));
        when(cashSessionRepository.findByUserIdAndStatus(2L, CashSessionStatus.ABIERTA))
                .thenReturn(Optional.empty());

        CashSession saved = new CashSession();
        saved.setId(1L);
        saved.setUserId(2L);
        saved.setStatus(CashSessionStatus.ABIERTA);
        saved.setInitialCash(new BigDecimal("50000.00"));
        saved.setOpeningDate(LocalDateTime.now());
        when(cashSessionRepository.save(any(CashSession.class))).thenReturn(saved);

        CashSession result = cashSessionService.abrirSesion(new BigDecimal("50000"), null);

        assertNotNull(result);
        assertEquals(CashSessionStatus.ABIERTA, result.getStatus());
        assertEquals(new BigDecimal("50000.00"), result.getInitialCash());
        verify(cashSessionRepository).save(any(CashSession.class));
    }

    // ─── No permite dos sesiones abiertas ───────────────────────────────

    @Test
    void abrirSesion_conSesionYaAbierta_lanzaIllegalStateException() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));

        CashSession existente = new CashSession();
        existente.setId(99L);
        existente.setStatus(CashSessionStatus.ABIERTA);
        when(cashSessionRepository.findByUserIdAndStatus(2L, CashSessionStatus.ABIERTA))
                .thenReturn(Optional.of(existente));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cashSessionService.abrirSesion(BigDecimal.ZERO, null));

        assertTrue(ex.getMessage().contains("alejandrina"));
        verify(cashSessionRepository, never()).save(any());
    }

    // ─── getSesionActiva ─────────────────────────────────────────────────

    @Test
    void getSesionActiva_conSesionAbierta_retornaPresente() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));
        CashSession session = new CashSession();
        session.setId(1L);
        session.setStatus(CashSessionStatus.ABIERTA);
        when(cashSessionRepository.findByUserIdAndStatus(2L, CashSessionStatus.ABIERTA))
                .thenReturn(Optional.of(session));

        Optional<CashSession> result = cashSessionService.getSesionActiva();

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getSesionActiva_sinSesion_retornaVacio() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));
        when(cashSessionRepository.findByUserIdAndStatus(2L, CashSessionStatus.ABIERTA))
                .thenReturn(Optional.empty());

        Optional<CashSession> result = cashSessionService.getSesionActiva();

        assertTrue(result.isEmpty());
    }

    // ─── Cerrar sesión ───────────────────────────────────────────────────

    @Test
    void cerrarSesion_calcula_efectivoEsperadoCorrectamente() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));

        CashSession session = new CashSession();
        session.setId(1L);
        session.setUserId(2L);
        session.setStatus(CashSessionStatus.ABIERTA);
        session.setInitialCash(new BigDecimal("100000.00"));
        session.setOpeningDate(LocalDateTime.now());
        when(cashSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Two completed EFECTIVO sales for $30.000 and $20.000
        Sale s1 = new Sale(); s1.setTotal(new BigDecimal("30000.00")); s1.setPaymentMethod(PaymentMethod.EFECTIVO);
        Sale s2 = new Sale(); s2.setTotal(new BigDecimal("20000.00")); s2.setPaymentMethod(PaymentMethod.EFECTIVO);
        Sale s3 = new Sale(); s3.setTotal(new BigDecimal("45000.00")); s3.setPaymentMethod(PaymentMethod.TARJETA);
        when(saleRepository.findByCashSessionIdAndStatus(1L, SaleStatus.COMPLETADA))
                .thenReturn(List.of(s1, s2, s3));

        when(cashSessionRepository.save(any(CashSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal efectivoDeclarado = new BigDecimal("150000.00");
        CashSession closed = cashSessionService.cerrarSesion(1L, efectivoDeclarado, null);

        // expectedCash = initialCash + cashSales = 100000 + 50000 = 150000
        assertEquals(new BigDecimal("150000.00"), closed.getExpectedCash());
        assertEquals(new BigDecimal("150000.00"), closed.getDeclaredCash());
        // difference = declared - expected = 150000 - 150000 = 0
        assertEquals(BigDecimal.ZERO.setScale(2), closed.getCashDifference().setScale(2));
        assertEquals(CashSessionStatus.CERRADA, closed.getStatus());
    }

    @Test
    void cerrarSesion_conFaltante_calculaDiferenciaCorrectamente() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(colaborador));

        CashSession session = new CashSession();
        session.setId(2L);
        session.setUserId(2L);
        session.setStatus(CashSessionStatus.ABIERTA);
        session.setInitialCash(new BigDecimal("100000.00"));
        session.setOpeningDate(LocalDateTime.now());
        when(cashSessionRepository.findById(2L)).thenReturn(Optional.of(session));

        Sale s1 = new Sale(); s1.setTotal(new BigDecimal("50000.00")); s1.setPaymentMethod(PaymentMethod.EFECTIVO);
        when(saleRepository.findByCashSessionIdAndStatus(2L, SaleStatus.COMPLETADA))
                .thenReturn(List.of(s1));

        when(cashSessionRepository.save(any(CashSession.class))).thenAnswer(inv -> inv.getArgument(0));

        // Expected = 100000 + 50000 = 150000, declared = 140000 → difference = -10000 (faltante)
        CashSession closed = cashSessionService.cerrarSesion(2L, new BigDecimal("140000"), null);

        assertEquals(new BigDecimal("150000.00"), closed.getExpectedCash());
        assertTrue(closed.getCashDifference().compareTo(BigDecimal.ZERO) < 0);
        assertEquals(new BigDecimal("-10000.00"), closed.getCashDifference());
    }

    // ─── Resumen de caja ─────────────────────────────────────────────────

    @Test
    void getResumenSesion_retornaTotalesCorrectamente() {
        CashSession session = new CashSession();
        session.setId(3L);
        session.setUserId(2L);
        session.setInitialCash(new BigDecimal("80000.00"));
        session.setOpeningDate(LocalDateTime.now());
        session.setStatus(CashSessionStatus.ABIERTA);
        when(cashSessionRepository.findById(3L)).thenReturn(Optional.of(session));

        Sale s1 = new Sale(); s1.setTotal(new BigDecimal("25000.00")); s1.setPaymentMethod(PaymentMethod.EFECTIVO);
        Sale s2 = new Sale(); s2.setTotal(new BigDecimal("35000.00")); s2.setPaymentMethod(PaymentMethod.NEQUI);
        when(saleRepository.findByCashSessionIdAndStatus(3L, SaleStatus.COMPLETADA))
                .thenReturn(List.of(s1, s2));
        when(userRepository.findById(2L)).thenReturn(Optional.of(colaborador));

        CashSessionResumenDTO resumen = cashSessionService.getResumenSesion(3L);

        assertEquals(new BigDecimal("60000.00"), resumen.totalSales());
        assertEquals(2, resumen.saleCount());
        // expectedCash = 80000 (initial) + 25000 (efectivo) = 105000
        assertEquals(new BigDecimal("105000.00"), resumen.expectedCash());
        assertEquals(new BigDecimal("25000.00"), resumen.salesByMethod().get(PaymentMethod.EFECTIVO));
        assertEquals(new BigDecimal("35000.00"), resumen.salesByMethod().get(PaymentMethod.NEQUI));
    }
}
