package com.tijetravel.tijefront.controladores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.BajasApiCliente;
import com.tijetravel.tijefront.dto.ErrorApiRespuesta;
import com.tijetravel.tijefront.dto.FilaBaja;
import com.tijetravel.tijefront.dto.RegistroDadoDeBaja;
import com.tijetravel.tijefront.dto.SesionRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Listado de registros dados de baja de un modulo, con el boton para
 * reactivarlos. Lo ve quien administra ese modulo.
 */
@Controller
@RequestMapping("/bajas")
public class BajasControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final BajasApiCliente bajasApi;

    public BajasControlador(AutenticacionApiCliente autenticacionApi, BajasApiCliente bajasApi) {
        this.autenticacionApi = autenticacionApi;
        this.bajasApi = bajasApi;
    }

    @GetMapping("/{ruta}")
    public String listar(@PathVariable String ruta, HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo) {
        ModuloBajas modulo = ModuloBajas.desdeRuta(ruta);
        String salida = comprobarAcceso(modulo, solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        List<FilaBaja> filas = new ArrayList<>();
        for (RegistroDadoDeBaja registro : bajasApi.listar(modulo.getRuta())) {
            filas.add(new FilaBaja(registro.getIdentificador(), modulo.describir(registro),
                    ModuloBajas.formatear(registro.getFechaCreacion()),
                    ModuloBajas.formatear(registro.getFechaBaja())));
        }
        modelo.addAttribute("modulo", modulo);
        modelo.addAttribute("filas", filas);
        return "bajas/lista";
    }

    @PostMapping("/{ruta}/{codigo}/reactivar")
    public String reactivar(@PathVariable String ruta, @PathVariable Integer codigo,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo,
            RedirectAttributes redireccion) {
        ModuloBajas modulo = ModuloBajas.desdeRuta(ruta);
        String salida = comprobarAcceso(modulo, solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        try {
            bajasApi.reactivar(modulo.getRuta(), codigo);
            redireccion.addFlashAttribute("mensajeExito", "Registro " + codigo + " reactivado correctamente.");
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado == 401) {
                throw error;
            }
            redireccion.addFlashAttribute("errorReactivacion", mensajeDeLaApi(error));
        } catch (ResourceAccessException error) {
            redireccion.addFlashAttribute("errorReactivacion",
                    "No pudimos confirmar la reactivación. Revisá el listado antes de intentarlo nuevamente.");
        }
        return "redirect:/bajas/" + modulo.getRuta();
    }

    private String comprobarAcceso(ModuloBajas modulo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) {
            return "redirect:/login";
        }
        if (modulo == null) {
            respuesta.setStatus(HttpServletResponse.SC_NOT_FOUND);
            modelo.addAttribute("mensaje", "No existe ese listado de dados de baja.");
            return "error";
        }
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!usuario.tienePermiso(modulo.getPermiso())) {
            respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
            modelo.addAttribute("mensaje", "No tenés permiso para ver estos registros dados de baja.");
            return "error";
        }
        return null;
    }

    /** La API explica por que no se pudo reactivar (DNI repetido, sin lugar, titular de baja, etc.). */
    private String mensajeDeLaApi(RestClientResponseException error) {
        try {
            ErrorApiRespuesta cuerpo = error.getResponseBodyAs(ErrorApiRespuesta.class);
            if (cuerpo != null && cuerpo.getMensaje() != null) {
                return "No se pudo reactivar: " + cuerpo.getMensaje() + ".";
            }
        } catch (RuntimeException sinCuerpo) {
            // Si la respuesta no trae un JSON valido se muestra el mensaje general.
        }
        return "No se pudo reactivar el registro.";
    }
}
