package com.marcofidel_dev.inventario.infrastructure.persistence;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * JPA converter that enforces scale=0 on all monetary columns.
 * Silently rounds any stored decimal to the nearest whole peso.
 * Apply with @Convert(converter = MoneyConverter.class) on every monetary field.
 */
@Converter
@Slf4j
public class MoneyConverter implements AttributeConverter<BigDecimal, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) return null;
        BigDecimal normalized = MoneyCOP.normalize(attribute);
        if (attribute.scale() > 0 && attribute.stripTrailingZeros().scale() > 0) {
            log.warn("Monetary value stored with decimals — rounding {} → {}", attribute, normalized);
        }
        return normalized;
    }

    @Override
    public BigDecimal convertToEntityAttribute(BigDecimal dbData) {
        return MoneyCOP.normalize(dbData);
    }
}
