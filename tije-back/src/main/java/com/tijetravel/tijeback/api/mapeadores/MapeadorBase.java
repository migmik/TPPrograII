package com.tijetravel.tijeback.api.mapeadores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.api.dto.PaginaRespuesta;

/**
 * Base de los mapeadores de la API. Cada subclase solo define como convertir
 * un elemento y esta clase reutiliza esa conversion para listas y paginas.
 *
 * @param <E> entidad del modelo
 * @param <R> DTO de respuesta
 */
public abstract class MapeadorBase<E, R> {

    public abstract R aRespuesta(E entidad);

    public List<R> aRespuestas(List<E> entidades) {
        List<R> respuestas = new ArrayList<>();
        for (E entidad : entidades) {
            respuestas.add(aRespuesta(entidad));
        }
        return respuestas;
    }

    public PaginaRespuesta<R> aPagina(Page<E> pagina) {
        return new PaginaRespuesta<>(
                aRespuestas(pagina.getContent()),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.hasPrevious(),
                pagina.hasNext());
    }
}
