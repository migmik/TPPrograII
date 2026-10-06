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
import com.tijetravel.tijefront.clientes.SucursalesApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.SucursalResumen;
import com.tijetravel.tijefront.formularios.GuardarSucursalFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/sucursales")
public class SucursalesControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final SucursalesApiCliente sucursalesApi;

    public SucursalesControlador(AutenticacionApiCliente autenticacionApi, SucursalesApiCliente sucursalesApi) {
        this.autenticacionApi = autenticacionApi;
        this.sucursalesApi = sucursalesApi;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("sucursales", sucursalesApi.listar());
        return "sucursales/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("sucursal", new GuardarSucursalFormulario());
        modelo.addAttribute("titulo", "Nueva sucursal");
        return "sucursales/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("sucursal") GuardarSucursalFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("titulo", "Nueva sucursal");
        if (errores.hasErrors()) return "sucursales/formulario";
        try {
            sucursalesApi.crear(formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "sucursales/formulario",
                    "Ya existe una sucursal con esa dirección.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "sucursales/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Sucursal creada correctamente.");
        return "redirect:/sucursales";
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        SucursalResumen actual = sucursalesApi.buscar(codigo);
        GuardarSucursalFormulario formulario = new GuardarSucursalFormulario();
        formulario.setDireccion(actual.getDireccion());
        formulario.setTelefono(actual.getTelefono());
        modelo.addAttribute("sucursal", formulario);
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("titulo", "Editar sucursal");
        return "sucursales/formulario";
    }

    @PostMapping("/{codigo}/editar")
    public String modificar(@PathVariable Integer codigo,
            @Valid @ModelAttribute("sucursal") GuardarSucursalFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("titulo", "Editar sucursal");
        if (errores.hasErrors()) return "sucursales/formulario";
        try {
            sucursalesApi.modificar(codigo, formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "sucursales/formulario",
                    "Ya existe una sucursal con esa dirección.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "sucursales/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Sucursal actualizada correctamente.");
        return "redirect:/sucursales";
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("sucursal", sucursalesApi.buscar(codigo));
        return "sucursales/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) return salida;
        modelo.addAttribute("sucursal", sucursalesApi.buscar(codigo));
        try {
            sucursalesApi.eliminar(codigo);
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() == 403) {
                respuesta.setStatus(403);
                modelo.addAttribute("errorGestion", "No se puede eliminar una sucursal vinculada a turistas o reservas.");
                return "sucursales/eliminar";
            }
            return mostrarError(error, respuesta, modelo, "sucursales/eliminar",
                    "No se puede eliminar una sucursal vinculada a turistas o reservas.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "sucursales/eliminar");
        }
        redireccion.addFlashAttribute("mensajeExito", "Sucursal eliminada correctamente.");
        return "redirect:/sucursales";
    }

    private String comprobarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) return "redirect:/login";
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo un administrador puede gestionar sucursales.");
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
                : estado == 403 ? "La API no permite esta operación. Revisá tus permisos y los datos relacionados."
                : mensajeConflicto);
        return vista;
    }

    private String mostrarConexionIncierta(HttpServletResponse respuesta, Model modelo, String vista) {
        respuesta.setStatus(503);
        modelo.addAttribute("errorGestion", "No pudimos confirmar la operación. Consultá el listado antes de intentarlo nuevamente.");
        return vista;
    }
}
