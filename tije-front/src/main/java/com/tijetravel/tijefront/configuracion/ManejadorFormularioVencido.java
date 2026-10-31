package com.tijetravel.tijefront.configuracion;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Se ejecuta cuando un formulario llega con el token CSRF vencido o invalido:
 * muestra la pagina de error en vez de la respuesta por defecto de Spring.
 */
public class ManejadorFormularioVencido implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest solicitud, HttpServletResponse respuesta,
            AccessDeniedException excepcion) throws IOException, ServletException {
        respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
        solicitud.setAttribute("mensaje",
                "El formulario venció o no es válido. Volvé a abrir la página e intentá nuevamente.");
        solicitud.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(solicitud, respuesta);
    }
}
