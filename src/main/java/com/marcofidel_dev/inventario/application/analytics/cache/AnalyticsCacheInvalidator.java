package com.marcofidel_dev.inventario.application.analytics.cache;

import com.marcofidel_dev.inventario.application.analytics.event.VentaRegistradaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsCacheInvalidator {

    private final AnalyticsCache analyticsCache;

    @EventListener
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        analyticsCache.invalidateByPrefix("kpis:");
        analyticsCache.invalidateByPrefix("series:");
        analyticsCache.invalidateByPrefix("top:");
        analyticsCache.invalidateByPrefix("hora:");
        analyticsCache.invalidateByPrefix("alertas");
        log.debug("Cache invalidated after sale id={}", event.getSale().getId());
    }
}
