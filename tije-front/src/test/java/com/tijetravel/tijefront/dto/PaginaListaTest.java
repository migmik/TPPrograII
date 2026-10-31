package com.tijetravel.tijefront.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PaginaListaTest {

    @Test
    void cortaLaListaEnPaginasDeVeinte() {
        List<Integer> numeros = numerosHasta(45);

        PaginaLista<Integer> primera = new PaginaLista<>(numeros, 0);
        PaginaLista<Integer> ultima = new PaginaLista<>(numeros, 2);

        assertEquals(3, primera.getTotalPaginas());
        assertEquals(20, primera.getElementos().size());
        assertEquals(1, primera.getElementos().get(0));
        assertFalse(primera.isHayAnterior());
        assertTrue(primera.isHaySiguiente());
        assertEquals(5, ultima.getElementos().size());
        assertEquals(41, ultima.getElementos().get(0));
        assertTrue(ultima.isHayAnterior());
        assertFalse(ultima.isHaySiguiente());
    }

    @Test
    void unaPaginaQueNoExisteMuestraLaMasCercana() {
        List<Integer> numeros = numerosHasta(25);

        assertEquals(1, new PaginaLista<>(numeros, 9).getPagina());
        assertEquals(0, new PaginaLista<>(numeros, -3).getPagina());
    }

    @Test
    void unaListaVaciaTieneUnaSolaPagina() {
        PaginaLista<Integer> vacia = new PaginaLista<>(new ArrayList<Integer>(), 0);

        assertEquals(1, vacia.getTotalPaginas());
        assertTrue(vacia.getElementos().isEmpty());
        assertFalse(vacia.isHaySiguiente());
    }

    private List<Integer> numerosHasta(int cantidad) {
        List<Integer> numeros = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            numeros.add(i);
        }
        return numeros;
    }
}
