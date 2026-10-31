package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.CrearRolSolicitud;
import com.tijetravel.tijeback.api.dto.ModificarRolSolicitud;
import com.tijetravel.tijeback.api.dto.PermisoRespuesta;
import com.tijetravel.tijeback.api.dto.RolRespuesta;
import com.tijetravel.tijeback.api.mapeadores.RolMapeador;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;
import com.tijetravel.tijeback.servicios.RolServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/roles")
public class RolesRestControlador {
    private final RolServicio rolServicio;
    private final RolMapeador rolMapeador;
    private final UsuarioActualServicio usuarioActualServicio;

    public RolesRestControlador(
            RolServicio rolServicio,
            RolMapeador rolMapeador,
            UsuarioActualServicio usuarioActualServicio) {
        this.rolServicio = rolServicio;
        this.rolMapeador = rolMapeador;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping
    public List<RolRespuesta> listar(Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return rolMapeador.aRespuestas(rolServicio.listarPara(actor));
    }

    @GetMapping("/permisos")
    public List<PermisoRespuesta> permisos() {
        return rolMapeador.aPermisos(rolServicio.listarPermisos());
    }

    @GetMapping("/{codigo}")
    public RolRespuesta encontrarPorId(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return rolMapeador.aRespuesta(rolServicio.encontrarPara(actor, codigo));
    }

    @PostMapping
    public ResponseEntity<RolRespuesta> crear(
            @Valid @RequestBody CrearRolSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        Rol rol = rolServicio.crear(actor, solicitud.nombre(), solicitud.tipoUsuario(), solicitud.permisos());
        RolRespuesta respuesta = rolMapeador.aRespuesta(rol);
        return ResponseEntity
                .created(URI.create("/api/v1/roles/" + respuesta.codigo()))
                .body(respuesta);
    }

    @PutMapping("/{codigo}")
    public RolRespuesta modificar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            @Valid @RequestBody ModificarRolSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return rolMapeador.aRespuesta(
                rolServicio.modificar(actor, codigo, solicitud.nombre(), solicitud.permisos()));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        rolServicio.darDeBaja(actor, codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bajas")
    public List<RolRespuesta> listarBajas(Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return rolMapeador.aRespuestas(rolServicio.listarBajasPara(actor));
    }

    @PostMapping("/{codigo}/reactivar")
    public RolRespuesta reactivar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return rolMapeador.aRespuesta(rolServicio.reactivar(actor, codigo));
    }
}
