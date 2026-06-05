package com.marcofidel_dev.inventario.application.analytics.event;

import com.marcofidel_dev.inventario.domain.entity.Sale;
import org.springframework.context.ApplicationEvent;

public class VentaRegistradaEvent extends ApplicationEvent {

    private final Sale sale;

    public VentaRegistradaEvent(Object source, Sale sale) {
        super(source);
        this.sale = sale;
    }

    public Sale getSale() { return sale; }
}
