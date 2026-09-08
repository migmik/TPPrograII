package com.tijetravel.tijeback.controladores;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class VuelosControlador {
    private final VueloRepositorio vueloRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionControlador autorizacion;

    public VuelosControlador(
            VueloRepositorio vueloRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionControlador autorizacion) {
        this.vueloRepositorio = vueloRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public Vuelo ingresar(
            Usuario actor,
            int numero,
            LocalDateTime fechaYHora,
            String origen,
            String destino,
            int totalPlazas,
            int plazasTurista,
            int plazasPrimera) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        if (vueloRepositorio.existsById(numero)) {
            throw new EntidadDuplicadaException("Ya existe el vuelo " + numero);
        }
        Vuelo vuelo = new Vuelo(
                numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera);
        return vueloRepositorio.save(vuelo);
    }

    public List<Vuelo> listar() {
        return vueloRepositorio.findAll();
    }

    public Vuelo encontrarPorId(Integer numero) {
        return vueloRepositorio.findById(numero)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el vuelo " + numero));
    }

    @Transactional
    public Vuelo modificar(
            Usuario actor,
            Integer numero,
            LocalDateTime fechaYHora,
            String origen,
            String destino,
            int totalPlazas,
            int plazasTurista,
            int plazasPrimera) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        Vuelo vuelo = encontrarPorId(numero);

        long reservasTurista = reservaRepositorio.countByVueloNumeroAndClaseVuelo(
                numero, ClaseVuelo.TURISTA);
        long reservasPrimera = reservaRepositorio.countByVueloNumeroAndClaseVuelo(
                numero, ClaseVuelo.PRIMERA);
        if (plazasTurista < reservasTurista || plazasPrimera < reservasPrimera) {
            throw new CapacidadExcedidaException(
                    "Las plazas no pueden ser menores que las reservas ya registradas");
        }

        vuelo.actualizarDatos(
                fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera);
        return vueloRepositorio.save(vuelo);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer numero) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_VUELOS);
        Vuelo vuelo = encontrarPorId(numero);
        if (reservaRepositorio.existsByVueloNumero(numero)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un vuelo que tiene reservas");
        }
        vueloRepositorio.delete(vuelo);
    }
}
