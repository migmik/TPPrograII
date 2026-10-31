package com.tijetravel.tijefront.controladores;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Reemplaza el valor de un parametro de un formulario ya armado. Se usa en los
 * tests para mandar un dato invalido sin repetir todo el formulario.
 */
class CambiarParametro implements RequestPostProcessor {
    private final String nombre;
    private final String valor;

    CambiarParametro(String nombre, String valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    @Override
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest solicitud) {
        solicitud.setParameter(nombre, valor);
        return solicitud;
    }
}
