package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class VueloServicio {
    private static final int MAXIMO_SUGERENCIAS = 5;

    private final BloqueoEscrituras bloqueoEscrituras;
    private final VueloRepositorio vueloRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionServicio autorizacion;

    public VueloServicio(
            VueloRepositorio vueloRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.vueloRepositorio = vueloRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Vuelo crear(Usuario actor, int numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        if (vueloRepositorio.existsById(numero)) {
            throw new EntidadDuplicadaException("Ya existe el vuelo " + numero);
        }
        Vuelo vuelo = new Vuelo(
                numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, precio);
        return vueloRepositorio.save(vuelo);
    }

    /** Catalogo completo, solo para el personal de la agencia. */
    public List<Vuelo> listarPara(Usuario actor, String orden) {
        autorizacion.verificarPermiso(actor, Permiso.VER_CATALOGO_COMPLETO,
                "Solo el personal puede consultar el catalogo completo");
        return vueloRepositorio.findAll(ordenar(orden));
    }

    public List<String> listarOrigenes() {
        return vueloRepositorio.listarOrigenes();
    }

    public List<String> listarDestinos() {
        return vueloRepositorio.listarDestinos();
    }

    /** Sin filtros equivale al catalogo completo, por eso pide el mismo permiso. */
    public Page<Vuelo> buscarPagina(Usuario actor, String origen, String destino, LocalDate desde,
            LocalDate hasta, String orden, int pagina, int tamanio) {
        String origenBuscado = FiltrosConsulta.texto(origen);
        String destinoBuscado = FiltrosConsulta.texto(destino);
        if (origenBuscado == null && destinoBuscado == null && desde == null && hasta == null) {
            autorizacion.verificarPermiso(actor, Permiso.VER_CATALOGO_COMPLETO,
                    "Solo el personal puede consultar el catalogo completo");
        }
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, ordenar(orden));
        LocalDateTime inicio = null;
        if (desde != null) {
            inicio = desde.atStartOfDay();
        }
        LocalDateTime fin = null;
        if (hasta != null) {
            fin = hasta.plusDays(1).atStartOfDay();
        }
        return vueloRepositorio.buscarPagina(origenBuscado, destinoBuscado, inicio, fin, paginado);
    }

    public List<Vuelo> buscarEnFecha(String origen, String destino, LocalDate fecha, int personas) {
        validarBusqueda(fecha, personas);
        return vueloRepositorio.buscarEnFecha(CiudadConsulta.nombres(origen), CiudadConsulta.nombres(destino),
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), ClaseVuelo.TURISTA, personas);
    }

    /** Vuelos de otros dias con lugar, empezando por los mas cercanos a la fecha pedida. */
    public List<Vuelo> sugerirOtrosDias(String origen, String destino, LocalDate fecha, int personas) {
        validarBusqueda(fecha, personas);
        List<Vuelo> vuelos = vueloRepositorio.buscarOtrosDias(
                CiudadConsulta.nombres(origen), CiudadConsulta.nombres(destino),
                LocalDate.now().atStartOfDay(), fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay(), ClaseVuelo.TURISTA, personas);
        List<Vuelo> ordenados = new ArrayList<>(vuelos);
        Collections.sort(ordenados, new ComparadorCercaniaFecha(fecha));

        List<Vuelo> sugerencias = new ArrayList<>();
        for (int i = 0; i < ordenados.size() && i < MAXIMO_SUGERENCIAS; i++) {
            sugerencias.add(ordenados.get(i));
        }
        return sugerencias;
    }

    public Vuelo encontrarPorId(Integer numero) {
        Vuelo vuelo = vueloRepositorio.findById(numero).orElse(null);
        if (vuelo == null) {
            throw new EntidadNoEncontradaException("No se encontro el vuelo " + numero);
        }
        return vuelo;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Vuelo modificar(Usuario actor, Integer numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        Vuelo vuelo = encontrarPorId(numero);
        if (fechaYHora == null) {
            throw new IllegalArgumentException("El campo fechaYHora es obligatorio");
        }
        String destinoNormalizado = ValidacionModelo.textoObligatorio(destino, "destino");

        long reservasTurista = reservaRepositorio.countByVueloNumeroAndClaseVuelo(numero, ClaseVuelo.TURISTA);
        long reservasPrimera = reservaRepositorio.countByVueloNumeroAndClaseVuelo(numero, ClaseVuelo.PRIMERA);
        if (plazasTurista < reservasTurista || plazasPrimera < reservasPrimera) {
            throw new CapacidadExcedidaException(
                    "Las plazas no pueden ser menores que las reservas ya registradas");
        }

        // Las reservas ya hechas tienen que seguir coincidiendo con la fecha y el destino del vuelo.
        LocalDate nuevaFecha = fechaYHora.toLocalDate();
        for (Reserva reserva : reservaRepositorio.findByVueloNumero(numero)) {
            if (!reserva.getFechaLlegada().equals(nuevaFecha)
                    || !CiudadConsulta.misma(reserva.getHotel().getCiudad(), destinoNormalizado)) {
                throw new OperacionNoPermitidaException(
                        "El cambio dejaría reservas incompatibles con su hotel o fecha");
            }
        }
        vuelo.actualizarDatos(fechaYHora, origen, destinoNormalizado, totalPlazas, plazasTurista, plazasPrimera,
                precio);
        return vuelo;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer numero) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        Vuelo vuelo = encontrarPorId(numero);
        if (reservaRepositorio.existsByVueloNumero(numero)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un vuelo que tiene reservas");
        }
        vueloRepositorio.delete(vuelo);
    }

    private void validarBusqueda(LocalDate fecha, int personas) {
        if (fecha == null || personas < 1 || personas > 20) {
            throw new IllegalArgumentException("La fecha y una cantidad de 1 a 20 personas son obligatorias");
        }
    }

    private Sort ordenar(String orden) {
        return FiltrosConsulta.ordenCatalogo(orden, "numero", "precio");
    }
}
