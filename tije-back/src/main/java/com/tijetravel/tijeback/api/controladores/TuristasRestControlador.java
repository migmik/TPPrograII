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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.CrearTuristaSolicitud;
import com.tijetravel.tijeback.api.dto.ModificarTuristaSolicitud;
import com.tijetravel.tijeback.api.dto.PaginaRespuesta;
import com.tijetravel.tijeback.api.dto.TuristaRespuesta;
import com.tijetravel.tijeback.api.mapeadores.TuristaMapeador;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;
import com.tijetravel.tijeback.servicios.TuristaServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/turistas")
public class TuristasRestControlador {
        private final TuristaServicio turistaServicio;
        private final TuristaMapeador turistaMapeador;
        private final UsuarioActualServicio usuarioActualServicio;

        public TuristasRestControlador(
                        TuristaServicio turistaServicio,
                        TuristaMapeador turistaMapeador,
                        UsuarioActualServicio usuarioActualServicio) {
                this.turistaServicio = turistaServicio;
                this.turistaMapeador = turistaMapeador;
                this.usuarioActualServicio = usuarioActualServicio;
        }

        @GetMapping
        public List<TuristaRespuesta> listar(
                        @RequestParam(required = false) String dni,
                        @RequestParam(required = false) Boolean titular,
                        @RequestParam(required = false) List<Integer> codigos,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                List<Turista> resultado;
                if (codigos == null) {
                        resultado = turistaServicio.listarPara(actor, dni, titular);
                } else {
                        resultado = turistaServicio.listarPorCodigosPara(actor, codigos);
                }
                return turistaMapeador.aRespuestas(resultado);
        }

        @GetMapping("/pagina")
        public PaginaRespuesta<TuristaRespuesta> listarPagina(
                        @RequestParam(required = false) String dni,
                        @RequestParam(required = false) Boolean titular,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "20") int tamanio,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return turistaMapeador.aPagina(turistaServicio.buscarPaginaPara(actor, dni, titular, pagina, tamanio));
        }

        @GetMapping("/sin-cuenta")
        public List<TuristaRespuesta> listarTitularesSinCuenta(Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return turistaMapeador.aRespuestas(turistaServicio.listarTitularesSinCuentaPara(actor));
        }

        @GetMapping("/{codigo}")
        public TuristaRespuesta encontrarPorId(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return turistaMapeador.aRespuesta(
                                turistaServicio.encontrarVisiblePara(actor, codigo));
        }

        @PostMapping
        public ResponseEntity<TuristaRespuesta> crear(
                        @Valid @RequestBody CrearTuristaSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                Turista turista = turistaServicio.crear(actor, solicitud.dni(), solicitud.nombre(), solicitud.apellido(),
                                solicitud.direccion(), solicitud.email(), solicitud.telefonoFijo(),
                                solicitud.telefonoCelular(), solicitud.codigoSucursal(), solicitud.codigoTitular());
                TuristaRespuesta respuesta = turistaMapeador.aRespuesta(turista);
                return ResponseEntity
                                .created(URI.create("/api/v1/turistas/" + respuesta.codigo()))
                                .body(respuesta);
        }

        @PutMapping("/{codigo}")
        public TuristaRespuesta modificar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        @Valid @RequestBody ModificarTuristaSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return turistaMapeador.aRespuesta(turistaServicio.modificar(
                                actor,
                                codigo,
                                solicitud.dni(),
                                solicitud.nombre(),
                                solicitud.apellido(),
                                solicitud.direccion(),
                                solicitud.email(),
                                solicitud.telefonoFijo(),
                                solicitud.telefonoCelular(),
                                solicitud.codigoSucursal()));
        }

        @DeleteMapping("/{codigo}")
        public ResponseEntity<Void> eliminar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                turistaServicio.eliminar(actor, codigo);
                return ResponseEntity.noContent().build();
        }

}
