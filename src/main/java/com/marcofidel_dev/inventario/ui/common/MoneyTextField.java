package com.marcofidel_dev.inventario.ui.common;

import com.marcofidel_dev.inventario.shared.money.InvalidMoneyFormatException;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * TextField that enforces Colombian Peso input rules:
 * - Accepts only digits while typing (no letters, dots, commas)
 * - Formats with thousands separators on focus loss ("15.000")
 * - Strips separators on focus gain for comfortable editing
 * - Exposes getAmount() / setAmount(BigDecimal)
 */
public class MoneyTextField extends TextField {

    private static final Pattern DIGITS_ONLY = Pattern.compile("[0-9]*");

    public MoneyTextField() {
        setPromptText("$0");
        setAlignment(Pos.CENTER_RIGHT);
        installDigitFilter();
        installFocusListeners();
    }

    private void installDigitFilter() {
        setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText()
                    .replace(".", "")
                    .replace(",", "");
            return DIGITS_ONLY.matcher(newText).matches() ? change : null;
        }));
    }

    private void installFocusListeners() {
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            String raw = getText();
            if (isNowFocused) {
                // Strip separators so the user can edit plain digits
                if (!raw.isBlank()) {
                    setText(raw.replace(".", ""));
                    selectAll();
                }
            } else {
                // Format with thousands separators on blur
                if (!raw.isBlank()) {
                    try {
                        BigDecimal value = MoneyCOP.parse(raw);
                        setText(value.signum() == 0 ? "" : MoneyCOP.formatPlain(value));
                    } catch (InvalidMoneyFormatException e) {
                        // Leave as-is; validation happens on submit
                    }
                }
            }
        });
    }

    /** Returns the current amount. Returns ZERO for blank or unparseable input. */
    public BigDecimal getAmount() {
        String raw = getText();
        if (raw == null || raw.isBlank()) return MoneyCOP.ZERO;
        try {
            return MoneyCOP.parse(raw);
        } catch (InvalidMoneyFormatException e) {
            return MoneyCOP.ZERO;
        }
    }

    /** Sets the field value. Clears the field if amount is null or zero. */
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() == 0) {
            setText("");
        } else {
            setText(MoneyCOP.formatPlain(amount));
        }
    }
}
