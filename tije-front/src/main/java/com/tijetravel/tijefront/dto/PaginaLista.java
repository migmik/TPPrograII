package com.tijetravel.tijefront.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Corta una lista completa en paginas de 20, para los listados que la API
 * devuelve enteros (sucursales, roles y dados de baja). Si se pide una pagina
 * que no existe se muestra la mas cercana.
 */
public class PaginaLista<T> {
    public static final int TAMANIO = 20;

    private final List<T> elementos;
    private final int pagina;
    private final int totalPaginas;

    public PaginaLista(List<T> todos, int paginaPedida) {
        int paginas = (todos.size() + TAMANIO - 1) / TAMANIO;
        if (paginas < 1) {
            paginas = 1;
        }
        int actual = paginaPedida;
        if (actual < 0) {
            actual = 0;
        }
        if (actual > paginas - 1) {
            actual = paginas - 1;
        }
        int desde = actual * TAMANIO;
        int hasta = Math.min(desde + TAMANIO, todos.size());
        this.elementos = new ArrayList<>(todos.subList(desde, hasta));
        this.pagina = actual;
        this.totalPaginas = paginas;
    }

    public List<T> getElementos() {
        return elementos;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public boolean isHayAnterior() {
        return pagina > 0;
    }

    public boolean isHaySiguiente() {
        return pagina < totalPaginas - 1;
    }
}
