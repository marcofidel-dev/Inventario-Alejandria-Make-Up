package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReporteInventarioService {

    ValoracionInventarioDTO getValoracionActual();

    List<InventarioPorValorDTO> getInventarioPorValor();

    List<RotacionProductoDTO> getRotacion(LocalDate desde, LocalDate hasta);

    List<MargenProductoDTO> getMargenes();

    List<StockPorCategoriaDTO> getStockPorCategoria();
}
