package com.marcofidel_dev.inventario.infrastructure.repository;

import com.marcofidel_dev.inventario.domain.entity.Customer;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.ClienteInactivoProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.TopClienteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActiveTrue();

    @Query("SELECT c FROM Customer c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR c.phone LIKE CONCAT('%', :query, '%')) AND c.active = true ORDER BY c.name")
    List<Customer> searchByNameOrPhone(@Param("query") String query);

    // ─── Analytics queries ───────────────────────────────────────────────────

    @Query(value = """
        SELECT
            c.id                                    AS clienteId,
            c.name                                  AS nombre,
            c.phone                                 AS phone,
            COUNT(DISTINCT s.id)                    AS cantidadCompras,
            COALESCE(ROUND(SUM(s.total), 2), 0.0)   AS totalComprado,
            MAX(s.sale_date)                         AS ultimaCompra
        FROM customer c
        JOIN sale s ON s.customer_id = c.id
        WHERE s.sale_date >= :desde AND s.sale_date < :hasta
          AND s.status = 'COMPLETADA'
        GROUP BY c.id, c.name, c.phone
        ORDER BY totalComprado DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<TopClienteProjection> findTopClientes(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDateTime hasta,
                                                @Param("limit") int limit);

    @Query(value = """
        SELECT
            c.id                                AS clienteId,
            c.name                              AS nombre,
            c.phone                             AS phone,
            MAX(DATE(s.sale_date))              AS ultimaCompra,
            COALESCE(ROUND(SUM(s.total), 2), 0) AS totalHistorico
        FROM customer c
        LEFT JOIN sale s ON s.customer_id = c.id AND s.status = 'COMPLETADA'
        WHERE c.active = 1
        GROUP BY c.id, c.name, c.phone
        HAVING ultimaCompra IS NULL OR ultimaCompra < :fechaCorte
        ORDER BY ultimaCompra ASC
        """, nativeQuery = true)
    List<ClienteInactivoProjection> findClientesInactivos(@Param("fechaCorte") String fechaCorte);
}
