package com.tijetravel.tijeback.api.dto;

import java.util.List;

public record PaginaRespuesta<T>(
        List<T> elementos,
        int pagina,
        int totalPaginas,
        boolean hayAnterior,
        boolean haySiguiente) {
}