package com.tijetravel.tijefront.controladores;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.TuristasApiCliente;
import com.tijetravel.tijefront.clientes.UsuariosApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.dto.UsuarioRespuesta;
import com.tijetravel.tijefront.formularios.CrearUsuarioFormulario;
import com.tijetravel.tijefront.formularios.ModificarUsuarioFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuariosControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final UsuariosApiCliente usuariosApi;
    private final TuristasApiCliente turistasApi;

    public UsuariosControlador(AutenticacionApiCliente autenticacionApi,
            UsuariosApiCliente usuariosApi, TuristasApiCliente turistasApi) {
        this.autenticacionApi = autenticacionApi;
        this.usuariosApi = usuariosApi;
        this.turistasApi = turistasApi;
    }

    @GetMapping
    public String listar(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("usuarios", usuariosApi.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("usuario", new CrearUsuarioFormulario());
        modelo.addAttribute("turistasDisponibles", turistasSinCuenta());
        return "usuarios/nuevo";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("usuario") CrearUsuarioFormulario formulario,
            BindingResult errores, HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo, RedirectAttributes redireccion) {
        try {
            String salida = comprobarAdministrador(solicitud, respuesta, modelo);
            if (salida != null) {
                return salida;
            }
            List<TuristaResumen> disponibles = turistasSinCuenta();
            modelo.addAttribute("turistasDisponibles", disponibles);
            validarFormulario(formulario, errores, disponibles);
            if (errores.hasErrors()) {
                return "usuarios/nuevo";
            }
            formulario.setNombreUsuario(formulario.getNombreUsuario().trim());
            try {
                usuariosApi.crear(formulario);
            } catch (RestClientResponseException error) {
                int estado = error.getStatusCode().value();
                if (estado != 400 && estado != 409) {
                    throw error;
                }
                respuesta.setStatus(estado);
                modelo.addAttribute("errorCreacion", estado == 409
                        ? "El nombre de usuario, el DNI o el turista elegido ya tiene una cuenta. Revisá los datos."
                        : "La API rechazó los datos. Revisá el usuario, la contraseña, el rol y el turista elegido.");
                return "usuarios/nuevo";
            } catch (ResourceAccessException error) {
                respuesta.setStatus(503);
                modelo.addAttribute("errorCreacion",
                        "No pudimos confirmar la creación. Consultá el listado antes de volver a enviar el formulario.");
                return "usuarios/nuevo";
            }
            redireccion.addFlashAttribute("mensajeExito", "Usuario creado correctamente.");
            return "redirect:/usuarios";
        } finally {
            formulario.setContrasenia("");
        }
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        UsuarioRespuesta usuario = usuariosApi.buscar(codigo);
        ModificarUsuarioFormulario formulario = new ModificarUsuarioFormulario();
        formulario.setNombreUsuario(usuario.getNombreUsuario());
        if (!"CLIENTE".equals(usuario.getRol())) formulario.setDni(usuario.getDni());
        modelo.addAttribute("cuenta", usuario);
        modelo.addAttribute("usuario", formulario);
        return "usuarios/editar";
    }

    @PostMapping("/{codigo}/editar")
    public String modificar(@PathVariable Integer codigo,
            @Valid @ModelAttribute("usuario") ModificarUsuarioFormulario formulario,
            BindingResult errores, HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo, RedirectAttributes redireccion) {
        try {
            String salida = comprobarAdministrador(solicitud, respuesta, modelo);
            if (salida != null) {
                return salida;
            }
            UsuarioRespuesta cuenta = usuariosApi.buscar(codigo);
            modelo.addAttribute("cuenta", cuenta);
            validarDni(cuenta.getRol(), formulario.getDni(), errores);
            validarLongitudContrasenia(formulario.getContrasenia(), errores);
            if (errores.hasErrors()) {
                return "usuarios/editar";
            }
            formulario.setNombreUsuario(formulario.getNombreUsuario().trim());
            try {
                usuariosApi.modificar(codigo, formulario);
            } catch (RestClientResponseException error) {
                int estado = error.getStatusCode().value();
                if (estado != 400 && estado != 409) {
                    throw error;
                }
                respuesta.setStatus(estado);
                modelo.addAttribute("errorEdicion", estado == 409
                        ? "Ya existe un usuario con ese nombre o DNI. Revisá los datos."
                        : "La API rechazó los datos. Revisá el nombre y la nueva contraseña.");
                return "usuarios/editar";
            } catch (ResourceAccessException error) {
                respuesta.setStatus(503);
                modelo.addAttribute("errorEdicion",
                        "No pudimos confirmar la modificación. Revisá el estado de la cuenta antes de volver a enviarla.");
                return "usuarios/editar";
            }
            if (esMiCuenta(codigo, solicitud)) {
                // Después de cambiar las propias credenciales se ingresa nuevamente.
                boolean salidaConfirmada = true;
                try {
                    autenticacionApi.cerrarSesion();
                } catch (RestClientException error) {
                    salidaConfirmada = false;
                } finally {
                    solicitud.getSession().invalidate();
                }
                return salidaConfirmada ? "redirect:/login?credencialesActualizadas"
                        : "redirect:/login?credencialesActualizadas&salidaLocal";
            }
            redireccion.addFlashAttribute("mensajeExito", "Credenciales actualizadas correctamente.");
            return "redirect:/usuarios";
        } finally {
            formulario.setContrasenia("");
        }
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("cuenta", usuariosApi.buscar(codigo));
        modelo.addAttribute("esMiCuenta", esMiCuenta(codigo, solicitud));
        return "usuarios/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("cuenta", usuariosApi.buscar(codigo));
        boolean propiaCuenta = esMiCuenta(codigo, solicitud);
        modelo.addAttribute("esMiCuenta", propiaCuenta);
        if (propiaCuenta) {
            respuesta.setStatus(403);
            return "usuarios/eliminar";
        }
        try {
            usuariosApi.eliminar(codigo);
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado != 403 && estado != 409) {
                throw error;
            }
            respuesta.setStatus(estado);
            modelo.addAttribute("errorEliminacion", estado == 403
                    ? "El backend no permite eliminar esta cuenta. Verificá tus permisos; no se puede eliminar la propia cuenta ni al último administrador."
                    : "No se pudo eliminar la cuenta porque está relacionada con otros datos.");
            return "usuarios/eliminar";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorEliminacion",
                    "No pudimos confirmar la eliminación. Consultá el listado antes de volver a intentarlo.");
            return "usuarios/eliminar";
        }
        redireccion.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        return "redirect:/usuarios";
    }

    private boolean esMiCuenta(Integer codigo, HttpServletRequest solicitud) {
        SesionRespuesta usuario = (SesionRespuesta) solicitud.getSession().getAttribute("usuarioActual");
        return codigo.equals(usuario.getCodigo());
    }

    private String comprobarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null) {
            return "redirect:/login";
        }
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo un administrador puede gestionar usuarios.");
            return "error";
        }
        return null;
    }

    private List<TuristaResumen> turistasSinCuenta() {
        Set<Integer> ocupados = new HashSet<>();
        for (UsuarioRespuesta usuario : usuariosApi.listar()) {
            if (usuario.getCodigoTurista() != null) {
                ocupados.add(usuario.getCodigoTurista());
            }
        }
        List<TuristaResumen> disponibles = new ArrayList<>();
        for (TuristaResumen turista : turistasApi.listar()) {
            if (turista.isTitular() && turista.getDni() != null && !ocupados.contains(turista.getCodigo())) {
                disponibles.add(turista);
            }
        }
        return disponibles;
    }

    private void validarFormulario(CrearUsuarioFormulario formulario, BindingResult errores,
            List<TuristaResumen> disponibles) {
        validarDni(formulario.getRol(), formulario.getDni(), errores);
        validarLongitudContrasenia(formulario.getContrasenia(), errores);
        if (errores.hasFieldErrors("rol") || errores.hasFieldErrors("codigoTurista")) {
            return;
        }
        if ("CLIENTE".equals(formulario.getRol())) {
            boolean encontrado = false;
            for (TuristaResumen turista : disponibles) {
                if (turista.getCodigo().equals(formulario.getCodigoTurista())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                errores.rejectValue("codigoTurista", "turista.requerido",
                        "Elegí un turista sin cuenta para el cliente.");
            }
        } else if (formulario.getCodigoTurista() != null) {
            errores.rejectValue("codigoTurista", "turista.noCorresponde",
                    "Para un administrador o vendedor dejá el turista sin seleccionar.");
        }
    }

    private void validarDni(String rol, String dni, BindingResult errores) {
        if (errores.hasFieldErrors("dni")) return;
        if ("CLIENTE".equals(rol)) {
            if (dni != null) errores.rejectValue("dni", "dni.delTurista", "Para un cliente dejá el DNI vacío: se toma del turista.");
        } else if (dni == null) {
            errores.rejectValue("dni", "dni.obligatorio", "Ingresá el DNI del administrador o vendedor.");
        }
    }

    private void validarLongitudContrasenia(String contrasenia, BindingResult errores) {
        if (!errores.hasFieldErrors("contrasenia") && contrasenia.getBytes(StandardCharsets.UTF_8).length > 72) {
            errores.rejectValue("contrasenia", "contrasenia.bytes",
                    "La contraseña es demasiado larga al codificarla. Usá menos caracteres.");
        }
    }
}
