package com.tijetravel.tijefront.dto;

import java.util.List;

public record PaginaReservasRespuesta(
        List<ReservaRespuesta> elementos,
        int pagina,
        int totalPaginas,
        boolean hayAnterior,
        boolean haySiguiente) {
}