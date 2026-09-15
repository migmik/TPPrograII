package com.tijetravel.tijefront.controladores;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.tijetravel.tijefront.dto.SesionRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class DatosNavegacion {
    @ModelAttribute("usuarioActual")
    public SesionRespuesta usuarioActual(HttpServletRequest solicitud, HttpServletResponse respuesta) {
        // Las páginas pueden contener datos del usuario y tokens de formularios.
        respuesta.setHeader("Cache-Control", "no-store");
        HttpSession sesion = solicitud.getSession(false);
        return sesion == null ? null : (SesionRespuesta) sesion.getAttribute("usuarioActual");
    }
}
