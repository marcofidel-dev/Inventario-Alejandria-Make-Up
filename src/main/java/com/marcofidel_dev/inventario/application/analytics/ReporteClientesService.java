package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReporteClientesService {

    List<TopClienteDTO> getTopClientes(LocalDate desde, LocalDate hasta, int limit);

    ClienteDetalleAnalyticsDTO getAnalyticsCliente(Long clienteId);

    List<ClienteInactivoDTO> getClientesInactivos(int diasSinComprar);
}
