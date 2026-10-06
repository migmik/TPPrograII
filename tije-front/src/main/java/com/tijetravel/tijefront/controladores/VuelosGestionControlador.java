package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.clientes.VuelosGestionApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.formularios.GuardarVueloFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/vuelos")
public class VuelosGestionControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final VuelosApiCliente vuelosApi;
    private final VuelosGestionApiCliente gestionApi;

    public VuelosGestionControlador(AutenticacionApiCliente autenticacionApi,
            VuelosApiCliente vuelosApi, VuelosGestionApiCliente gestionApi) {
        this.autenticacionApi = autenticacionApi;
        this.vuelosApi = vuelosApi;
        this.gestionApi = gestionApi;
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("vuelo", new GuardarVueloFormulario());
        modelo.addAttribute("titulo", "Nuevo vuelo");
        return "vuelos/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("vuelo") GuardarVueloFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("titulo", "Nuevo vuelo");
        validarPlazas(formulario, errores);
        if (errores.hasErrors()) return "vuelos/formulario";
        try {
            gestionApi.crear(formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "vuelos/formulario",
                    "No se pudo crear: el número ya existe o las plazas no son válidas.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "vuelos/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Vuelo creado correctamente.");
        return "redirect:/vuelos";
    }

    @GetMapping("/{numero}/editar")
    public String editar(@PathVariable Integer numero, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        VueloRespuesta actual = vuelosApi.buscar(numero);
        GuardarVueloFormulario formulario = new GuardarVueloFormulario();
        formulario.setNumero(actual.getNumero());
        formulario.setFechaYHora(actual.getFechaYHora());
        formulario.setOrigen(actual.getOrigen());
        formulario.setDestino(actual.getDestino());
        formulario.setTotalPlazas(actual.getTotalPlazas());
        formulario.setPlazasTurista(actual.getPlazasTurista());
        formulario.setPlazasPrimera(actual.getPlazasPrimera());
        modelo.addAttribute("vuelo", formulario);
        modelo.addAttribute("numero", numero);
        modelo.addAttribute("titulo", "Editar vuelo");
        return "vuelos/formulario";
    }

    @PostMapping("/{numero}/editar")
    public String modificar(@PathVariable Integer numero,
            @Valid @ModelAttribute("vuelo") GuardarVueloFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("numero", numero);
        modelo.addAttribute("titulo", "Editar vuelo");
        // El número es parte de la URL: no es editable y no viene del formulario.
        formulario.setNumero(numero);
        validarPlazas(formulario, errores);
        if (errores.hasErrors()) return "vuelos/formulario";
        try {
            gestionApi.modificar(numero, formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "vuelos/formulario",
                    "No se pudo modificar: las plazas, la fecha o el destino no son compatibles con las reservas existentes.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "vuelos/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Vuelo actualizado correctamente.");
        return "redirect:/vuelos";
    }

    @GetMapping("/{numero}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer numero, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("vuelo", vuelosApi.buscar(numero));
        return "vuelos/eliminar";
    }

    @PostMapping("/{numero}/eliminar")
    public String eliminar(@PathVariable Integer numero, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("vuelo", vuelosApi.buscar(numero));
        try {
            gestionApi.eliminar(numero);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "vuelos/eliminar",
                    "No se puede eliminar un vuelo que tiene reservas.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "vuelos/eliminar");
        }
        redireccion.addFlashAttribute("mensajeExito", "Vuelo eliminado correctamente.");
        return "redirect:/vuelos";
    }

    private void validarPlazas(GuardarVueloFormulario formulario, BindingResult errores) {
        if (formulario.getTotalPlazas() != null && formulario.getPlazasTurista() != null
                && formulario.getPlazasPrimera() != null
                && (long) formulario.getPlazasTurista() + formulario.getPlazasPrimera() > formulario.getTotalPlazas()) {
            errores.rejectValue("totalPlazas", "plazas.excedidas",
                    "La suma de plazas turista y primera no puede superar el total.");
        }
    }

    private String comprobarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) return "redirect:/login";
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo un administrador puede gestionar vuelos.");
            return "error";
        }
        return null;
    }

    private String mostrarError(RestClientResponseException error, HttpServletResponse respuesta, Model modelo,
            String vista, String mensajeConflicto) {
        int estado = error.getStatusCode().value();
        if (estado != 400 && estado != 403 && estado != 409) throw error;
        respuesta.setStatus(estado);
        modelo.addAttribute("errorGestion", estado == 400 ? "La API rechazó los datos. Revisá los campos."
                : estado == 403 ? "La API no permite esta operación. Revisá tus permisos y las reservas relacionadas."
                : mensajeConflicto);
        return vista;
    }

    private String mostrarConexionIncierta(HttpServletResponse respuesta, Model modelo, String vista) {
        respuesta.setStatus(503);
        modelo.addAttribute("errorGestion", "No pudimos confirmar la operación. Consultá el listado antes de intentarlo nuevamente.");
        return vista;
    }
}
