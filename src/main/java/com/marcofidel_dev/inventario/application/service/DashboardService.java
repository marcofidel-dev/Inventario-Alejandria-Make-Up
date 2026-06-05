package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.repository.CashSessionRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductoRepository productoRepository;
    private final CashSessionRepository cashSessionRepository;
    private final AnalyticsCache cache;

    private static final int STOCK_CRITICO_DIAS_ROTACION = 30;
    private static final int TOP_PRODUCTOS_LIMIT = 10;

    // ─── KPIs ────────────────────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public KPIsDelDiaDTO getKPIsDelDia() {
        LocalDate hoy = LocalDate.now();
        return cache.compute("kpis:dia:" + hoy, () -> calcularKPIsDelDia(hoy));
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public KPIsRangoDTO getKPIsRango(LocalDate desde, LocalDate hasta) {
        String key = "kpis:rango:" + desde + ":" + hasta;
        return cache.compute(key, () -> calcularKPIsRango(desde, hasta));
    }

    // ─── Series temporales ───────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<VentaDiariaDTO> getSeriesVentas(LocalDate desde, LocalDate hasta) {
        String key = "series:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<VentaDiariaProjection> rows = saleRepository.findVentasDiarias(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            return rows.stream()
                    .map(r -> new VentaDiariaDTO(
                            LocalDate.parse(r.getFecha()),
                            safe(r.getTotalVendido()),
                            safe(r.getUtilidad()),
                            r.getCantidadVentas()))
                    .collect(Collectors.toList());
        });
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<VentaPorHoraDTO> getVentasPorHora(LocalDate fecha) {
        String key = "hora:" + fecha;
        return cache.compute(key, () -> {
            List<VentaPorHoraProjection> rows =
                    saleRepository.findVentasPorHora(fecha.toString());
            return rows.stream()
                    .map(r -> new VentaPorHoraDTO(
                            r.getHora(),
                            r.getCantidadVentas(),
                            safe(r.getTotalVendido())))
                    .collect(Collectors.toList());
        });
    }

    // ─── Top productos ───────────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<TopProductoDTO> getTopProductos(LocalDate desde, LocalDate hasta, int limit) {
        String key = "top:" + desde + ":" + hasta + ":" + limit;
        return cache.compute(key, () -> {
            List<TopProductoProjection> rows = saleRepository.findTopProductos(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), limit);
            return rows.stream().map(this::toTopProductoDTO).collect(Collectors.toList());
        });
    }

    // ─── Stock y rotación ────────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<ProductoStockCriticoDTO> getProductosStockCritico() {
        return cache.compute("stock-critico", () -> {
            List<ProductoStockCriticoProjection> rows = productoRepository.findStockCritico();
            return rows.stream()
                    .map(r -> new ProductoStockCriticoDTO(
                            r.getProductoId(),
                            r.getNombre(),
                            r.getCodigoProducto(),
                            r.getStockActual(),
                            r.getStockMinimo(),
                            r.getStockActual() - r.getStockMinimo()))
                    .collect(Collectors.toList());
        });
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<ProductoSinRotacionDTO> getProductosSinRotacion(int diasSinVenta) {
        return cache.compute("sin-rotacion:" + diasSinVenta, () -> {
            String fechaCorte = LocalDate.now().minusDays(diasSinVenta).toString();
            List<SinRotacionProjection> rows = productoRepository.findSinRotacion(fechaCorte);
            return rows.stream()
                    .map(r -> new ProductoSinRotacionDTO(
                            r.getProductoId(),
                            r.getNombre(),
                            r.getStockActual(),
                            r.getUltimaVenta() != null ? LocalDate.parse(r.getUltimaVenta()) : null))
                    .collect(Collectors.toList());
        });
    }

    // ─── Alertas ─────────────────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public AlertasResumenDTO getAlertas() {
        return cache.compute("alertas", () -> {
            int stockCritico = productoRepository.findStockCritico().size();
            int sinRotacion = productoRepository.findSinRotacion(
                    LocalDate.now().minusDays(STOCK_CRITICO_DIAS_ROTACION).toString()).size();
            LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
            int descuadres = cashSessionRepository.countDescuadres(
                    hace30Dias, LocalDateTime.now().plusDays(1));
            return new AlertasResumenDTO(stockCritico, sinRotacion, descuadres);
        });
    }

    // ─── Desglose por usuario ────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<DesgloseUsuarioDTO> getDesglosePorUsuario(LocalDate desde, LocalDate hasta) {
        String key = "desglose-usuario:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<DesgloseUsuarioProjection> rows = saleRepository.findDesglosePorUsuario(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            return rows.stream()
                    .map(r -> new DesgloseUsuarioDTO(
                            r.getUserId(),
                            r.getNombreUsuario(),
                            r.getCantidadVentas(),
                            safe(r.getTotalVendido()),
                            safe(r.getTicketPromedio())))
                    .collect(Collectors.toList());
        });
    }

    // ─── Valoración ──────────────────────────────────────────────────────────

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public ValoracionInventarioDTO getValoracionInventario() {
        return cache.compute("valoracion", () -> {
            Object[] row = productoRepository.findValoracionRaw();
            if (row == null || row[0] == null) {
                return new ValoracionInventarioDTO(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, 0, 0);
            }
            BigDecimal aCosto   = toBD(row[0]);
            BigDecimal aVenta   = toBD(row[1]);
            int totalProd       = toInt(row[2]);
            int totalUnid       = toInt(row[3]);
            BigDecimal potencial = aVenta.subtract(aCosto).setScale(2, RoundingMode.HALF_UP);
            BigDecimal margen   = aCosto.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : potencial.divide(aVenta, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100))
                               .setScale(2, RoundingMode.HALF_UP);
            return new ValoracionInventarioDTO(
                    aCosto.setScale(2, RoundingMode.HALF_UP),
                    aVenta.setScale(2, RoundingMode.HALF_UP),
                    potencial,
                    margen,
                    totalProd,
                    totalUnid);
        });
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private KPIsDelDiaDTO calcularKPIsDelDia(LocalDate hoy) {
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();
        LocalDateTime desdeAyer = hoy.minusDays(1).atStartOfDay();
        LocalDateTime hastaAyer = hoy.atStartOfDay();
        LocalDateTime desdeSemPas = hoy.minusDays(7).atStartOfDay();
        LocalDateTime hastaSemPas = hoy.minusDays(6).atStartOfDay();

        KPIProjection hoyData   = saleRepository.findKPIs(desde, hasta);
        KPIProjection ayerData  = saleRepository.findKPIs(desdeAyer, hastaAyer);
        KPIProjection semData   = saleRepository.findKPIs(desdeSemPas, hastaSemPas);

        BigDecimal totalHoy  = safe(hoyData  != null ? hoyData.getTotalVendido()  : null);
        BigDecimal totalAyer = safe(ayerData != null ? ayerData.getTotalVendido() : null);
        BigDecimal totalSem  = safe(semData  != null ? semData.getTotalVendido()  : null);

        BigDecimal pctAyer = calcPorcentajeCambio(totalHoy, totalAyer);
        BigDecimal pctSem  = calcPorcentajeCambio(totalHoy, totalSem);

        String mejorVendedor = saleRepository.findMejorVendedor(desde, hasta).orElse("—");

        return new KPIsDelDiaDTO(
                totalHoy,
                hoyData != null ? hoyData.getCantidadVentas() : 0,
                safe(hoyData != null ? hoyData.getUtilidadBruta() : null),
                safe(hoyData != null ? hoyData.getTicketPromedio() : null),
                pctAyer,
                pctSem,
                hoyData != null ? hoyData.getCantidadProductosVendidos() : 0,
                mejorVendedor);
    }

    private KPIsRangoDTO calcularKPIsRango(LocalDate desde, LocalDate hasta) {
        KPIProjection data = saleRepository.findKPIs(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        if (data == null || data.getCantidadVentas() == 0) {
            return new KPIsRangoDTO(
                    BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
        BigDecimal totalVendido = safe(data.getTotalVendido());
        BigDecimal utilidad     = safe(data.getUtilidadBruta());
        BigDecimal margen       = totalVendido.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : utilidad.divide(totalVendido, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .setScale(2, RoundingMode.HALF_UP);
        return new KPIsRangoDTO(
                totalVendido,
                data.getCantidadVentas(),
                utilidad,
                safe(data.getTicketPromedio()),
                margen,
                data.getCantidadProductosVendidos());
    }

    private TopProductoDTO toTopProductoDTO(TopProductoProjection r) {
        BigDecimal ingreso   = safe(r.getIngresoTotal());
        BigDecimal utilidad  = safe(r.getUtilidadTotal());
        BigDecimal margen    = ingreso.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : utilidad.divide(ingreso, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .setScale(2, RoundingMode.HALF_UP);
        return new TopProductoDTO(
                r.getProductoId(),
                r.getCodigoProducto(),
                r.getNombreProducto(),
                r.getUnidadesVendidas(),
                ingreso,
                utilidad,
                margen);
    }

    private BigDecimal calcPorcentajeCambio(BigDecimal actual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal toBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return new BigDecimal(o.toString()).setScale(2, RoundingMode.HALF_UP);
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        return ((Number) o).intValue();
    }
}
