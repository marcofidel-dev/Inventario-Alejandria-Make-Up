package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.repository.ProductoRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.MargenProductoProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.RotacionProjection;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteInventarioServiceImpl implements ReporteInventarioService {

    private final ProductoRepository productoRepository;
    private final AnalyticsCache cache;

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public ValoracionInventarioDTO getValoracionActual() {
        return cache.compute("valoracion", () -> {
            Object[] row = productoRepository.findValoracionRaw();
            if (row == null || row[0] == null) {
                return new ValoracionInventarioDTO(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, 0, 0);
            }
            BigDecimal aCosto  = toBD(row[0]);
            BigDecimal aVenta  = toBD(row[1]);
            int totalProd      = toInt(row[2]);
            int totalUnid      = toInt(row[3]);
            BigDecimal potencial = aVenta.subtract(aCosto).setScale(2, RoundingMode.HALF_UP);
            BigDecimal margen    = aVenta.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : potencial.divide(aVenta, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            return new ValoracionInventarioDTO(
                    aCosto.setScale(2, RoundingMode.HALF_UP),
                    aVenta.setScale(2, RoundingMode.HALF_UP),
                    potencial, margen, totalProd, totalUnid);
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<InventarioPorValorDTO> getInventarioPorValor() {
        return cache.compute("inv-por-valor", () ->
            productoRepository.findByActivoTrue().stream()
                    .map(p -> {
                        BigDecimal aCosto = p.getCosto()
                                .multiply(BigDecimal.valueOf(p.getStockActual()))
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal aVenta = p.getPrecioVenta()
                                .multiply(BigDecimal.valueOf(p.getStockActual()))
                                .setScale(2, RoundingMode.HALF_UP);
                        return new InventarioPorValorDTO(
                                p.getId(), p.getNombre(), p.getCodigoProducto(),
                                p.getStockActual(), p.getCosto(), p.getPrecioVenta(),
                                aCosto, aVenta);
                    })
                    .sorted(Comparator.comparing(InventarioPorValorDTO::valorAPrecioVenta).reversed())
                    .collect(Collectors.toList())
        );
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<RotacionProductoDTO> getRotacion(LocalDate desde, LocalDate hasta) {
        String key = "rotacion:" + desde + ":" + hasta;
        return cache.compute(key, () -> {
            List<RotacionProjection> rows = productoRepository.findRotacion(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            return rows.stream().map(r -> {
                int vendidas = r.getUnidadesVendidas();
                int stock    = r.getStockActual();
                BigDecimal rotacion = stock == 0 ? BigDecimal.ZERO
                        : BigDecimal.valueOf(vendidas)
                                   .divide(BigDecimal.valueOf(stock), 2, RoundingMode.HALF_UP);
                return new RotacionProductoDTO(
                        r.getProductoId(), r.getNombre(), stock, vendidas, rotacion);
            }).collect(Collectors.toList());
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<MargenProductoDTO> getMargenes() {
        return cache.compute("margenes", () -> {
            List<MargenProductoProjection> rows = productoRepository.findMargenes();
            return rows.stream().map(r -> {
                BigDecimal margenPesos = r.getPrecioVenta().subtract(r.getCosto())
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal margenPct = r.getPrecioVenta().compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : margenPesos.divide(r.getPrecioVenta(), 4, RoundingMode.HALF_UP)
                                     .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                return new MargenProductoDTO(
                        r.getProductoId(), r.getNombre(), r.getCodigoProducto(),
                        r.getCosto(), r.getPrecioVenta(), margenPesos, margenPct);
            }).collect(Collectors.toList());
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<StockPorCategoriaDTO> getStockPorCategoria() {
        return cache.compute("stock-categoria", () -> {
            List<Producto> activos = productoRepository.findByActivoTrue();
            Map<String, List<Producto>> porTipo = activos.stream()
                    .collect(Collectors.groupingBy(p -> p.getTipo().name()));

            return porTipo.entrySet().stream().map(e -> {
                List<Producto> lista = e.getValue();
                int unidades = lista.stream().mapToInt(Producto::getStockActual).sum();
                BigDecimal valorCosto = lista.stream()
                        .map(p -> p.getCosto().multiply(BigDecimal.valueOf(p.getStockActual())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);
                return new StockPorCategoriaDTO(e.getKey(), lista.size(), unidades, valorCosto);
            }).sorted(Comparator.comparing(StockPorCategoriaDTO::valorACosto).reversed())
                    .collect(Collectors.toList());
        });
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
