package com.tijetravel.tijefront.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(RestClientException.class)
    public ModelAndView errorApi(RestClientException excepcion, HttpServletRequest solicitud) {
        if (excepcion instanceof RestClientResponseException respuesta) {
            if (respuesta.getStatusCode().value() == 401) {
                HttpSession sesion = solicitud.getSession(false);
                if (sesion != null) {
                    sesion.invalidate();
                }
                return new ModelAndView("redirect:/login?sesionVencida");
            }
            if (respuesta.getStatusCode().value() == 403) {
                return pagina(HttpStatus.FORBIDDEN, "No tenés permiso para realizar esta operación.");
            }
            if (respuesta.getStatusCode().value() == 404) {
                return pagina(HttpStatus.NOT_FOUND, "No encontramos el recurso solicitado.");
            }
            if (respuesta.getStatusCode().value() == 400) {
                return pagina(HttpStatus.BAD_REQUEST, "Revisá los datos de la consulta.");
            }
        }
        return pagina(HttpStatus.SERVICE_UNAVAILABLE,
                "No pudimos consultar los datos en este momento. Intentá nuevamente en unos minutos.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView parametroInvalido() {
        return pagina(HttpStatus.BAD_REQUEST, "El identificador debe ser un número entero.");
    }

    private ModelAndView pagina(HttpStatus estado, String mensaje) {
        ModelAndView vista = new ModelAndView("error");
        vista.setStatus(estado);
        vista.addObject("mensaje", mensaje);
        return vista;
    }
}
