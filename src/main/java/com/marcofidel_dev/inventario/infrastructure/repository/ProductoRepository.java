package com.marcofidel_dev.inventario.infrastructure.repository;

import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.MargenProductoProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.ProductoStockCriticoProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.RotacionProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.SinRotacionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    Optional<Producto> findByCodigoProducto(String codigoProducto);

    List<Producto> findByTipo(Producto.TipoProducto tipo);

    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosConStockBajo();

    @Query("SELECT p FROM Producto p WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :search, '%'))) AND p.activo = true")
    List<Producto> buscarPorNombreOCodigoProducto(String search);

    // ─── Analytics queries ───────────────────────────────────────────────────

    @Query(value = """
        SELECT
            p.id               AS productoId,
            p.nombre           AS nombre,
            p.codigo_producto  AS codigoProducto,
            p.stock_actual     AS stockActual,
            p.stock_minimo     AS stockMinimo
        FROM producto p
        WHERE p.activo = 1
          AND p.stock_actual <= p.stock_minimo
        ORDER BY (p.stock_actual - p.stock_minimo) ASC
        """, nativeQuery = true)
    List<ProductoStockCriticoProjection> findStockCritico();

    @Query(value = """
        SELECT
            p.id               AS productoId,
            p.nombre           AS nombre,
            p.codigo_producto  AS codigoProducto,
            p.costo            AS costo,
            p.precio_venta     AS precioVenta
        FROM producto p
        WHERE p.activo = 1
        ORDER BY (p.precio_venta - p.costo) DESC
        """, nativeQuery = true)
    List<MargenProductoProjection> findMargenes();

    @Query(value = """
        SELECT
            p.id                                            AS productoId,
            p.nombre                                        AS nombre,
            p.stock_actual                                  AS stockActual,
            CAST(COALESCE(SUM(si.quantity), 0) AS INTEGER)  AS unidadesVendidas
        FROM producto p
        LEFT JOIN sale_item si ON si.producto_id = p.id
        LEFT JOIN sale s       ON s.id = si.sale_id
            AND s.status = 'COMPLETADA'
            AND s.sale_date >= :desde AND s.sale_date < :hasta
        WHERE p.activo = 1
        GROUP BY p.id, p.nombre, p.stock_actual
        ORDER BY unidadesVendidas DESC
        """, nativeQuery = true)
    List<RotacionProjection> findRotacion(@Param("desde") LocalDateTime desde,
                                           @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT
            p.id                        AS productoId,
            p.nombre                    AS nombre,
            p.stock_actual              AS stockActual,
            MAX(DATE(s.sale_date))      AS ultimaVenta
        FROM producto p
        LEFT JOIN sale_item si ON si.producto_id = p.id
        LEFT JOIN sale s       ON s.id = si.sale_id AND s.status = 'COMPLETADA'
        WHERE p.activo = 1
          AND p.stock_actual > 0
        GROUP BY p.id, p.nombre, p.stock_actual
        HAVING ultimaVenta IS NULL OR ultimaVenta < :fechaCorte
        ORDER BY ultimaVenta ASC
        """, nativeQuery = true)
    List<SinRotacionProjection> findSinRotacion(@Param("fechaCorte") String fechaCorte);

    @Query(value = """
        SELECT
            SUM(CAST(p.stock_actual AS REAL) * p.costo)       AS valorACosto,
            SUM(CAST(p.stock_actual AS REAL) * p.precio_venta) AS valorAPrecioVenta,
            COUNT(p.id)                                         AS totalProductos,
            SUM(p.stock_actual)                                 AS totalUnidades
        FROM producto p
        WHERE p.activo = 1
        """, nativeQuery = true)
    Object[] findValoracionRaw();
}
