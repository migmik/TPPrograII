package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.util.Comparator;
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

import com.tijetravel.tijeback.api.dto.CrearUsuarioSolicitud;
import com.tijetravel.tijeback.api.dto.ModificarUsuarioSolicitud;
import com.tijetravel.tijeback.api.dto.UsuarioRespuesta;
import com.tijetravel.tijeback.api.mapeadores.UsuarioMapeador;
import com.tijetravel.tijeback.controladores.UsuariosControlador;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosRestControlador {
    private final UsuariosControlador usuariosControlador;
    private final UsuarioMapeador usuarioMapeador;
    private final UsuarioActualServicio usuarioActualServicio;

    public UsuariosRestControlador(
            UsuariosControlador usuariosControlador,
            UsuarioMapeador usuarioMapeador,
            UsuarioActualServicio usuarioActualServicio) {
        this.usuariosControlador = usuariosControlador;
        this.usuarioMapeador = usuarioMapeador;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping
    public List<UsuarioRespuesta> listar(Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return usuariosControlador.listarPara(actor).stream()
                .map(usuarioMapeador::aRespuesta)
                .sorted(Comparator.comparing(UsuarioRespuesta::codigo))
                .toList();
    }

    @GetMapping("/{codigo}")
    public UsuarioRespuesta encontrarPorId(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return usuarioMapeador.aRespuesta(
                usuariosControlador.encontrarVisiblePara(actor, codigo));
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> ingresar(
            @Valid @RequestBody CrearUsuarioSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        Usuario usuario = usuariosControlador.ingresar(
                actor,
                solicitud.nombreUsuario(),
                solicitud.contrasenia(),
                solicitud.rol(),
                solicitud.codigoTurista());
        UsuarioRespuesta respuesta = usuarioMapeador.aRespuesta(usuario);
        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + respuesta.codigo()))
                .body(respuesta);
    }

    @PutMapping("/{codigo}")
    public UsuarioRespuesta modificar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            @Valid @RequestBody ModificarUsuarioSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return usuarioMapeador.aRespuesta(usuariosControlador.modificarCredenciales(
                actor,
                codigo,
                solicitud.nombreUsuario(),
                solicitud.contrasenia()));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        usuariosControlador.eliminar(actor, codigo);
        return ResponseEntity.noContent().build();
    }
}
