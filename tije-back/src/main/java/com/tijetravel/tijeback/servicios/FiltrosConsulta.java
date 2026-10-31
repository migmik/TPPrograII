package com.tijetravel.tijeback.servicios;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Validaciones comunes de los filtros de busqueda y del paginado, para no
 * repetirlas en cada servicio.
 */
final class FiltrosConsulta {
    static final int TAMANIO_MAXIMO = 100;

    private FiltrosConsulta() {
    }

    /** Devuelve el texto sin espacios o null si esta vacio. */
    static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        if (valor.contains("%") || valor.contains("_")) {
            throw new IllegalArgumentException("El filtro contiene caracteres no permitidos");
        }
        return valor.trim();
    }

    /** Devuelve el DNI sin espacios o null si esta vacio. */
    static String dni(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String dni = valor.trim();
        if (!dni.matches("[0-9]{7,8}")) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 digitos");
        }
        return dni;
    }

    static void codigoPositivo(Integer codigo) {
        if (codigo != null && codigo < 1) {
            throw new IllegalArgumentException("Los codigos deben ser positivos");
        }
    }

    static Pageable pagina(int pagina, int tamanio, Sort orden) {
        if (pagina < 0 || tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("Pagina o tamanio no valido");
        }
        return PageRequest.of(pagina, tamanio, orden);
    }

    /**
     * Arma el orden de un catalogo: por su codigo o por precio (ascendente o
     * descendente), desempatando siempre por el codigo.
     */
    static Sort ordenCatalogo(String orden, String campoCodigo, String campoPrecio) {
        Sort porCodigo = Sort.by(campoCodigo);
        if (orden == null || orden.equals(campoCodigo)) {
            return porCodigo;
        }
        if (orden.equals("precioAsc")) {
            return Sort.by(campoPrecio).ascending().and(porCodigo);
        }
        if (orden.equals("precioDesc")) {
            return Sort.by(campoPrecio).descending().and(porCodigo);
        }
        throw new IllegalArgumentException("Orden no valido");
    }
}
