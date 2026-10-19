package com.tijetravel.tijefront.dto;

import java.util.List;

public record PaginaVuelosRespuesta(List<VueloRespuesta> elementos, int pagina,
        int totalPaginas, boolean hayAnterior, boolean haySiguiente) {
}
