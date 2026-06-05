package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReporteCajaService {

    List<SesionCajaResumenDTO> listarSesiones(LocalDate desde, LocalDate hasta);

    SesionCajaDetalleDTO getDetalleSesion(Long sesionId);

    DescuadresResumenDTO getResumenDescuadres(LocalDate desde, LocalDate hasta);
}
