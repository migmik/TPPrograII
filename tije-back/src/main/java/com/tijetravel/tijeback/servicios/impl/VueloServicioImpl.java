package com.tijetravel.tijeback.servicios.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.VueloServicio;

@Service
@Transactional(readOnly = true)
public class VueloServicioImpl implements VueloServicio {
    private static final int MAXIMO_SUGERENCIAS = 3;

    private final BloqueoEscrituras bloqueoEscrituras;
    private final VueloRepositorio vueloRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionServicio autorizacion;

    public VueloServicioImpl(
            VueloRepositorio vueloRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.vueloRepositorio = vueloRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Vuelo crear(Usuario actor, int numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        Vuelo existente = vueloRepositorio.findById(numero).orElse(null);
        if (existente != null && existente.estaActivo()) {
            throw new EntidadDuplicadaException("Ya existe el vuelo " + numero);
        }
        if (existente != null) {
            throw new EntidadDuplicadaException(
                    "El vuelo " + numero + " esta dado de baja; se puede reactivar desde los dados de baja");
        }
        Vuelo vuelo = new Vuelo(
                numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, precio);
        return vueloRepositorio.save(vuelo);
    }

    @Override
    public List<Vuelo> listarPara(Usuario actor, String orden) {
        autorizacion.verificarPermiso(actor, Permiso.VER_CATALOGO_COMPLETO,
                "Solo el personal puede consultar el catalogo completo");
        return vueloRepositorio.findByFechaBajaIsNull(ordenar(orden));
    }

    @Override
    public List<String> listarOrigenes() {
        return vueloRepositorio.listarOrigenes();
    }

    @Override
    public List<String> listarDestinos() {
        return vueloRepositorio.listarDestinos();
    }

    @Override
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

    @Override
    public List<Vuelo> buscarEnFecha(String origen, String destino, LocalDate fecha, int personas) {
        validarBusqueda(fecha, personas);
        return vueloRepositorio.buscarEnFecha(CiudadConsulta.nombres(origen), CiudadConsulta.nombres(destino),
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), ClaseVuelo.TURISTA, personas);
    }

    @Override
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

    @Override
    public List<Vuelo> buscarProximos(String origen, String destino, int personas) {
        if (personas < 1 || personas > 20) {
            throw new IllegalArgumentException("La cantidad de personas debe ser de 1 a 20");
        }
        return vueloRepositorio.buscarProximos(CiudadConsulta.nombres(origen), CiudadConsulta.nombres(destino),
                LocalDateTime.now(), ClaseVuelo.TURISTA, personas, PageRequest.of(0, MAXIMO_SUGERENCIAS));
    }

    @Override
    public Vuelo encontrarPorId(Integer numero) {
        Vuelo vuelo = vueloRepositorio.findById(numero).orElse(null);
        if (vuelo == null || !vuelo.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro el vuelo " + numero);
        }
        return vuelo;
    }

    @Override
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

        long reservasTurista = reservaRepositorio.countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(
                numero, ClaseVuelo.TURISTA);
        long reservasPrimera = reservaRepositorio.countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(
                numero, ClaseVuelo.PRIMERA);
        if (plazasTurista < reservasTurista || plazasPrimera < reservasPrimera) {
            throw new CapacidadExcedidaException(
                    "Las plazas no pueden ser menores que las reservas ya registradas");
        }

        // Las reservas ya hechas tienen que seguir coincidiendo con la fecha y el destino del vuelo.
        LocalDate nuevaFecha = fechaYHora.toLocalDate();
        for (Reserva reserva : reservaRepositorio.findByVueloNumeroAndFechaBajaIsNull(numero)) {
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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer numero) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        Vuelo vuelo = encontrarPorId(numero);
        if (reservaRepositorio.existsByVueloNumeroAndFechaBajaIsNullAndFechaLlegadaGreaterThanEqual(
                numero, LocalDate.now())) {
            throw new OperacionNoPermitidaException(
                    "No se puede dar de baja un vuelo que todavia no salio y tiene reservas");
        }
        vuelo.darDeBaja(EntidadBase.ahora());
    }

    @Override
    public List<Vuelo> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        return vueloRepositorio.findByFechaBajaIsNotNullOrderByFechaBajaDesc();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Vuelo reactivar(Usuario actor, Integer numero) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        bloqueoEscrituras.adquirir();
        Vuelo vuelo = vueloRepositorio.findById(numero).orElse(null);
        if (vuelo == null || vuelo.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay un vuelo dado de baja con el numero " + numero);
        }
        vuelo.reactivar();
        return vuelo;
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
