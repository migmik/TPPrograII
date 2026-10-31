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
import com.tijetravel.tijefront.clientes.RolesApiCliente;
import com.tijetravel.tijefront.dto.RolRespuesta;
import com.tijetravel.tijefront.formularios.GuardarRolFormulario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Pantalla de roles: cada rol es un conjunto de permisos con casillas. Al
 * cambiar un rol cambia lo que pueden hacer todos los usuarios que lo tienen.
 */
@Controller
@RequestMapping("/roles")
public class RolesControlador extends ControladorAdministracion {
    private final RolesApiCliente rolesApi;

    public RolesControlador(AutenticacionApiCliente autenticacionApi, RolesApiCliente rolesApi) {
        super(autenticacionApi);
        this.rolesApi = rolesApi;
    }

    @GetMapping
    public String listar(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("roles", rolesApi.listar());
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
        return "roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("rol", new GuardarRolFormulario());
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
        return "roles/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("rol") GuardarRolFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo,
            RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
        if (errores.hasErrors()) {
            return "roles/formulario";
        }
        formulario.setNombre(formulario.getNombre().trim());
        try {
            rolesApi.crear(formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "roles/formulario", "Ya existe un rol con ese nombre.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "roles/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Rol creado correctamente.");
        return "redirect:/roles";
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        RolRespuesta rol = rolesApi.buscar(codigo);
        GuardarRolFormulario formulario = new GuardarRolFormulario();
        formulario.setNombre(rol.getNombre());
        formulario.setTipoUsuario(rol.getTipoUsuario());
        formulario.setPermisos(rol.getPermisos());
        modelo.addAttribute("rol", formulario);
        modelo.addAttribute("actual", rol);
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
        return "roles/formulario";
    }

    @PostMapping("/{codigo}/editar")
    public String modificar(@PathVariable Integer codigo,
            @Valid @ModelAttribute("rol") GuardarRolFormulario formulario, BindingResult errores,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo,
            RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        RolRespuesta actual = rolesApi.buscar(codigo);
        // El tipo de usuario no se cambia: se toma siempre el del rol guardado.
        formulario.setTipoUsuario(actual.getTipoUsuario());
        modelo.addAttribute("actual", actual);
        modelo.addAttribute("permisos", rolesApi.listarPermisos());
        if (errores.hasErrors()) {
            return "roles/formulario";
        }
        formulario.setNombre(formulario.getNombre().trim());
        try {
            rolesApi.modificar(codigo, formulario);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "roles/formulario", "Ya existe un rol con ese nombre.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "roles/formulario");
        }
        redireccion.addFlashAttribute("mensajeExito", "Rol actualizado. Los usuarios con este rol ya tienen los nuevos permisos.");
        return "redirect:/roles";
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("actual", rolesApi.buscar(codigo));
        return "roles/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAdministrador(solicitud, respuesta, modelo);
        if (salida != null) {
            return salida;
        }
        modelo.addAttribute("actual", rolesApi.buscar(codigo));
        try {
            rolesApi.eliminar(codigo);
        } catch (RestClientResponseException error) {
            return mostrarError(error, respuesta, modelo, "roles/eliminar", "No se pudo dar de baja el rol.");
        } catch (ResourceAccessException error) {
            return mostrarConexionIncierta(respuesta, modelo, "roles/eliminar");
        }
        redireccion.addFlashAttribute("mensajeExito", "Rol dado de baja correctamente.");
        return "redirect:/roles";
    }

    @Override
    protected String recursoGestionado() {
        return "roles";
    }

    @Override
    protected String permisoRequerido() {
        return "ADMINISTRAR_USUARIOS";
    }

    @Override
    protected String mensajeOperacionNoPermitida() {
        return "No se puede: el rol tiene usuarios activos, es el último rol de clientes o te quitaría "
                + "el permiso de administrar usuarios.";
    }
}
