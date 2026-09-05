package com.tijetravel.tijeback.controladores;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;

@Service
@Transactional(readOnly = true)
public class SucursalesControlador {
    private final SucursalRepositorio sucursalRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final AutorizacionControlador autorizacion;

    public SucursalesControlador(
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            TuristaRepositorio turistaRepositorio,
            AutorizacionControlador autorizacion) {
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public Sucursal ingresar(Usuario actor, String direccion, String telefono) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        Sucursal sucursal = new Sucursal(direccion, telefono);
        if (sucursalRepositorio.existsByDireccionIgnoreCase(sucursal.getDireccion())) {
            throw new EntidadDuplicadaException("Ya existe una sucursal en esa direccion");
        }
        return sucursalRepositorio.save(sucursal);
    }

    public List<Sucursal> listar() {
        return sucursalRepositorio.findAll();
    }

    public Sucursal encontrarPorId(Integer codigo) {
        return sucursalRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro la sucursal " + codigo));
    }

    @Transactional
    public Sucursal modificar(Usuario actor, Integer codigo, String direccion, String telefono) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        Sucursal propuesta = new Sucursal(direccion, telefono);
        if (sucursalRepositorio.existsByDireccionIgnoreCaseAndCodigoNot(propuesta.getDireccion(), codigo)) {
            throw new EntidadDuplicadaException("Ya existe una sucursal en esa direccion");
        }

        Sucursal sucursal = encontrarPorId(codigo);
        sucursal.actualizarDatos(direccion, telefono);
        return sucursalRepositorio.save(sucursal);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_SUCURSALES);
        Sucursal sucursal = encontrarPorId(codigo);
        if (reservaRepositorio.existsBySucursalCodigo(codigo)
                || turistaRepositorio.existsBySucursalContratacionCodigo(codigo)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar una sucursal vinculada a turistas o reservas");
        }
        sucursalRepositorio.delete(sucursal);
    }
}
