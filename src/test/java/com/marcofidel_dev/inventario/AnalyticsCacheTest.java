package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.analytics.cache.AnalyticsCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsCacheTest {

    private AnalyticsCache cache;

    @BeforeEach
    void setUp() {
        cache = new AnalyticsCache();
    }

    @Test
    void compute_primerAcceso_ejecutaSupplier() {
        AtomicInteger llamadas = new AtomicInteger(0);
        String result = cache.compute("key1", () -> {
            llamadas.incrementAndGet();
            return "valor";
        });
        assertEquals("valor", result);
        assertEquals(1, llamadas.get());
    }

    @Test
    void compute_segundoAcceso_noEjecutaSupplier() {
        AtomicInteger llamadas = new AtomicInteger(0);
        cache.compute("key2", () -> { llamadas.incrementAndGet(); return "primero"; });
        String second = cache.compute("key2", () -> { llamadas.incrementAndGet(); return "segundo"; });
        assertEquals("primero", second); // devuelve valor cacheado
        assertEquals(1, llamadas.get()); // supplier solo llamado una vez
    }

    @Test
    void invalidate_limpiaTodoElCache() {
        AtomicInteger llamadas = new AtomicInteger(0);
        cache.compute("a", () -> { llamadas.incrementAndGet(); return "A"; });
        cache.compute("b", () -> { llamadas.incrementAndGet(); return "B"; });
        assertEquals(2, llamadas.get());

        cache.invalidate();

        cache.compute("a", () -> { llamadas.incrementAndGet(); return "A2"; });
        cache.compute("b", () -> { llamadas.incrementAndGet(); return "B2"; });
        assertEquals(4, llamadas.get()); // se llamó de nuevo tras invalidar
    }

    @Test
    void invalidateByPrefix_soloEliminaClavesCoincidenConPrefijo() {
        AtomicInteger llamadasKpi = new AtomicInteger(0);
        AtomicInteger llamadasSeries = new AtomicInteger(0);

        cache.compute("kpis:hoy", () -> { llamadasKpi.incrementAndGet(); return "kpi"; });
        cache.compute("series:mes", () -> { llamadasSeries.incrementAndGet(); return "serie"; });

        cache.invalidateByPrefix("kpis:");

        cache.compute("kpis:hoy", () -> { llamadasKpi.incrementAndGet(); return "kpi2"; });
        cache.compute("series:mes", () -> { llamadasSeries.incrementAndGet(); return "serie2"; });

        assertEquals(2, llamadasKpi.get());    // kpis fue invalidado → se llama de nuevo
        assertEquals(1, llamadasSeries.get()); // series no fue invalidado → hit
    }

    @Test
    void compute_clavesDistintasSonIndependientes() {
        String v1 = cache.compute("x", () -> "valorX");
        String v2 = cache.compute("y", () -> "valorY");
        assertNotEquals(v1, v2);
        assertEquals("valorX", cache.compute("x", () -> "otro"));
    }
}
