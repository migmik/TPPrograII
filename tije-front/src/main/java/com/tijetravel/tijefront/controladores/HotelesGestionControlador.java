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
import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.HotelesGestionApiCliente;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.formularios.GuardarHotelFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/hoteles")
public class HotelesGestionControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final HotelesApiCliente hotelesApi;
    private final HotelesGestionApiCliente gestionApi;

    public HotelesGestionControlador(AutenticacionApiCliente autenticacionApi,
            HotelesApiCliente hotelesApi, HotelesGestionApiCliente gestionApi) {
        this.autenticacionApi = autenticacionApi;
        this.hotelesApi = hotelesApi;
        this.gestionApi = gestionApi;
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("hotel", new GuardarHotelFormulario());
        modelo.addAttribute("titulo", "Nuevo hotel");
        return "hoteles/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("hotel") GuardarHotelFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("titulo", "Nuevo hotel");
        if (errores.hasErrors())
            return "hoteles/formulario";
        try {
            gestionApi.crear(formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "hoteles/formulario",
                    "No se pudo crear: ya existe un hotel con ese nombre en esa ciudad.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "hoteles/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Hotel creado correctamente.");
        return "redirect:/hoteles";
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        HotelRespuesta actual = hotelesApi.buscar(codigo);
        GuardarHotelFormulario formulario = new GuardarHotelFormulario();
        formulario.setNombre(actual.getNombre());
        formulario.setDireccion(actual.getDireccion());
        formulario.setCiudad(actual.getCiudad());
        formulario.setTelefono(actual.getTelefono());
        formulario.setCapacidadTotal(actual.getCapacidadTotal());
        modelo.addAttribute("hotel", formulario);
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("titulo", "Editar hotel");
        return "hoteles/formulario";
    }

    @PostMapping("/{codigo}/editar")
    public String modificar(@PathVariable Integer codigo,
            @Valid @ModelAttribute("hotel") GuardarHotelFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("titulo", "Editar hotel");
        if (errores.hasErrors())
            return "hoteles/formulario";
        try {
            gestionApi.modificar(codigo, formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "hoteles/formulario",
                    "No se pudo modificar: nombre repetido, ciudad incompatible o capacidad menor a las reservas existentes.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "hoteles/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Hotel actualizado correctamente.");
        return "redirect:/hoteles";
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("hotel", hotelesApi.buscar(codigo));
        return "hoteles/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("hotel", hotelesApi.buscar(codigo));
        try {
            gestionApi.eliminar(codigo);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "hoteles/eliminar",
                    "No se puede eliminar un hotel que tiene reservas.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "hoteles/eliminar");
        }
        redireccion.addFlashAttribute("mensajeExito", "Hotel eliminado correctamente.");
        return "redirect:/hoteles";
    }

    private String comprobarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null)
            return "redirect:/login";
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo un administrador puede gestionar hoteles.");
            return "error";
        }
        return null;
    }

    private String mostrarError(RestClientResponseException error, HttpServletResponse respuesta, Model modelo,
            String vista, String mensajeConflicto) {
        int estado = error.getStatusCode().value();
        if (estado != 400 && estado != 403 && estado != 409)
            throw error;
        respuesta.setStatus(estado);
        modelo.addAttribute("errorGestion", estado == 400 ? "La API rechazó los datos. Revisá los campos."
                : estado == 403 ? "La API no permite esta operación. Revisá tus permisos y las reservas relacionadas."
                        : mensajeConflicto);
        return vista;
    }

    private String mostrarConexionIncierta(HttpServletResponse respuesta, Model modelo, String vista) {
        respuesta.setStatus(503);
        modelo.addAttribute("errorGestion",
                "No pudimos confirmar la operación. Consultá el listado antes de intentarlo nuevamente.");
        return vista;
    }
}
