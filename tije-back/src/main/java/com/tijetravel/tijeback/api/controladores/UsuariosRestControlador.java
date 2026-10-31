package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.tijetravel.tijeback.api.dto.PaginaRespuesta;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.CrearUsuarioSolicitud;
import com.tijetravel.tijeback.api.dto.ModificarUsuarioSolicitud;
import com.tijetravel.tijeback.api.dto.UsuarioRespuesta;
import com.tijetravel.tijeback.api.mapeadores.UsuarioMapeador;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.servicios.UsuarioServicio;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosRestControlador {
        private final UsuarioServicio usuarioServicio;
        private final UsuarioMapeador usuarioMapeador;
        private final UsuarioActualServicio usuarioActualServicio;

        public UsuariosRestControlador(
                        UsuarioServicio usuarioServicio,
                        UsuarioMapeador usuarioMapeador,
                        UsuarioActualServicio usuarioActualServicio) {
                this.usuarioServicio = usuarioServicio;
                this.usuarioMapeador = usuarioMapeador;
                this.usuarioActualServicio = usuarioActualServicio;
        }

        @SuppressWarnings("null") // El mapeador siempre devuelve una instancia no nula.
        @GetMapping
        public List<UsuarioRespuesta> listar(
                        @RequestParam(required = false) RolUsuario rol,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return usuarioServicio.listarPara(actor, rol).stream()
                                .map(usuarioMapeador::aRespuesta)
                                .sorted(Comparator.comparing(UsuarioRespuesta::codigo))
                                .toList();
        }

        @GetMapping("/pagina")
        public PaginaRespuesta<UsuarioRespuesta> listarPagina(
                        @RequestParam(required = false) RolUsuario rol,
                        @RequestParam(required = false) Integer codigo,
                        @RequestParam(required = false) String nombre,
                        @RequestParam(required = false) String dni,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "20") int tamanio,
                        Authentication autenticacion) {
                if (pagina < 0 || tamanio < 1 || tamanio > 100)
                        throw new IllegalArgumentException("Pagina o tamanio no valido");
                if (nombre != null && (nombre.contains("%") || nombre.contains("_")))
                        throw new IllegalArgumentException("El filtro contiene caracteres no permitidos");
                if (dni != null && !dni.isBlank() && !dni.trim().matches("[0-9]{7,8}"))
                        throw new IllegalArgumentException("El DNI debe tener 7 u 8 digitos");
                if (codigo != null && codigo < 1)
                        throw new IllegalArgumentException("El codigo debe ser positivo");
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                Page<Usuario> resultado = usuarioServicio.buscarPaginaPara(actor, rol, codigo, nombre, dni,
                                PageRequest.of(pagina, tamanio, Sort.by("codigo")));
                return new PaginaRespuesta<>(resultado.map(usuarioMapeador::aRespuesta).getContent(),
                                resultado.getNumber(), resultado.getTotalPages(), resultado.hasPrevious(),
                                resultado.hasNext());
        }

        @GetMapping("/{codigo}")
        public UsuarioRespuesta encontrarPorId(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return usuarioMapeador.aRespuesta(
                                usuarioServicio.encontrarVisiblePara(actor, codigo));
        }

        @PostMapping
        public ResponseEntity<UsuarioRespuesta> crear(
                        @Valid @RequestBody CrearUsuarioSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                Usuario usuario = usuarioServicio.crear(
                                actor,
                                solicitud.nombreUsuario(),
                                solicitud.contrasenia(),
                                solicitud.rol(),
                                solicitud.codigoTurista(), solicitud.dni());
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
                return usuarioMapeador.aRespuesta(usuarioServicio.modificarCredenciales(
                                actor,
                                codigo,
                                solicitud.nombreUsuario(),
                                solicitud.contrasenia(), solicitud.dni()));
        }

        @DeleteMapping("/{codigo}")
        public ResponseEntity<Void> eliminar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                usuarioServicio.eliminar(actor, codigo);
                return ResponseEntity.noContent().build();
        }
}
