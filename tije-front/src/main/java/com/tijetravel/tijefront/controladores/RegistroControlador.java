package com.tijetravel.tijefront.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tijetravel.tijefront.clientes.RegistroApiCliente;
import com.tijetravel.tijefront.clientes.SucursalesApiCliente;
import com.tijetravel.tijefront.formularios.RegistroClienteFormulario;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class RegistroControlador {
    private final RegistroApiCliente registroApi;
    private final SucursalesApiCliente sucursalesApi;

    public RegistroControlador(RegistroApiCliente registroApi, SucursalesApiCliente sucursalesApi) {
        this.registroApi = registroApi;
        this.sucursalesApi = sucursalesApi;
    }

    @GetMapping("/registro")
    public String formulario(Model modelo) {
        modelo.addAttribute("registro", new RegistroClienteFormulario());
        modelo.addAttribute("sucursales", sucursalesApi.listar());
        return "sesion/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroClienteFormulario formulario,
            BindingResult errores, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        modelo.addAttribute("sucursales", sucursalesApi.listar());
        if (errores.hasErrors()) {
            formulario.setContrasenia("");
            return "sesion/registro";
        }
        try {
            registroApi.registrar(formulario);
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado != 400 && estado != 409) throw error;
            respuesta.setStatus(estado);
            modelo.addAttribute("errorRegistro", estado == 409
                    ? "Ese nombre de usuario, DNI o email ya está registrado."
                    : "Revisá los datos y la sucursal elegida.");
            formulario.setContrasenia("");
            return "sesion/registro";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            modelo.addAttribute("errorRegistro", "No pudimos completar el registro. Intentá nuevamente.");
            formulario.setContrasenia("");
            return "sesion/registro";
        }
        formulario.setContrasenia("");
        redireccion.addFlashAttribute("registroExitoso", true);
        return "redirect:/login";
    }
}