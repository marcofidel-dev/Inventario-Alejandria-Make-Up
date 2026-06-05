package com.marcofidel_dev.inventario.application.analytics.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@Slf4j
public class AnalyticsCache {

    private static final Duration TTL = Duration.ofMinutes(5);

    private record CacheEntry(Object value, Instant expiresAt) {
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T compute(String key, Supplier<T> supplier) {
        CacheEntry entry = store.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache HIT: {}", key);
            return (T) entry.value();
        }
        log.debug("Cache MISS: {}", key);
        T value = supplier.get();
        store.put(key, new CacheEntry(value, Instant.now().plus(TTL)));
        return value;
    }

    public void invalidate() {
        store.clear();
        log.debug("Analytics cache cleared");
    }

    public void invalidateByPrefix(String prefix) {
        store.keySet().removeIf(k -> k.startsWith(prefix));
        log.debug("Analytics cache keys with prefix '{}' removed", prefix);
    }
}
