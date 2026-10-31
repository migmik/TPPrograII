package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;

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
import com.tijetravel.tijeback.api.dto.PaginaRespuesta;
import com.tijetravel.tijeback.api.dto.VueloRespuesta;
import com.tijetravel.tijeback.api.mapeadores.VueloMapeador;
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;
import com.tijetravel.tijeback.servicios.VueloServicio;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
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

    @SuppressWarnings("null") // El mapeador siempre devuelve una instancia no nula.
    @GetMapping
    public List<VueloRespuesta> listar(@RequestParam(defaultValue = "numero") String orden,
            Authentication autenticacion) {
        exigirPersonal(autenticacion);
        Comparator<VueloRespuesta> comparador = switch (orden) {
            case "precioAsc" -> Comparator.comparingInt(VueloRespuesta::precio);
            case "precioDesc" -> Comparator.comparingInt(VueloRespuesta::precio).reversed();
            case "numero" -> Comparator.comparingInt(VueloRespuesta::numero);
            default -> throw new IllegalArgumentException("Orden no valido");
        };
        return vueloServicio.listar().stream()
                .map(vueloMapeador::aRespuesta)
                .sorted(comparador.thenComparing(VueloRespuesta::numero))
                .toList();
    }

    @GetMapping("/origenes")
    public List<String> origenes() { return vueloServicio.listarOrigenes(); }

    @GetMapping("/destinos")
    public List<String> destinos() { return vueloServicio.listarDestinos(); }

    @GetMapping("/pagina")
    public PaginaRespuesta<VueloRespuesta> listarPagina(
            @RequestParam(required = false) String origen, @RequestParam(required = false) String destino,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(defaultValue = "numero") String orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio,
            Authentication autenticacion) {
        origen = limpiar(origen);
        destino = limpiar(destino);
        if (origen == null && destino == null && fechaDesde == null && fechaHasta == null)
            exigirPersonal(autenticacion);
        if (pagina < 0 || tamanio < 1 || tamanio > 100)
            throw new IllegalArgumentException("Pagina o tamanio no valido");
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde))
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        Sort ordenamiento = switch (orden) {
            case "numero" -> Sort.by("numero");
            case "precioAsc" -> Sort.by("precio").ascending().and(Sort.by("numero"));
            case "precioDesc" -> Sort.by("precio").descending().and(Sort.by("numero"));
            default -> throw new IllegalArgumentException("Orden no valido");
        };
        Page<Vuelo> resultado = vueloServicio.buscarPagina(origen, destino, fechaDesde, fechaHasta,
                PageRequest.of(pagina, tamanio, ordenamiento));
        return new PaginaRespuesta<>(resultado.map(vueloMapeador::aRespuesta).getContent(),
                resultado.getNumber(), resultado.getTotalPages(), resultado.hasPrevious(), resultado.hasNext());
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        if (valor.contains("%") || valor.contains("_"))
            throw new IllegalArgumentException("El filtro contiene caracteres no permitidos");
        return valor.trim();
    }

    private void exigirPersonal(Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.buscar(autenticacion);
        if (actor == null || !actor.tienePermiso(Permiso.VER_CATALOGO_COMPLETO))
            throw new OperacionNoPermitidaException("Solo el personal puede consultar el catalogo completo");
    }

    @GetMapping("/buscar")
    public List<VueloRespuesta> buscar(@RequestParam String origen, @RequestParam String destino,
            @RequestParam LocalDate fecha, @RequestParam int personas,
            @RequestParam(defaultValue = "false") boolean sugerencias) {
        List<Vuelo> vuelos = sugerencias
                ? vueloServicio.sugerirOtrosDias(origen, destino, fecha, personas)
                : vueloServicio.buscarEnFecha(origen, destino, fecha, personas);
        return vuelos.stream().map(vueloMapeador::aRespuesta).toList();
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
                solicitud.plazasPrimera(),
                solicitud.precio());
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
                solicitud.plazasPrimera(),
                solicitud.precio()));
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
