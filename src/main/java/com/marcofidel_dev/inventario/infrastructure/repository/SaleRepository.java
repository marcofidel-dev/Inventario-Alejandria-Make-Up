package com.marcofidel_dev.inventario.infrastructure.repository;

import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.domain.entity.SaleStatus;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCashSessionIdOrderBySaleDateDesc(Long cashSessionId);

    List<Sale> findByCashSessionIdAndStatus(Long cashSessionId, SaleStatus status);

    List<Sale> findByUserIdAndSaleDateBetweenOrderBySaleDateDesc(
            Long userId, LocalDateTime from, LocalDateTime to);

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(
            LocalDateTime from, LocalDateTime to);

    // ─── Analytics queries ───────────────────────────────────────────────────

    @Query(value = """
        SELECT
            COALESCE(ROUND(SUM(s.total), 2), 0.0) AS totalVendido,
            COUNT(DISTINCT s.id)                   AS cantidadVentas,
            COALESCE(ROUND(SUM(si.quantity * (si.unit_price - si.unit_cost)), 2), 0.0) AS utilidadBruta,
            COALESCE(ROUND(AVG(s.total), 2), 0.0)  AS ticketPromedio,
            COALESCE(SUM(si.quantity), 0)           AS cantidadProductosVendidos
        FROM sale s
        LEFT JOIN sale_item si ON si.sale_id = s.id
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        """, nativeQuery = true)
    KPIProjection findKPIs(@Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT
            DATE(s.sale_date/1000, 'unixepoch')                                       AS fecha,
            COALESCE(ROUND(SUM(s.total), 2), 0.0)                                    AS totalVendido,
            COALESCE(ROUND(SUM(si.quantity * (si.unit_price - si.unit_cost)), 2), 0.0) AS utilidad,
            COUNT(DISTINCT s.id)                                                       AS cantidadVentas
        FROM sale s
        JOIN sale_item si ON si.sale_id = s.id
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY DATE(s.sale_date/1000, 'unixepoch')
        ORDER BY fecha
        """, nativeQuery = true)
    List<VentaDiariaProjection> findVentasDiarias(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT
            CAST(STRFTIME('%H', s.sale_date/1000, 'unixepoch') AS INTEGER) AS hora,
            COUNT(DISTINCT s.id)                                            AS cantidadVentas,
            COALESCE(ROUND(SUM(s.total), 2), 0.0)                          AS totalVendido
        FROM sale s
        WHERE DATE(s.sale_date/1000, 'unixepoch') = :fecha
          AND s.status = 'COMPLETADA'
        GROUP BY STRFTIME('%H', s.sale_date/1000, 'unixepoch')
        ORDER BY hora
        """, nativeQuery = true)
    List<VentaPorHoraProjection> findVentasPorHora(@Param("fecha") String fecha);

    @Query(value = """
        SELECT
            p.id                                                                        AS productoId,
            p.codigo_producto                                                           AS codigoProducto,
            p.nombre                                                                    AS nombreProducto,
            CAST(SUM(si.quantity) AS INTEGER)                                           AS unidadesVendidas,
            COALESCE(ROUND(SUM(CAST(si.quantity AS REAL) * si.unit_price), 2), 0.0)   AS ingresoTotal,
            COALESCE(ROUND(SUM(CAST(si.quantity AS REAL) * (si.unit_price - si.unit_cost)), 2), 0.0) AS utilidadTotal
        FROM sale_item si
        JOIN sale s      ON s.id  = si.sale_id
        JOIN producto p  ON p.id  = si.producto_id
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY si.producto_id, p.codigo_producto, p.nombre
        ORDER BY unidadesVendidas DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<TopProductoProjection> findTopProductos(@Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta,
                                                  @Param("limit") int limit);

    @Query(value = """
        SELECT
            s.payment_method                       AS metodoPago,
            COALESCE(ROUND(SUM(s.total), 2), 0.0)  AS total,
            COUNT(DISTINCT s.id)                    AS cantidadVentas
        FROM sale s
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY s.payment_method
        ORDER BY total DESC
        """, nativeQuery = true)
    List<DesglosePagoProjection> findDesglosePorMetodo(@Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT
            s.user_id                                AS userId,
            COALESCE(u.full_name, u.username)        AS nombreUsuario,
            COUNT(DISTINCT s.id)                     AS cantidadVentas,
            COALESCE(ROUND(SUM(s.total), 2), 0.0)    AS totalVendido,
            COALESCE(ROUND(AVG(s.total), 2), 0.0)    AS ticketPromedio
        FROM sale s
        JOIN users u ON u.id = s.user_id
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY s.user_id, u.full_name, u.username
        ORDER BY totalVendido DESC
        """, nativeQuery = true)
    List<DesgloseUsuarioProjection> findDesglosePorUsuario(@Param("desde") LocalDateTime desde,
                                                            @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT COALESCE(u.full_name, u.username)
        FROM sale s
        JOIN users u ON u.id = s.user_id
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY s.user_id, u.full_name, u.username
        ORDER BY SUM(s.total) DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> findMejorVendedor(@Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT s.*
        FROM sale s
        WHERE DATETIME(s.sale_date/1000, 'unixepoch') >= :desde
          AND DATETIME(s.sale_date/1000, 'unixepoch') < :hasta
          AND (:estado IS NULL OR s.status = :estado)
          AND (:userId IS NULL OR s.user_id = :userId)
          AND (:clienteId IS NULL OR s.customer_id = :clienteId)
          AND (:metodoPago IS NULL OR s.payment_method = :metodoPago)
        ORDER BY s.sale_date DESC
        LIMIT 1000
        """, nativeQuery = true)
    List<Sale> findFiltradas(@Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta,
                              @Param("estado") String estado,
                              @Param("userId") Long userId,
                              @Param("clienteId") Long clienteId,
                              @Param("metodoPago") String metodoPago);
}
