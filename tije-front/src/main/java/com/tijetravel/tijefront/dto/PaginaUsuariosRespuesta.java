package com.tijetravel.tijefront.dto;

import java.util.List;

public record PaginaUsuariosRespuesta(List<UsuarioRespuesta> elementos, int pagina,
        int totalPaginas, boolean hayAnterior, boolean haySiguiente) {
}
