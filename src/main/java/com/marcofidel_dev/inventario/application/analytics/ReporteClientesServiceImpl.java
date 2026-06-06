package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.infrastructure.repository.CustomerRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.ClienteInactivoProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.TopClienteProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.TopProductoProjection;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteClientesServiceImpl implements ReporteClientesService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final AnalyticsCache cache;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<TopClienteDTO> getTopClientes(LocalDate desde, LocalDate hasta, int limit) {
        String key = "top-clientes:" + desde + ":" + hasta + ":" + limit;
        return cache.compute(key, () -> {
            List<TopClienteProjection> rows = customerRepository.findTopClientes(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), limit);
            return rows.stream().map(r -> new TopClienteDTO(
                    r.getClienteId(),
                    r.getNombre(),
                    r.getPhone(),
                    r.getCantidadCompras(),
                    safe(r.getTotalComprado()),
                    parseDateTime(r.getUltimaCompra())
            )).collect(Collectors.toList());
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public ClienteDetalleAnalyticsDTO getAnalyticsCliente(Long clienteId) {
        return cache.compute("cliente-detalle:" + clienteId, () -> {
            var cliente = customerRepository.findById(clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + clienteId));

            List<Sale> ventas = saleRepository.findAll().stream()
                    .filter(s -> s.getCustomer() != null
                            && s.getCustomer().getId().equals(clienteId)
                            && s.getStatus().name().equals("COMPLETADA"))
                    .collect(Collectors.toList());

            BigDecimal total = MoneyCOP.normalize(ventas.stream()
                    .map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
            BigDecimal ticket = ventas.isEmpty() ? MoneyCOP.ZERO
                    : MoneyCOP.normalize(total.divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP));

            LocalDateTime ultimaCompra = ventas.stream()
                    .map(Sale::getSaleDate).max(LocalDateTime::compareTo).orElse(null);
            long dias = ultimaCompra == null ? 0
                    : ChronoUnit.DAYS.between(ultimaCompra.toLocalDate(), LocalDate.now());

            // Top 3 productos del cliente
            LocalDateTime epoch = LocalDateTime.of(2000, 1, 1, 0, 0);
            List<TopProductoProjection> topProds = saleRepository.findTopProductos(
                    epoch, LocalDateTime.now().plusDays(1), 3);

            List<TopProductoDTO> favs = topProds.stream().map(r -> {
                BigDecimal ing = safe(r.getIngresoTotal());
                BigDecimal util = safe(r.getUtilidadTotal());
                BigDecimal margen = ing.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                        : util.divide(ing, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                return new TopProductoDTO(r.getProductoId(), r.getCodigoProducto(),
                        r.getNombreProducto(), r.getUnidadesVendidas(), ing, util, margen);
            }).collect(Collectors.toList());

            return new ClienteDetalleAnalyticsDTO(
                    clienteId, cliente.getName(), cliente.getPhone(), cliente.getEmail(),
                    ventas.size(), total, ticket, ultimaCompra, dias, favs);
        });
    }

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<ClienteInactivoDTO> getClientesInactivos(int diasSinComprar) {
        String key = "clientes-inactivos:" + diasSinComprar;
        return cache.compute(key, () -> {
            String fechaCorte = LocalDate.now().minusDays(diasSinComprar).toString();
            List<ClienteInactivoProjection> rows = customerRepository.findClientesInactivos(fechaCorte);
            return rows.stream().map(r -> {
                LocalDate ultCompra = r.getUltimaCompra() != null
                        ? LocalDate.parse(r.getUltimaCompra().substring(0, 10)) : null;
                long dias = ultCompra == null ? Long.MAX_VALUE
                        : ChronoUnit.DAYS.between(ultCompra, LocalDate.now());
                return new ClienteInactivoDTO(
                        r.getClienteId(), r.getNombre(), r.getPhone(),
                        ultCompra, safe(r.getTotalHistorico()), dias);
            }).collect(Collectors.toList());
        });
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null) return null;
        try { return LocalDateTime.parse(s.substring(0, 19), DT_FMT); } catch (Exception e) { return null; }
    }

    private BigDecimal safe(BigDecimal v) {
        return MoneyCOP.normalize(v);
    }
}
