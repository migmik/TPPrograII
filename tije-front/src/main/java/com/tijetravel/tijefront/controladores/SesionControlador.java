package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.formularios.IniciarSesionFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class SesionControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final CsrfTokenRepository tokensFormularios;

    public SesionControlador(AutenticacionApiCliente autenticacionApi, CsrfTokenRepository tokensFormularios) {
        this.autenticacionApi = autenticacionApi;
        this.tokensFormularios = tokensFormularios;
    }

    @GetMapping("/login")
    public String formulario(Model modelo) {
        modelo.addAttribute("credenciales", new IniciarSesionFormulario());
        return "sesion/login";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @Valid @ModelAttribute("credenciales") IniciarSesionFormulario formulario,
            BindingResult errores, HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession();
        if (sesion.getAttribute("usuarioActual") != null) {
            return "redirect:/cuenta";
        }
        if (errores.hasErrors()) {
            formulario.setContrasenia("");
            return "sesion/login";
        }
        try {
            SesionRespuesta usuario = autenticacionApi.iniciarSesion(
                    formulario.getNombreUsuario().trim(), formulario.getContrasenia());
            solicitud.changeSessionId();
            tokensFormularios.saveToken(null, solicitud, respuesta);
            sesion.setAttribute("usuarioActual", usuario);
            return "redirect:/cuenta";
        } catch (HttpClientErrorException.Unauthorized error) {
            respuesta.setStatus(401);
            modelo.addAttribute("errorIngreso", "El usuario o la contraseña son incorrectos.");
        } catch (RestClientException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorIngreso", "No pudimos iniciar sesión en este momento. Intentá nuevamente.");
        } finally {
            formulario.setContrasenia("");
        }
        return "sesion/login";
    }

    @GetMapping("/cuenta")
    public String cuenta(HttpServletRequest solicitud, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) {
            return "redirect:/login";
        }
        try {
            // La pantalla privada confirma que la sesión sigue vigente en el backend.
            SesionRespuesta usuario = autenticacionApi.obtenerSesion();
            sesion.setAttribute("usuarioActual", usuario);
            modelo.addAttribute("usuarioActual", usuario);
            return "sesion/cuenta";
        } catch (HttpClientErrorException.Unauthorized error) {
            sesion.invalidate();
            return "redirect:/login?sesionVencida";
        }
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(false);
        boolean salidaConfirmada = true;
        try {
            if (sesion != null && sesion.getAttribute("usuarioActual") != null) {
                autenticacionApi.cerrarSesion();
            }
        } catch (RestClientException error) {
            salidaConfirmada = false;
        } finally {
            if (sesion != null) {
                sesion.invalidate();
            }
        }
        return salidaConfirmada ? "redirect:/login?salida" : "redirect:/login?salidaLocal";
    }
}
