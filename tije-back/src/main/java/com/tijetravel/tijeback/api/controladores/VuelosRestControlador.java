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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.CrearVueloSolicitud;
import com.tijetravel.tijeback.api.dto.DisponibilidadVueloRespuesta;
import com.tijetravel.tijeback.api.dto.ModificarVueloSolicitud;
import com.tijetravel.tijeback.api.dto.VueloRespuesta;
import com.tijetravel.tijeback.api.mapeadores.VueloMapeador;
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;
import com.tijetravel.tijeback.servicios.VueloServicio;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/vuelos")
public class VuelosRestControlador {
    private final VueloServicio vueloServicio;
    private final DisponibilidadServicio disponibilidadServicio;
    private final VueloMapeador vueloMapeador;
    private final UsuarioActualServicio usuarioActualServicio;

    public VuelosRestControlador(
            VueloServicio vueloServicio,
            DisponibilidadServicio disponibilidadServicio,
            VueloMapeador vueloMapeador,
            UsuarioActualServicio usuarioActualServicio) {
        this.vueloServicio = vueloServicio;
        this.disponibilidadServicio = disponibilidadServicio;
        this.vueloMapeador = vueloMapeador;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping
    public List<VueloRespuesta> listar() {
        return vueloServicio.listar().stream()
                .map(vueloMapeador::aRespuesta)
                .sorted(Comparator.comparing(VueloRespuesta::numero))
                .toList();
    }

    @GetMapping("/{numero}")
    public VueloRespuesta encontrarPorId(
            @PathVariable @Positive(message = "El numero debe ser positivo") Integer numero) {
        return vueloMapeador.aRespuesta(vueloServicio.encontrarPorId(numero));
    }

    @GetMapping("/{numero}/disponibilidad")
    public DisponibilidadVueloRespuesta consultarDisponibilidad(
            @PathVariable @Positive(message = "El numero debe ser positivo") Integer numero,
            @RequestParam ClaseVuelo clase) {
        int plazasDisponibles = disponibilidadServicio.plazasDisponiblesVuelo(numero, clase);
        return new DisponibilidadVueloRespuesta(numero, clase, plazasDisponibles);
    }

    @PostMapping
    public ResponseEntity<VueloRespuesta> crear(
            @Valid @RequestBody CrearVueloSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        Vuelo vuelo = vueloServicio.crear(
                actor,
                solicitud.numero(),
                solicitud.fechaYHora(),
                solicitud.origen(),
                solicitud.destino(),
                solicitud.totalPlazas(),
                solicitud.plazasTurista(),
                solicitud.plazasPrimera());
        VueloRespuesta respuesta = vueloMapeador.aRespuesta(vuelo);
        return ResponseEntity
                .created(URI.create("/api/v1/vuelos/" + respuesta.numero()))
                .body(respuesta);
    }

    @PutMapping("/{numero}")
    public VueloRespuesta modificar(
            @PathVariable @Positive(message = "El numero debe ser positivo") Integer numero,
            @Valid @RequestBody ModificarVueloSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return vueloMapeador.aRespuesta(vueloServicio.modificar(
                actor,
                numero,
                solicitud.fechaYHora(),
                solicitud.origen(),
                solicitud.destino(),
                solicitud.totalPlazas(),
                solicitud.plazasTurista(),
                solicitud.plazasPrimera()));
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El numero debe ser positivo") Integer numero,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        vueloServicio.eliminar(actor, numero);
        return ResponseEntity.noContent().build();
    }
}
