package com.tijetravel.tijeback.servicios.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
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
    private final TuristaRepositorio turistaRepositorio;
    private final AutorizacionServicio autorizacion;

    public SucursalServicioImpl(
            SucursalRepositorio sucursalRepositorio,
            TuristaRepositorio turistaRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.sucursalRepositorio = sucursalRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Sucursal crear(Usuario actor, String direccion, String telefono, String ciudad) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = new Sucursal(direccion, telefono, ciudad);
        verificarDireccionDisponible(sucursal.getDireccion(), null);
        return sucursalRepositorio.save(sucursal);
    }

    @Override
    public List<Sucursal> listar(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            return sucursalRepositorio.findByFechaBajaIsNullOrderByCodigo();
        }
        return sucursalRepositorio.findByCiudadIgnoreCaseAndFechaBajaIsNullOrderByCodigo(ciudad.trim());
    }

    @Override
    public List<String> listarCiudades() {
        return sucursalRepositorio.listarCiudades();
    }

    @Override
    public Sucursal encontrarPorId(Integer codigo) {
        Sucursal sucursal = sucursalRepositorio.findById(codigo).orElse(null);
        if (sucursal == null || !sucursal.estaActivo()) {
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
        verificarDireccionDisponible(direccionNormalizada, codigo);

        Sucursal sucursal = encontrarPorId(codigo);
        sucursal.actualizarDatos(direccionNormalizada, telefono, ciudad);
        return sucursal;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarPorId(codigo);
        if (turistaRepositorio.existsBySucursalContratacionCodigoAndFechaBajaIsNull(codigo)) {
            throw new OperacionNoPermitidaException(
                    "No se puede dar de baja una sucursal que tiene turistas activos");
        }
        sucursal.darDeBaja(EntidadBase.ahora());
    }

    @Override
    public List<Sucursal> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        return sucursalRepositorio.findByFechaBajaIsNotNullOrderByFechaBajaDesc();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Sucursal reactivar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = sucursalRepositorio.findById(codigo).orElse(null);
        if (sucursal == null || sucursal.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay una sucursal dada de baja con el codigo " + codigo);
        }
        verificarDireccionDisponible(sucursal.getDireccion(), codigo);
        sucursal.reactivar();
        return sucursal;
    }

    private void verificarDireccionDisponible(String direccion, Integer codigoPropio) {
        boolean ocupada;
        if (codigoPropio == null) {
            ocupada = sucursalRepositorio.existsByDireccionIgnoreCaseAndFechaBajaIsNull(direccion);
        } else {
            ocupada = sucursalRepositorio.existsByDireccionIgnoreCaseAndFechaBajaIsNullAndCodigoNot(
                    direccion, codigoPropio);
        }
        if (ocupada) {
            throw new EntidadDuplicadaException("Ya existe una sucursal en esa direccion");
        }
    }
}
