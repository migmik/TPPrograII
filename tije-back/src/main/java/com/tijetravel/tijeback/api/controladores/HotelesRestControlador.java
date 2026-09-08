package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijeback.api.dto.GuardarHotelSolicitud;
import com.tijetravel.tijeback.api.dto.HotelRespuesta;
import com.tijetravel.tijeback.api.mapeadores.HotelMapeador;
import com.tijetravel.tijeback.controladores.HotelesControlador;
import com.tijetravel.tijeback.controladores.ReservasControlador;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/hoteles")
public class HotelesRestControlador {
    private final HotelesControlador hotelesControlador;
    private final ReservasControlador reservasControlador;
    private final HotelMapeador hotelMapeador;
    private final UsuarioActualServicio usuarioActualServicio;

    public HotelesRestControlador(
            HotelesControlador hotelesControlador,
            ReservasControlador reservasControlador,
            HotelMapeador hotelMapeador,
            UsuarioActualServicio usuarioActualServicio) {
        this.hotelesControlador = hotelesControlador;
        this.reservasControlador = reservasControlador;
        this.hotelMapeador = hotelMapeador;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping
    public List<HotelRespuesta> listar() {
        return hotelesControlador.listar().stream()
                .map(hotelMapeador::aRespuesta)
                .sorted(Comparator.comparing(HotelRespuesta::codigo))
                .toList();
    }

    @GetMapping("/{codigo}")
    public HotelRespuesta encontrarPorId(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo) {
        return hotelMapeador.aRespuesta(hotelesControlador.encontrarPorId(codigo));
    }

    @GetMapping("/{codigo}/disponibilidad")
    public DisponibilidadHotelRespuesta consultarDisponibilidad(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            @RequestParam LocalDate fechaLlegada,
            @RequestParam LocalDate fechaPartida) {
        int plazasDisponibles = reservasControlador.plazasDisponiblesHotel(
                codigo, fechaLlegada, fechaPartida);
        return new DisponibilidadHotelRespuesta(
                codigo, fechaLlegada, fechaPartida, plazasDisponibles);
    }

    @PostMapping
    public ResponseEntity<HotelRespuesta> ingresar(
            @Valid @RequestBody GuardarHotelSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        Hotel hotel = hotelesControlador.ingresar(
                actor,
                solicitud.nombre(),
                solicitud.direccion(),
                solicitud.ciudad(),
                solicitud.telefono(),
                solicitud.plazasDisponibles());
        HotelRespuesta respuesta = hotelMapeador.aRespuesta(hotel);
        return ResponseEntity
                .created(URI.create("/api/v1/hoteles/" + respuesta.codigo()))
                .body(respuesta);
    }

    @PutMapping("/{codigo}")
    public HotelRespuesta modificar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            @Valid @RequestBody GuardarHotelSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return hotelMapeador.aRespuesta(hotelesControlador.modificar(
                actor,
                codigo,
                solicitud.nombre(),
                solicitud.direccion(),
                solicitud.ciudad(),
                solicitud.telefono(),
                solicitud.plazasDisponibles()));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        hotelesControlador.eliminar(actor, codigo);
        return ResponseEntity.noContent().build();
    }
}
