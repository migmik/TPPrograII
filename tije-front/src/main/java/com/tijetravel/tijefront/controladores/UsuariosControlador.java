package com.tijetravel.tijefront.controladores;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.RolesApiCliente;
import com.tijetravel.tijefront.clientes.TuristasApiCliente;
import com.tijetravel.tijefront.clientes.UsuariosApiCliente;
import com.tijetravel.tijefront.dto.PaginaUsuariosRespuesta;
import com.tijetravel.tijefront.dto.RolRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.dto.UsuarioRespuesta;
import com.tijetravel.tijefront.formularios.CrearUsuarioFormulario;
import com.tijetravel.tijefront.formularios.ModificarUsuarioFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuariosControlador extends ControladorAdministracion {
    private final UsuariosApiCliente usuariosApi;
    private final TuristasApiCliente turistasApi;
    private final RolesApiCliente rolesApi;

    public UsuariosControlador(AutenticacionApiCliente autenticacionApi,
            UsuariosApiCliente usuariosApi, TuristasApiCliente turistasApi, RolesApiCliente rolesApi) {
        super(autenticacionApi);
        this.usuariosApi = usuariosApi;
        this.turistasApi = turistasApi;
        this.rolesApi = rolesApi;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Integer codigoRol,
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String dni,
            @RequestParam(defaultValue = "0") int pagina,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        PaginaUsuariosRespuesta resultado = usuariosApi.listarPagina(codigoRol, codigo, nombre, dni,
                Math.max(0, pagina));
        modelo.addAttribute("usuarios", resultado.elementos());
        modelo.addAttribute("pagina", resultado.pagina());
        modelo.addAttribute("totalPaginas", resultado.totalPaginas());
        modelo.addAttribute("hayAnterior", resultado.hayAnterior());
        modelo.addAttribute("haySiguiente", resultado.haySiguiente());
        modelo.addAttribute("roles", rolesApi.listar());
        modelo.addAttribute("rolBusqueda", codigoRol);
        modelo.addAttribute("codigoBusqueda", codigo);
        modelo.addAttribute("nombreBusqueda", nombre == null ? "" : nombre.trim());
        modelo.addAttribute("dniBusqueda", dni == null ? "" : dni.trim());
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
        cargarRoles(modelo, rolesApi.listar());
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
            List<RolRespuesta> roles = rolesApi.listar();
            modelo.addAttribute("turistasDisponibles", disponibles);
            cargarRoles(modelo, roles);
            validarFormulario(formulario, errores, disponibles, roles);
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
        formulario.setCodigoRol(usuario.getCodigoRol());
        if (!usuario.isCliente()) {
            formulario.setDni(usuario.getDni());
        }
        modelo.addAttribute("cuenta", usuario);
        modelo.addAttribute("usuario", formulario);
        cargarRolesCompatibles(modelo, usuario);
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
            List<RolRespuesta> compatibles = cargarRolesCompatibles(modelo, cuenta);
            validarDni(cuenta.isCliente(), formulario.getDni(), errores);
            validarLongitudContrasenia(formulario.getContrasenia(), errores);
            if (!errores.hasFieldErrors("codigoRol") && buscarRol(compatibles, formulario.getCodigoRol()) == null) {
                errores.rejectValue("codigoRol", "rol.incompatible", "Elegí un rol para este tipo de usuario.");
            }
            if (errores.hasErrors()) {
                return "usuarios/editar";
            }
            formulario.setNombreUsuario(formulario.getNombreUsuario().trim());
            try {
                usuariosApi.modificar(codigo, formulario);
            } catch (RestClientResponseException error) {
                int estado = error.getStatusCode().value();
                if (estado != 400 && estado != 403 && estado != 409) {
                    throw error;
                }
                respuesta.setStatus(estado);
                String mensaje = "La API rechazó los datos. Revisá el nombre, el rol y la contraseña.";
                if (estado == 409) {
                    mensaje = "Ya existe un usuario con ese nombre o DNI. Revisá los datos.";
                } else if (estado == 403) {
                    mensaje = "No podés pasarte a un rol que no administre usuarios.";
                }
                modelo.addAttribute("errorEdicion", mensaje);
                return "usuarios/editar";
            } catch (ResourceAccessException error) {
                respuesta.setStatus(503);
                modelo.addAttribute("errorEdicion",
                        "No pudimos confirmar la modificación. Revisá el estado de la cuenta antes de volver a enviarla.");
                return "usuarios/editar";
            }
            if (esMiCuenta(codigo, solicitud)) {
                // Después de cambiar la propia cuenta se ingresa nuevamente.
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
            redireccion.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
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
                    ? "El backend no permite dar de baja esta cuenta. No se puede dar de baja la propia cuenta ni al último usuario que administra usuarios."
                    : "No se pudo dar de baja la cuenta porque está relacionada con otros datos.");
            return "usuarios/eliminar";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorEliminacion",
                    "No pudimos confirmar la baja. Consultá el listado antes de volver a intentarlo.");
            return "usuarios/eliminar";
        }
        redireccion.addFlashAttribute("mensajeExito", "Usuario dado de baja correctamente.");
        return "redirect:/usuarios";
    }

    private boolean esMiCuenta(Integer codigo, HttpServletRequest solicitud) {
        SesionRespuesta usuario = (SesionRespuesta) solicitud.getSession().getAttribute("usuarioActual");
        return codigo.equals(usuario.getCodigo());
    }

    private List<TuristaResumen> turistasSinCuenta() {
        return turistasApi.listarTitularesSinCuenta();
    }

    /** Roles para elegir al crear y los permisos para mostrar que puede hacer cada uno. */
    private void cargarRoles(Model modelo, List<RolRespuesta> roles) {
        modelo.addAttribute("roles", roles);
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
    }

    /** Al editar solo se ofrecen roles del mismo tipo de usuario que la cuenta. */
    private List<RolRespuesta> cargarRolesCompatibles(Model modelo, UsuarioRespuesta cuenta) {
        List<RolRespuesta> compatibles = new ArrayList<>();
        for (RolRespuesta rol : rolesApi.listar()) {
            if (rol.isParaClientes() == cuenta.isCliente()) {
                compatibles.add(rol);
            }
        }
        cargarRoles(modelo, compatibles);
        return compatibles;
    }

    private RolRespuesta buscarRol(List<RolRespuesta> roles, Integer codigoRol) {
        for (RolRespuesta rol : roles) {
            if (rol.getCodigo().equals(codigoRol)) {
                return rol;
            }
        }
        return null;
    }

    private void validarFormulario(CrearUsuarioFormulario formulario, BindingResult errores,
            List<TuristaResumen> disponibles, List<RolRespuesta> roles) {
        validarLongitudContrasenia(formulario.getContrasenia(), errores);
        if (errores.hasFieldErrors("codigoRol") || errores.hasFieldErrors("codigoTurista")) {
            return;
        }
        RolRespuesta rol = buscarRol(roles, formulario.getCodigoRol());
        if (rol == null) {
            errores.rejectValue("codigoRol", "rol.inexistente", "Elegí un rol de la lista.");
            return;
        }
        validarDni(rol.isParaClientes(), formulario.getDni(), errores);
        if (rol.isParaClientes()) {
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
                    "Para un empleado dejá el turista sin seleccionar.");
        }
    }

    private void validarDni(boolean esCliente, String dni, BindingResult errores) {
        if (errores.hasFieldErrors("dni")) {
            return;
        }
        if (esCliente) {
            if (dni != null) {
                errores.rejectValue("dni", "dni.delTurista", "Para un cliente dejá el DNI vacío: se toma del turista.");
            }
        } else if (dni == null) {
            errores.rejectValue("dni", "dni.obligatorio", "Ingresá el DNI del empleado.");
        }
    }

    private void validarLongitudContrasenia(String contrasenia, BindingResult errores) {
        if (contrasenia == null || errores.hasFieldErrors("contrasenia")) {
            return;
        }
        if (contrasenia.getBytes(StandardCharsets.UTF_8).length > 72) {
            errores.rejectValue("contrasenia", "contrasenia.bytes",
                    "La contraseña es demasiado larga al codificarla. Usá menos caracteres.");
        }
    }

    @Override
    protected String recursoGestionado() {
        return "usuarios";
    }

    @Override
    protected String permisoRequerido() {
        return "ADMINISTRAR_USUARIOS";
    }
}
