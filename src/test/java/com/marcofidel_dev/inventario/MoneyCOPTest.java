package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.shared.money.InvalidMoneyFormatException;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyCOPTest {

    // ─── format() ─────────────────────────────────────────────────────────────

    @Test
    void format_standardAmount() {
        assertThat(MoneyCOP.format(bd(15000))).isEqualTo("$15.000");
    }

    @Test
    void format_millions() {
        assertThat(MoneyCOP.format(bd(1500000))).isEqualTo("$1.500.000");
    }

    @Test
    void format_null_returnsZero() {
        assertThat(MoneyCOP.format(null)).isEqualTo("$0");
    }

    @Test
    void format_zero() {
        assertThat(MoneyCOP.format(BigDecimal.ZERO)).isEqualTo("$0");
    }

    @Test
    void format_roundsDecimalsBeforeFormatting() {
        // 15000.789 → 15001 → "$15.001"
        assertThat(MoneyCOP.format(new BigDecimal("15000.789"))).isEqualTo("$15.001");
    }

    @Test
    void format_negativeAmount() {
        assertThat(MoneyCOP.format(bd(-500))).isEqualTo("-$500");
    }

    @Test
    void format_smallAmount_noThousandsSeparator() {
        assertThat(MoneyCOP.format(bd(500))).isEqualTo("$500");
    }

    @Test
    void format_exactThousand() {
        assertThat(MoneyCOP.format(bd(1000))).isEqualTo("$1.000");
    }

    // ─── formatPlain() ────────────────────────────────────────────────────────

    @Test
    void formatPlain_removesSymbol() {
        assertThat(MoneyCOP.formatPlain(bd(15000))).isEqualTo("15.000");
    }

    @Test
    void formatPlain_negative() {
        assertThat(MoneyCOP.formatPlain(bd(-500))).isEqualTo("-500");
    }

    // ─── parse() ─────────────────────────────────────────────────────────────

    @Test
    void parse_rawNumber() {
        assertThat(MoneyCOP.parse("15000")).isEqualByComparingTo("15000");
    }

    @Test
    void parse_withThousandsSeparator() {
        assertThat(MoneyCOP.parse("15.000")).isEqualByComparingTo("15000");
    }

    @Test
    void parse_withDollarSign() {
        assertThat(MoneyCOP.parse("$15.000")).isEqualByComparingTo("15000");
    }

    @Test
    void parse_withLeadingTrailingSpaces() {
        assertThat(MoneyCOP.parse(" 15000 ")).isEqualByComparingTo("15000");
    }

    @Test
    void parse_withCommaAsThousands() {
        assertThat(MoneyCOP.parse("15,000")).isEqualByComparingTo("15000");
    }

    @Test
    void parse_blank_returnsZero() {
        assertThat(MoneyCOP.parse("")).isEqualByComparingTo("0");
        assertThat(MoneyCOP.parse("   ")).isEqualByComparingTo("0");
    }

    @Test
    void parse_null_returnsZero() {
        assertThat(MoneyCOP.parse(null)).isEqualByComparingTo("0");
    }

    @Test
    void parse_letters_throwsInvalidMoneyFormatException() {
        assertThatThrownBy(() -> MoneyCOP.parse("abc"))
                .isInstanceOf(InvalidMoneyFormatException.class)
                .hasMessageContaining("Formato de monto inválido");
    }

    @Test
    void parse_negative_throwsInvalidMoneyFormatException() {
        assertThatThrownBy(() -> MoneyCOP.parse("-100"))
                .isInstanceOf(InvalidMoneyFormatException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void parse_onlyDollarSign_returnsZero() {
        assertThat(MoneyCOP.parse("$")).isEqualByComparingTo("0");
    }

    // ─── scale invariant ─────────────────────────────────────────────────────

    @Test
    void parse_resultHasScaleZero() {
        BigDecimal result = MoneyCOP.parse("15.000");
        assertThat(result.scale()).isZero();
    }

    @Test
    void normalize_scaleZero() {
        BigDecimal result = MoneyCOP.normalize(new BigDecimal("15000.50"));
        assertThat(result.scale()).isZero();
        assertThat(result).isEqualByComparingTo("15001");
    }

    // ─── applyPercentage() ────────────────────────────────────────────────────

    @Test
    void applyPercentage_tenPercent() {
        // 10% of $15.000 = $1.500 (not $1.500,10)
        assertThat(MoneyCOP.applyPercentage(bd(15000), new BigDecimal("10")))
                .isEqualByComparingTo("1500");
    }

    @Test
    void applyPercentage_fractionalPercent_roundsToWholePeso() {
        // 10% of $15.001 → 1500.1 → $1.500
        assertThat(MoneyCOP.applyPercentage(bd(15001), new BigDecimal("10")))
                .isEqualByComparingTo("1500");
    }

    @Test
    void applyPercentage_nullPercent_returnsBase() {
        assertThat(MoneyCOP.applyPercentage(bd(15000), null))
                .isEqualByComparingTo("15000");
    }

    @Test
    void applyPercentage_zeroPercent_returnsZero() {
        assertThat(MoneyCOP.applyPercentage(bd(15000), BigDecimal.ZERO))
                .isEqualByComparingTo("0");
    }

    // ─── multiply() ──────────────────────────────────────────────────────────

    @Test
    void multiply_quantity() {
        assertThat(MoneyCOP.multiply(bd(15000), 3)).isEqualByComparingTo("45000");
    }

    @Test
    void multiply_zero() {
        assertThat(MoneyCOP.multiply(bd(15000), 0)).isEqualByComparingTo("0");
    }

    // ─── subtract() ──────────────────────────────────────────────────────────

    @Test
    void subtract_simpleCase() {
        assertThat(MoneyCOP.subtract(bd(15000), bd(1500))).isEqualByComparingTo("13500");
    }

    @Test
    void subtract_resultCanBeNegative() {
        assertThat(MoneyCOP.subtract(bd(1000), bd(2000))).isEqualByComparingTo("-1000");
    }

    // ─── add() ───────────────────────────────────────────────────────────────

    @Test
    void add_multipleValues() {
        assertThat(MoneyCOP.add(bd(1000), bd(2000), bd(500))).isEqualByComparingTo("3500");
    }

    @Test
    void add_withNull_treatedAsZero() {
        assertThat(MoneyCOP.add(bd(1000), null)).isEqualByComparingTo("1000");
    }

    // ─── marginPercentage() ──────────────────────────────────────────────────

    @Test
    void marginPercentage_standard() {
        // ((15000 - 8000) / 15000) * 100 = 46.666... → 46.7
        assertThat(MoneyCOP.marginPercentage(bd(15000), bd(8000)))
                .isEqualByComparingTo("46.7");
    }

    @Test
    void marginPercentage_zeroCost() {
        // 100% margin
        assertThat(MoneyCOP.marginPercentage(bd(15000), bd(0)))
                .isEqualByComparingTo("100.0");
    }

    @Test
    void marginPercentage_zeroPriceReturnsZero() {
        assertThat(MoneyCOP.marginPercentage(bd(0), bd(5000)))
                .isEqualByComparingTo("0");
    }

    // ─── MoneyConverter ──────────────────────────────────────────────────────

    @Test
    void converter_roundsToWholeOnWrite() {
        var converter = new com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter();
        BigDecimal stored = converter.convertToDatabaseColumn(new BigDecimal("15000.789"));
        assertThat(stored).isEqualByComparingTo("15001");
        assertThat(stored.scale()).isZero();
    }

    @Test
    void converter_roundsOnRead() {
        var converter = new com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter();
        BigDecimal read = converter.convertToEntityAttribute(new BigDecimal("15000.20"));
        assertThat(read).isEqualByComparingTo("15000");
        assertThat(read.scale()).isZero();
    }

    @Test
    void converter_nullSafeOnWrite() {
        var converter = new com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void converter_nullSafeOnRead() {
        var converter = new com.marcofidel_dev.inventario.infrastructure.persistence.MoneyConverter();
        assertThat(converter.convertToEntityAttribute(null)).isEqualByComparingTo("0");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private static BigDecimal bd(long value) {
        return BigDecimal.valueOf(value).setScale(0);
    }
}
