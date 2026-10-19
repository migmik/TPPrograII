package com.tijetravel.tijefront.dto;

import java.util.List;

public record PaginaHotelesRespuesta(List<HotelRespuesta> elementos, int pagina,
        int totalPaginas, boolean hayAnterior, boolean haySiguiente) {
}
