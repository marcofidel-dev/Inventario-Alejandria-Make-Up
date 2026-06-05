package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReporteVentasService {

    ReporteVentasPeriodoDTO generarReporte(FiltroReporteDTO filtro);

    List<VentaResumenDTO> listarVentasFiltradas(FiltroReporteDTO filtro);

    List<DesglosePagoDTO> getDesglosePorMetodo(LocalDate desde, LocalDate hasta);

    List<DesgloseUsuarioDTO> getDesglosePorUsuario(LocalDate desde, LocalDate hasta);

    AnalisisABCDTO getAnalisisABC(LocalDate desde, LocalDate hasta);
}
