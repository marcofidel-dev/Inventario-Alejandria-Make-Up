package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.DescuadreProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.SesionResumenProjection;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteCajaServiceImpl implements ReporteCajaService {

    private final CashSessionRepository cashSessionRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<SesionCajaResumenDTO> listarSesiones(LocalDate desde, LocalDate hasta) {
        List<SesionResumenProjection> rows = cashSessionRepository.findSesionesPorRango(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        return rows.stream().map(this::toResumenDTO).collect(Collectors.toList());
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public SesionCajaDetalleDTO getDetalleSesion(Long sesionId) {
        var sesion = cashSessionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        String nombreUsuario = userRepository.findById(sesion.getUserId())
                .map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                .orElse("Usuario " + sesion.getUserId());

        SesionCajaResumenDTO resumen = new SesionCajaResumenDTO(
                sesion.getId(), sesion.getUserId(), nombreUsuario,
                sesion.getOpeningDate(), sesion.getClosingDate(),
                sesion.getInitialCash(), sesion.getExpectedCash(),
                sesion.getDeclaredCash(), sesion.getCashDifference(),
                sesion.getStatus().name());

        List<Sale> salesRaw = saleRepository.findByCashSessionIdOrderBySaleDateDesc(sesionId);
        Map<Long, String> userNames = buildUserNameMap();

        List<VentaResumenDTO> ventas = salesRaw.stream().map(s -> new VentaResumenDTO(
                s.getId(), s.getSaleDate(),
                s.getCustomer() != null ? s.getCustomer().getName() : "Ocasional",
                s.getTotal(), s.getPaymentMethod().name(), s.getStatus().name(),
                userNames.getOrDefault(s.getUserId(), "Usuario " + s.getUserId()),
                s.getItems().size()
        )).collect(Collectors.toList());

        BigDecimal totalVentas = salesRaw.stream()
                .filter(s -> s.getStatus().name().equals("COMPLETADA"))
                .map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new SesionCajaDetalleDTO(resumen, ventas, totalVentas, ventas.size());
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public DescuadresResumenDTO getResumenDescuadres(LocalDate desde, LocalDate hasta) {
        LocalDateTime desdedt = desde.atStartOfDay();
        LocalDateTime hastadte = hasta.plusDays(1).atStartOfDay();

        List<SesionCajaResumenDTO> todas = listarSesiones(desde, hasta);
        int totalSesiones   = todas.size();
        int totalDescuadres = cashSessionRepository.countDescuadres(desdedt, hastadte);

        List<DescuadreProjection> porUser = cashSessionRepository.findDescuadresPorUsuario(desdedt, hastadte);

        BigDecimal totalFaltante = porUser.stream()
                .map(d -> safe(d.getTotalFaltante())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSobrante = porUser.stream()
                .map(d -> safe(d.getTotalSobrante())).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DesgloseUsuarioDescuadreDTO> desglose = porUser.stream().map(d ->
                new DesgloseUsuarioDescuadreDTO(
                        d.getUserId(), d.getNombreUsuario(),
                        d.getCantidadDescuadres(),
                        safe(d.getTotalFaltante()),
                        safe(d.getTotalSobrante()))
        ).collect(Collectors.toList());

        return new DescuadresResumenDTO(
                totalSesiones, totalDescuadres, totalFaltante, totalSobrante, desglose);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private SesionCajaResumenDTO toResumenDTO(SesionResumenProjection r) {
        return new SesionCajaResumenDTO(
                r.getId(), r.getUserId(), r.getNombreUsuario(),
                parseDateTime(r.getOpeningDate()),
                parseDateTime(r.getClosingDate()),
                safe(r.getInitialCash()),
                safe(r.getExpectedCash()),
                safe(r.getDeclaredCash()),
                safe(r.getCashDifference()),
                r.getStatus());
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String trimmed = s.length() > 19 ? s.substring(0, 19) : s;
            return LocalDateTime.parse(trimmed, DT_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<Long, String> buildUserNameMap() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        u -> u.getId(),
                        u -> u.getFullName() != null ? u.getFullName() : u.getUsername()));
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}
