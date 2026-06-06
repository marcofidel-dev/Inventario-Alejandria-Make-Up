package com.marcofidel_dev.inventario.ui.common;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.math.BigDecimal;

/**
 * TableCell that displays BigDecimal values as Colombian Peso format ("$15.000").
 * Right-aligned, consistent with accounting convention.
 *
 * Usage:
 *   colPrecio.setCellFactory(MoneyTableCell.factory());
 */
public class MoneyTableCell<S> extends TableCell<S, BigDecimal> {

    public MoneyTableCell() {
        setAlignment(Pos.CENTER_RIGHT);
    }

    @Override
    protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : MoneyCOP.format(item));
    }

    public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> factory() {
        return col -> new MoneyTableCell<>();
    }
}
