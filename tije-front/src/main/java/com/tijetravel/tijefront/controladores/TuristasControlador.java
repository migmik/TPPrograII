package com.tijetravel.tijefront.controladores;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.SucursalesApiCliente;
import com.tijetravel.tijefront.clientes.TuristasApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.TuristaRespuesta;
import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.formularios.GuardarTuristaFormulario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/turistas")
public class TuristasControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final TuristasApiCliente turistasApi;
    private final SucursalesApiCliente sucursalesApi;

    public TuristasControlador(AutenticacionApiCliente autenticacionApi,
            TuristasApiCliente turistasApi, SucursalesApiCliente sucursalesApi) {
        this.autenticacionApi = autenticacionApi;
        this.turistasApi = turistasApi;
        this.sucursalesApi = sucursalesApi;
    }

    @GetMapping
    public String listar(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(false, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("turistas", turistasApi.listarDetalles());
        return "turistas/lista";
    }

    @GetMapping("/{codigo}")
    public String detalle(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(false, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("turista", turistasApi.buscar(codigo));
        return "turistas/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("turista", new GuardarTuristaFormulario());
        prepararFormulario(null, modelo);
        return "turistas/formulario";
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        TuristaRespuesta actual = turistasApi.buscar(codigo);
        GuardarTuristaFormulario formulario = new GuardarTuristaFormulario();
        formulario.setNombre(actual.getNombre());
        formulario.setApellido(actual.getApellido());
        formulario.setDireccion(actual.getDireccion());
        formulario.setEmail(actual.getEmail());
        formulario.setTelefonoFijo(actual.getTelefonoFijo());
        formulario.setTelefonoCelular(actual.getTelefonoCelular());
        formulario.setCodigoSucursal(actual.getCodigoSucursal());
        formulario.setCodigoTitular(actual.getCodigoTitular());
        modelo.addAttribute("turista", formulario);
        prepararFormulario(actual, modelo);
        return "turistas/formulario";
    }

    @PostMapping({ "", "/{codigo}/editar" })
    public String guardar(@PathVariable(required = false) Integer codigo,
            @Valid @ModelAttribute("turista") GuardarTuristaFormulario formulario,
            BindingResult errores, HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        TuristaRespuesta actual = codigo == null ? null : turistasApi.buscar(codigo);
        prepararFormulario(actual, modelo);
        if (actual != null) {
            // La relación familiar no se modifica desde este formulario.
            formulario.setCodigoTitular(actual.getCodigoTitular());
            if (!actual.isTitular()) {
                formulario.setCodigoSucursal(actual.getCodigoSucursal());
            }
        }
        if (!errores.hasFieldErrors("codigoTitular") && !errores.hasFieldErrors("codigoSucursal")) {
            if ((actual != null || formulario.getCodigoTitular() == null) && formulario.getCodigoSucursal() == null) {
                errores.rejectValue("codigoSucursal", "sucursal.requerida", "Elegí una sucursal.");
            } else if (actual == null && formulario.getCodigoTitular() != null
                    && formulario.getCodigoSucursal() != null) {
                errores.rejectValue("codigoSucursal", "sucursal.heredada",
                        "Para un familiar dejá la sucursal sin seleccionar: la hereda del titular.");
            }
        }
        if (errores.hasErrors())
            return "turistas/formulario";
        try {
            if (codigo == null)
                turistasApi.crear(formulario);
            else
                turistasApi.modificar(codigo, formulario);
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado != 400 && estado != 409 && estado != 403)
                throw error;
            respuesta.setStatus(estado);
            modelo.addAttribute("errorOperacion", estado == 409 ? "Ya existe un turista con ese email. Elegí otro."
                    : "No se pudo guardar. Revisá los datos, la sucursal, el titular y tus permisos.");
            return "turistas/formulario";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorOperacion",
                    "No pudimos confirmar el guardado. Consultá el listado antes de volver a intentarlo.");
            return "turistas/formulario";
        }
        redireccion.addFlashAttribute("mensajeExito", "Turista guardado correctamente.");
        return "redirect:/turistas";
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("turista", turistasApi.buscar(codigo));
        return "turistas/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        modelo.addAttribute("turista", turistasApi.buscar(codigo));
        try {
            turistasApi.eliminar(codigo);
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado != 403 && estado != 409)
                throw error;
            respuesta.setStatus(estado);
            modelo.addAttribute("errorOperacion",
                    "No se pudo eliminar. No debe tener reservas, familiares ni una cuenta de usuario asociada. También necesitás permisos de gestión.");
            return "turistas/eliminar";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorOperacion",
                    "No pudimos confirmar la eliminación. Consultá el listado antes de volver a intentarlo.");
            return "turistas/eliminar";
        }
        redireccion.addFlashAttribute("mensajeExito", "Turista eliminado correctamente.");
        return "redirect:/turistas";
    }

    private void prepararFormulario(TuristaRespuesta actual, Model modelo) {
        modelo.addAttribute("actual", actual);
        modelo.addAttribute("sucursales", sucursalesApi.listar());
        if (actual == null) {
            List<TuristaResumen> titulares = new ArrayList<>();
            for (TuristaResumen turista : turistasApi.listar()) {
                if (turista.isTitular())
                    titulares.add(turista);
            }
            modelo.addAttribute("titulares", titulares);
        }
    }

    private String comprobarAcceso(boolean escritura, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        var sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null)
            return "redirect:/login";
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        boolean puedeGestionar = "ADMINISTRADOR".equals(usuario.getRol()) || "VENDEDOR".equals(usuario.getRol());
        modelo.addAttribute("puedeGestionar", puedeGestionar);
        if (escritura && !puedeGestionar) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo administradores y vendedores pueden gestionar turistas.");
            return "error";
        }
        return null;
    }
}
