package com.tijetravel.tijefront.dto;

import java.util.List;

public record PaginaTuristasRespuesta(List<TuristaRespuesta> elementos, int pagina,
        int totalPaginas, boolean hayAnterior, boolean haySiguiente) {
}
