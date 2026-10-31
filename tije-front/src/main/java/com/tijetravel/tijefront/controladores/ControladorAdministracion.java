package com.tijetravel.tijefront.controladores;

import org.springframework.ui.Model;
import org.springframework.web.client.RestClientResponseException;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Base de las pantallas de gestion (hoteles, vuelos, sucursales, usuarios y
 * roles). Junta el control de acceso y el manejo de errores de la API que antes
 * estaba copiado en cada controlador. Cada pantalla dice que permiso pide.
 */
public abstract class ControladorAdministracion {
    protected final AutenticacionApiCliente autenticacionApi;

    protected ControladorAdministracion(AutenticacionApiCliente autenticacionApi) {
        this.autenticacionApi = autenticacionApi;
    }

    /** Nombre de lo que administra la pantalla, para los mensajes. Por ejemplo "hoteles". */
    protected abstract String recursoGestionado();

    /** Permiso que hace falta para usar la pantalla. Por ejemplo "ADMINISTRAR_HOTELES". */
    protected abstract String permisoRequerido();

    /** Mensaje cuando la API responde 403. Cada pantalla lo puede cambiar. */
    protected String mensajeOperacionNoPermitida() {
        return "La API no permite esta operación. Revisá tus permisos y las reservas relacionadas.";
    }

    /**
     * Devuelve la vista a mostrar si el usuario no puede entrar (login o error),
     * o null si tiene el permiso y la sesion vigente. Los permisos se vuelven a
     * pedir a la API para tomar los cambios de rol al instante.
     */
    protected String comprobarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) {
            return "redirect:/login";
        }
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!usuario.tienePermiso(permisoRequerido())) {
            respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
            modelo.addAttribute("mensaje", "No tenés permiso para gestionar " + recursoGestionado() + ".");
            return "error";
        }
        return null;
    }

    /** Muestra en el formulario los errores esperables de la API; los demas se propagan. */
    protected String mostrarError(RestClientResponseException error, HttpServletResponse respuesta, Model modelo,
            String vista, String mensajeConflicto) {
        int estado = error.getStatusCode().value();
        String mensaje;
        if (estado == 400) {
            mensaje = "La API rechazó los datos. Revisá los campos.";
        } else if (estado == 403) {
            mensaje = mensajeOperacionNoPermitida();
        } else if (estado == 409) {
            mensaje = mensajeConflicto;
        } else {
            throw error;
        }
        respuesta.setStatus(estado);
        modelo.addAttribute("errorGestion", mensaje);
        return vista;
    }

    /** La API no respondio a tiempo: no se sabe si la operacion se hizo o no. */
    protected String mostrarConexionIncierta(HttpServletResponse respuesta, Model modelo, String vista) {
        respuesta.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        modelo.addAttribute("errorGestion",
                "No pudimos confirmar la operación. Consultá el listado antes de intentarlo nuevamente.");
        return vista;
    }
}
