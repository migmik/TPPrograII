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

import com.tijetravel.tijeback.api.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijeback.api.dto.GuardarHotelSolicitud;
import com.tijetravel.tijeback.api.dto.HotelRespuesta;
import com.tijetravel.tijeback.api.dto.PaginaRespuesta;
import com.tijetravel.tijeback.api.mapeadores.HotelMapeador;
import com.tijetravel.tijeback.servicios.HotelServicio;
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/hoteles")
public class HotelesRestControlador {
        private final HotelServicio hotelServicio;
        private final DisponibilidadServicio disponibilidadServicio;
        private final HotelMapeador hotelMapeador;
        private final UsuarioActualServicio usuarioActualServicio;

        public HotelesRestControlador(
                        HotelServicio hotelServicio,
                        DisponibilidadServicio disponibilidadServicio,
                        HotelMapeador hotelMapeador,
                        UsuarioActualServicio usuarioActualServicio) {
                this.hotelServicio = hotelServicio;
                this.disponibilidadServicio = disponibilidadServicio;
                this.hotelMapeador = hotelMapeador;
                this.usuarioActualServicio = usuarioActualServicio;
        }

        @SuppressWarnings("null") // El mapeador siempre devuelve una instancia no nula.
        @GetMapping
        public List<HotelRespuesta> listar(@RequestParam(defaultValue = "codigo") String orden,
                        Authentication autenticacion) {
                exigirPersonal(autenticacion);
                Comparator<HotelRespuesta> comparador = switch (orden) {
                        case "precioAsc" -> Comparator.comparingInt(HotelRespuesta::precioNochePersona);
                        case "precioDesc" -> Comparator.comparingInt(HotelRespuesta::precioNochePersona).reversed();
                        case "codigo" -> Comparator.comparingInt(HotelRespuesta::codigo);
                        default -> throw new IllegalArgumentException("Orden no valido");
                };
                return hotelServicio.listar().stream()
                                .map(hotelMapeador::aRespuesta)
                                .sorted(comparador.thenComparing(HotelRespuesta::codigo))
                                .toList();
        }

        @GetMapping("/pagina")
        public PaginaRespuesta<HotelRespuesta> listarPagina(
                        @RequestParam(required = false) String ciudad,
                        @RequestParam(required = false) String nombre,
                        @RequestParam(defaultValue = "codigo") String orden,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "20") int tamanio,
                        Authentication autenticacion) {
                ciudad = limpiar(ciudad);
                nombre = limpiar(nombre);
                if (ciudad == null && nombre == null) exigirPersonal(autenticacion);
                if (pagina < 0 || tamanio < 1 || tamanio > 100)
                        throw new IllegalArgumentException("Pagina o tamanio no valido");
                Sort ordenamiento = switch (orden) {
                        case "codigo" -> Sort.by("codigo");
                        case "precioAsc" -> Sort.by("precioNochePersona").ascending().and(Sort.by("codigo"));
                        case "precioDesc" -> Sort.by("precioNochePersona").descending().and(Sort.by("codigo"));
                        default -> throw new IllegalArgumentException("Orden no valido");
                };
                Page<Hotel> resultado = hotelServicio.buscarPagina(ciudad, nombre,
                                PageRequest.of(pagina, tamanio, ordenamiento));
                return new PaginaRespuesta<>(resultado.map(hotelMapeador::aRespuesta).getContent(),
                                resultado.getNumber(), resultado.getTotalPages(), resultado.hasPrevious(),
                                resultado.hasNext());
        }

        private String limpiar(String valor) {
                if (valor == null || valor.isBlank()) return null;
                if (valor.contains("%") || valor.contains("_"))
                        throw new IllegalArgumentException("El filtro contiene caracteres no permitidos");
                return valor.trim();
        }

        private void exigirPersonal(Authentication autenticacion) {
                if (autenticacion == null || !autenticacion.isAuthenticated())
                        throw new OperacionNoPermitidaException("Solo el personal puede consultar el catalogo completo");
                RolUsuario rol = usuarioActualServicio.obtener(autenticacion).getRol();
                if (rol != RolUsuario.ADMINISTRADOR && rol != RolUsuario.VENDEDOR)
                        throw new OperacionNoPermitidaException("Solo el personal puede consultar el catalogo completo");
        }

        @GetMapping("/buscar")
        public List<HotelRespuesta> buscar(@RequestParam String ciudad,
                        @RequestParam LocalDate fechaLlegada, @RequestParam LocalDate fechaPartida,
                        @RequestParam int personas) {
                disponibilidadServicio.validarFechas(fechaLlegada, fechaPartida);
                if (personas < 1 || personas > 20) {
                        throw new IllegalArgumentException("La cantidad de personas debe estar entre 1 y 20");
                }
                return hotelServicio.buscarPorCiudad(ciudad).stream()
                                .filter(hotel -> disponibilidadServicio.plazasDisponiblesHotel(
                                                hotel.getCodigo(), fechaLlegada, fechaPartida) >= personas)
                                .map(hotelMapeador::aRespuesta).toList();
        }

        @GetMapping("/{codigo}")
        public HotelRespuesta encontrarPorId(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo) {
                return hotelMapeador.aRespuesta(hotelServicio.encontrarPorId(codigo));
        }

        @GetMapping("/{codigo}/disponibilidad")
        public DisponibilidadHotelRespuesta consultarDisponibilidad(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        @RequestParam LocalDate fechaLlegada,
                        @RequestParam LocalDate fechaPartida) {
                int plazasDisponibles = disponibilidadServicio.plazasDisponiblesHotel(
                                codigo, fechaLlegada, fechaPartida);
                return new DisponibilidadHotelRespuesta(
                                codigo, fechaLlegada, fechaPartida, plazasDisponibles);
        }

        @PostMapping
        public ResponseEntity<HotelRespuesta> crear(
                        @Valid @RequestBody GuardarHotelSolicitud solicitud,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                Hotel hotel = hotelServicio.crear(
                                actor,
                                solicitud.nombre(),
                                solicitud.direccion(),
                                solicitud.ciudad(),
                                solicitud.telefono(),
                                solicitud.capacidadTotal(),
                                solicitud.precioNochePersona());
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
                return hotelMapeador.aRespuesta(hotelServicio.modificar(
                                actor,
                                codigo,
                                solicitud.nombre(),
                                solicitud.direccion(),
                                solicitud.ciudad(),
                                solicitud.telefono(),
                                solicitud.capacidadTotal(),
                                solicitud.precioNochePersona()));
        }

        @DeleteMapping("/{codigo}")
        public ResponseEntity<Void> eliminar(
                        @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
                        Authentication autenticacion) {
                Usuario actor = usuarioActualServicio.obtener(autenticacion);
                hotelServicio.eliminar(actor, codigo);
                return ResponseEntity.noContent().build();
        }
}
