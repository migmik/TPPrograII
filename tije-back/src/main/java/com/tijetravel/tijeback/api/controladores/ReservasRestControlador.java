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

import com.tijetravel.tijeback.api.dto.GuardarReservaSolicitud;
import com.tijetravel.tijeback.api.dto.ReservaRespuesta;
import com.tijetravel.tijeback.api.mapeadores.ReservaMapeador;
import com.tijetravel.tijeback.servicios.ReservaServicio;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservasRestControlador {
        private final ReservaServicio reservaServicio;
        private final ReservaMapeador reservaMapeador;
        private final UsuarioActualServicio usuarioActualServicio;

        public ReservasRestControlador(
                        ReservaServicio reservaServicio,
                        ReservaMapeador reservaMapeador,
                        UsuarioActualServicio usuarioActualServicio) {
                this.reservaServicio = reservaServicio;
                this.reservaMapeador = reservaMapeador;
                this.usuarioActualServicio = usuarioActualServicio;
        }

        @SuppressWarnings("null") // El mapeador siempre devuelve una instancia no nula.
        @GetMapping
        public List<ReservaRespuesta> listar(Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return reservaServicio.listarPara(actor).stream()
                                .map(reservaMapeador::aRespuesta)
                                .sorted(Comparator.comparing(ReservaRespuesta::codigo))
                                .toList();
        }

        @GetMapping("/{codigo}")
        public ReservaRespuesta encontrarPorId(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return reservaMapeador.aRespuesta(
                                reservaServicio.encontrarVisiblePara(actor, codigo));
        }

        @PostMapping
        public ResponseEntity<ReservaRespuesta> crear(
                        @Valid @RequestBody GuardarReservaSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                Reserva reserva = reservaServicio.crear(
                                actor,
                                solicitud.codigoTurista(),
                                solicitud.numeroVuelo(),
                                solicitud.codigoHotel(),
                                solicitud.claseVuelo(),
                                solicitud.tipoHospedaje(),
                                solicitud.fechaLlegada(),
                                solicitud.fechaPartida());
                ReservaRespuesta respuesta = reservaMapeador.aRespuesta(reserva);
                return ResponseEntity
                                .created(URI.create("/api/v1/reservas/" + respuesta.codigo()))
                                .body(respuesta);
        }

        @PutMapping("/{codigo}")
        public ReservaRespuesta modificar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        @Valid @RequestBody GuardarReservaSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                return reservaMapeador.aRespuesta(reservaServicio.modificar(
                                actor,
                                codigo,
                                solicitud.codigoTurista(),
                                solicitud.numeroVuelo(),
                                solicitud.codigoHotel(),
                                solicitud.claseVuelo(),
                                solicitud.tipoHospedaje(),
                                solicitud.fechaLlegada(),
                                solicitud.fechaPartida()));
        }

        @DeleteMapping("/{codigo}")
        public ResponseEntity<Void> eliminar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                reservaServicio.eliminar(actor, codigo);
                return ResponseEntity.noContent().build();
        }
}
