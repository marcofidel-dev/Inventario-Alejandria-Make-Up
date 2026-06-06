package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.*;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteVentasServiceImpl implements ReporteVentasService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final AnalyticsCache cache;

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public ReporteVentasPeriodoDTO generarReporte(FiltroReporteDTO filtro) {
        LocalDateTime desde = filtro.desde().atStartOfDay();
        LocalDateTime hasta = filtro.hasta().plusDays(1).atStartOfDay();

        KPIProjection kpi = saleRepository.findKPIs(desde, hasta);
        List<VentaDiariaDTO> diarias = getSeriesDiarias(filtro.desde(), filtro.hasta());
        List<TopProductoDTO> top = toTopProductoDTOList(
                saleRepository.findTopProductos(desde, hasta, 10));
        List<DesglosePagoDTO> desglosePago = getDesglosePorMetodo(filtro.desde(), filtro.hasta());
        List<DesgloseUsuarioDTO> desgloseUser = getDesglosePorUsuario(filtro.desde(), filtro.hasta());

        BigDecimal totalVendido = safe(kpi != null ? kpi.getTotalVendido() : null);
        BigDecimal utilidad     = safe(kpi != null ? kpi.getUtilidadBruta() : null);
        BigDecimal margen       = totalVendido.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : utilidad.divide(totalVendido, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return new ReporteVentasPeriodoDTO(
                filtro.desde(), filtro.hasta(),
                totalVendido, utilidad, margen,
                kpi != null ? kpi.getCantidadVentas() : 0,
                safe(kpi != null ? kpi.getTicketPromedio() : null),
                diarias, top, desglosePago, desgloseUser);
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<VentaResumenDTO> listarVentasFiltradas(FiltroReporteDTO filtro) {
        String estado     = filtro.incluirAnuladas() ? null : "COMPLETADA";
        String metodoPago = filtro.metodoPago() != null ? filtro.metodoPago().name() : null;

        List<Sale> ventas = saleRepository.findFiltradas(
                filtro.desde().atStartOfDay(),
                filtro.hasta().plusDays(1).atStartOfDay(),
                estado,
                filtro.usuarioId(),
                filtro.clienteId(),
                metodoPago);

        // Optional product filter (applied in Java — lista ya limitada a 1000)
        if (filtro.productoId() != null) {
            Long pid = filtro.productoId();
            ventas = ventas.stream()
                    .filter(s -> s.getItems().stream()
                            .anyMatch(i -> i.getProducto().getId().equals(pid)))
                    .collect(Collectors.toList());
        }

        Map<Long, String> userNames = buildUserNameMap();

        return ventas.stream().map(s -> new VentaResumenDTO(
                s.getId(),
                s.getSaleDate(),
                s.getCustomer() != null ? s.getCustomer().getName() : "Ocasional",
                s.getTotal(),
                s.getPaymentMethod().name(),
                s.getStatus().name(),
                userNames.getOrDefault(s.getUserId(), "Usuario " + s.getUserId()),
                s.getItems().size()
        )).collect(Collectors.toList());
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<DesglosePagoDTO> getDesglosePorMetodo(LocalDate desde, LocalDate hasta) {
        String key = "desglose-pago:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<DesglosePagoProjection> rows = saleRepository.findDesglosePorMetodo(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());

            BigDecimal totalGeneral = rows.stream()
                    .map(r -> safe(r.getTotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return rows.stream().map(r -> {
                BigDecimal total = safe(r.getTotal());
                BigDecimal pct = totalGeneral.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : total.divide(totalGeneral, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                return new DesglosePagoDTO(r.getMetodoPago(), total, r.getCantidadVentas(), pct);
            }).collect(Collectors.toList());
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<DesgloseUsuarioDTO> getDesglosePorUsuario(LocalDate desde, LocalDate hasta) {
        String key = "desglose-usuario:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<DesgloseUsuarioProjection> rows = saleRepository.findDesglosePorUsuario(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            return rows.stream().map(r -> new DesgloseUsuarioDTO(
                    r.getUserId(),
                    r.getNombreUsuario(),
                    r.getCantidadVentas(),
                    safe(r.getTotalVendido()),
                    safe(r.getTicketPromedio())
            )).collect(Collectors.toList());
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public AnalisisABCDTO getAnalisisABC(LocalDate desde, LocalDate hasta) {
        String key = "abc:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<TopProductoProjection> rows = saleRepository.findTopProductos(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), Integer.MAX_VALUE);

            BigDecimal totalIngresos = rows.stream()
                    .map(r -> safe(r.getIngresoTotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<ProductoABCDTO> categoriaA = new ArrayList<>();
            List<ProductoABCDTO> categoriaB = new ArrayList<>();
            List<ProductoABCDTO> categoriaC = new ArrayList<>();

            BigDecimal acumulado = BigDecimal.ZERO;

            for (TopProductoProjection r : rows) {
                BigDecimal ingreso = safe(r.getIngresoTotal());
                acumulado = acumulado.add(ingreso);
                BigDecimal pctAcum = totalIngresos.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : acumulado.divide(totalIngresos, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

                ProductoABCDTO dto = new ProductoABCDTO(
                        r.getProductoId(), r.getNombreProducto(), r.getCodigoProducto(),
                        r.getUnidadesVendidas(), ingreso, pctAcum);

                if (pctAcum.compareTo(BigDecimal.valueOf(80)) <= 0) {
                    categoriaA.add(dto);
                } else if (pctAcum.compareTo(BigDecimal.valueOf(95)) <= 0) {
                    categoriaB.add(dto);
                } else {
                    categoriaC.add(dto);
                }
            }

            return new AnalisisABCDTO(categoriaA, categoriaB, categoriaC, totalIngresos);
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private List<VentaDiariaDTO> getSeriesDiarias(LocalDate desde, LocalDate hasta) {
        return saleRepository.findVentasDiarias(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())
                .stream()
                .map(r -> new VentaDiariaDTO(
                        LocalDate.parse(r.getFecha()),
                        safe(r.getTotalVendido()),
                        safe(r.getUtilidad()),
                        r.getCantidadVentas()))
                .collect(Collectors.toList());
    }

    private List<TopProductoDTO> toTopProductoDTOList(List<TopProductoProjection> rows) {
        return rows.stream().map(r -> {
            BigDecimal ingreso  = safe(r.getIngresoTotal());
            BigDecimal utilidad = safe(r.getUtilidadTotal());
            BigDecimal margen   = ingreso.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : utilidad.divide(ingreso, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            return new TopProductoDTO(
                    r.getProductoId(), r.getCodigoProducto(), r.getNombreProducto(),
                    r.getUnidadesVendidas(), ingreso, utilidad, margen);
        }).collect(Collectors.toList());
    }

    private Map<Long, String> buildUserNameMap() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        u -> u.getId(),
                        u -> u.getFullName() != null ? u.getFullName() : u.getUsername()));
    }

    private BigDecimal safe(BigDecimal v) {
        return MoneyCOP.normalize(v);
    }
}
