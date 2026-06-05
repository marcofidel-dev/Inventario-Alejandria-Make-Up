package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.dto.CashSessionResumenDTO;
import com.marcofidel_dev.inventario.domain.entity.*;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.security.AccessDeniedException;
import com.marcofidel_dev.inventario.infrastructure.security.Audited;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashSessionService {

    private final CashSessionRepository cashSessionRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final SessionContext sessionContext;

    @Audited(action = AuditAction.OPEN_CASH_SESSION, entity = "CashSession")
    @Transactional
    public CashSession abrirSesion(BigDecimal efectivoInicial, String observaciones) {
        User user = sessionContext.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado"));

        cashSessionRepository.findByUserIdAndStatus(user.getId(), CashSessionStatus.ABIERTA)
                .ifPresent(s -> {
                    throw new IllegalStateException(
                            "Ya existe una sesión de caja abierta para " + user.getUsername());
                });

        CashSession session = new CashSession();
        session.setUserId(user.getId());
        session.setOpeningDate(LocalDateTime.now());
        session.setInitialCash(efectivoInicial.setScale(2, RoundingMode.HALF_UP));
        session.setOpeningNotes(observaciones);
        session.setStatus(CashSessionStatus.ABIERTA);

        log.info("Apertura de caja: usuario={} efectivoInicial={}", user.getUsername(), efectivoInicial);
        return cashSessionRepository.save(session);
    }

    @Audited(action = AuditAction.CLOSE_CASH_SESSION, entity = "CashSession")
    @Transactional
    public CashSession cerrarSesion(Long sesionId, BigDecimal efectivoDeclarado, String observaciones) {
        CashSession session = cashSessionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión de caja no encontrada: " + sesionId));

        if (session.getStatus() == CashSessionStatus.CERRADA) {
            throw new IllegalStateException("La sesión de caja ya está cerrada");
        }

        User user = sessionContext.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado"));

        if (!session.getUserId().equals(user.getId()) && !sessionContext.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("No tienes permiso para cerrar esta sesión de caja");
        }

        // Expected cash = initial + sum of completed EFECTIVO sales in this session
        BigDecimal cashSales = saleRepository
                .findByCashSessionIdAndStatus(sesionId, SaleStatus.COMPLETADA)
                .stream()
                .filter(s -> s.getPaymentMethod() == PaymentMethod.EFECTIVO)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedCash = session.getInitialCash().add(cashSales).setScale(2, RoundingMode.HALF_UP);
        BigDecimal declared = efectivoDeclarado.setScale(2, RoundingMode.HALF_UP);
        BigDecimal difference = declared.subtract(expectedCash).setScale(2, RoundingMode.HALF_UP);

        session.setClosingDate(LocalDateTime.now());
        session.setDeclaredCash(declared);
        session.setExpectedCash(expectedCash);
        session.setCashDifference(difference);
        session.setClosingNotes(observaciones);
        session.setStatus(CashSessionStatus.CERRADA);

        log.info("Cierre de caja: sesion={} esperado={} declarado={} diferencia={}",
                sesionId, expectedCash, declared, difference);
        return cashSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Optional<CashSession> getSesionActiva() {
        User user = sessionContext.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado"));
        return cashSessionRepository.findByUserIdAndStatus(user.getId(), CashSessionStatus.ABIERTA);
    }

    @Transactional(readOnly = true)
    public CashSessionResumenDTO getResumenSesion(Long sesionId) {
        CashSession session = cashSessionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        List<Sale> completedSales = saleRepository
                .findByCashSessionIdAndStatus(sesionId, SaleStatus.COMPLETADA);

        BigDecimal totalSales = completedSales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Map<PaymentMethod, BigDecimal> salesByMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod method : PaymentMethod.values()) {
            BigDecimal sub = completedSales.stream()
                    .filter(s -> s.getPaymentMethod() == method)
                    .map(Sale::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sub.compareTo(BigDecimal.ZERO) > 0) {
                salesByMethod.put(method, sub.setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal cashSales = salesByMethod.getOrDefault(PaymentMethod.EFECTIVO, BigDecimal.ZERO);
        BigDecimal expectedCash = session.getInitialCash().add(cashSales).setScale(2, RoundingMode.HALF_UP);

        String userName = userRepository.findById(session.getUserId())
                .map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                .orElse("Usuario " + session.getUserId());

        return new CashSessionResumenDTO(
                session.getId(),
                userName,
                session.getOpeningDate(),
                session.getInitialCash(),
                totalSales,
                completedSales.size(),
                salesByMethod,
                expectedCash,
                session.getStatus()
        );
    }
}
