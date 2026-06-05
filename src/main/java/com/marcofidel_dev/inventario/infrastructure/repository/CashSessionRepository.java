package com.marcofidel_dev.inventario.infrastructure.repository;

import com.marcofidel_dev.inventario.domain.entity.CashSession;
import com.marcofidel_dev.inventario.domain.entity.CashSessionStatus;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.DescuadreProjection;
import com.marcofidel_dev.inventario.infrastructure.repository.projection.SesionResumenProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, Long> {

    Optional<CashSession> findByUserIdAndStatus(Long userId, CashSessionStatus status);

    List<CashSession> findByUserIdOrderByOpeningDateDesc(Long userId);

    // ─── Analytics queries ───────────────────────────────────────────────────

    @Query(value = """
        SELECT
            cs.id                               AS id,
            cs.user_id                          AS userId,
            COALESCE(u.full_name, u.username)   AS nombreUsuario,
            cs.opening_date                     AS openingDate,
            cs.closing_date                     AS closingDate,
            cs.initial_cash                     AS initialCash,
            cs.expected_cash                    AS expectedCash,
            cs.declared_cash                    AS declaredCash,
            cs.cash_difference                  AS cashDifference,
            cs.status                           AS status
        FROM cash_session cs
        JOIN users u ON u.id = cs.user_id
        WHERE cs.opening_date >= :desde AND cs.opening_date < :hasta
        ORDER BY cs.opening_date DESC
        """, nativeQuery = true)
    List<SesionResumenProjection> findSesionesPorRango(@Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT
            cs.user_id                            AS userId,
            COALESCE(u.full_name, u.username)     AS nombreUsuario,
            COUNT(cs.id)                          AS cantidadDescuadres,
            COALESCE(ROUND(SUM(CASE WHEN cs.cash_difference < 0 THEN cs.cash_difference ELSE 0 END), 2), 0) AS totalFaltante,
            COALESCE(ROUND(SUM(CASE WHEN cs.cash_difference > 0 THEN cs.cash_difference ELSE 0 END), 2), 0) AS totalSobrante
        FROM cash_session cs
        JOIN users u ON u.id = cs.user_id
        WHERE cs.opening_date >= :desde AND cs.opening_date < :hasta
          AND cs.status = 'CERRADA'
          AND cs.cash_difference IS NOT NULL
          AND cs.cash_difference != 0
        GROUP BY cs.user_id, u.full_name, u.username
        ORDER BY cantidadDescuadres DESC
        """, nativeQuery = true)
    List<DescuadreProjection> findDescuadresPorUsuario(@Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta);

    @Query(value = """
        SELECT COUNT(cs.id)
        FROM cash_session cs
        WHERE cs.opening_date >= :desde AND cs.opening_date < :hasta
          AND cs.status = 'CERRADA'
          AND cs.cash_difference IS NOT NULL
          AND cs.cash_difference != 0
        """, nativeQuery = true)
    int countDescuadres(@Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta);
}
