package com.tijetravel.tijeback.servicios.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.SucursalServicio;

@Service
@Transactional(readOnly = true)
public class SucursalServicioImpl implements SucursalServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final SucursalRepositorio sucursalRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final AutorizacionServicio autorizacion;

    public SucursalServicioImpl(
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            TuristaRepositorio turistaRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Sucursal crear(Usuario actor, String direccion, String telefono, String ciudad) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = new Sucursal(direccion, telefono, ciudad);
        if (sucursalRepositorio.existsByDireccionIgnoreCase(sucursal.getDireccion())) {
            throw new EntidadDuplicadaException("Ya existe una sucursal en esa direccion");
        }
        return sucursalRepositorio.save(sucursal);
    }

    @Override
    public List<Sucursal> listar(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            return sucursalRepositorio.findAll(Sort.by("codigo"));
        }
        return sucursalRepositorio.findByCiudadIgnoreCaseOrderByCodigo(ciudad.trim());
    }

    @Override
    public List<String> listarCiudades() {
        return sucursalRepositorio.listarCiudades();
    }

    @Override
    public Sucursal encontrarPorId(Integer codigo) {
        Sucursal sucursal = sucursalRepositorio.findById(codigo).orElse(null);
        if (sucursal == null) {
            throw new EntidadNoEncontradaException("No se encontro la sucursal " + codigo);
        }
        return sucursal;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Sucursal modificar(Usuario actor, Integer codigo, String direccion, String telefono, String ciudad) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        String direccionNormalizada = ValidacionModelo.textoObligatorio(direccion, "direccion");
        if (sucursalRepositorio.existsByDireccionIgnoreCaseAndCodigoNot(direccionNormalizada, codigo)) {
            throw new EntidadDuplicadaException("Ya existe una sucursal en esa direccion");
        }

        Sucursal sucursal = encontrarPorId(codigo);
        sucursal.actualizarDatos(direccionNormalizada, telefono, ciudad);
        return sucursal;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarPorId(codigo);
        if (reservaRepositorio.existsBySucursalContratacionCodigo(codigo)
                || turistaRepositorio.existsBySucursalContratacionCodigo(codigo)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar una sucursal vinculada a turistas o reservas");
        }
        sucursalRepositorio.delete(sucursal);
    }
}
