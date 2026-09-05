package com.tijetravel.tijeback.controladores;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class TuristasControlador {
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AutorizacionControlador autorizacion;

    public TuristasControlador(
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            AutorizacionControlador autorizacion) {
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public Turista ingresarTitular(
            Usuario actor,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_CLIENTES);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = new Turista(
                nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        verificarEmailDisponible(turista.getEmail());
        return turistaRepositorio.save(turista);
    }

    @Transactional
    public Turista ingresarFamiliar(
            Usuario actor,
            Integer codigoTitular,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_CLIENTES);
        Turista titular = encontrarPorId(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }

        Turista familiar = new Turista(
                nombre,
                apellido,
                direccion,
                email,
                telefonoFijo,
                telefonoCelular,
                titular.getSucursalContratacion(),
                titular);
        verificarEmailDisponible(familiar.getEmail());
        return turistaRepositorio.save(familiar);
    }

    public List<Turista> listar() {
        return turistaRepositorio.findAll();
    }

    public List<Turista> listarFamiliares(Integer codigoTitular) {
        Turista titular = encontrarPorId(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }
        return turistaRepositorio.findByTitularCodigo(codigoTitular);
    }

    public Turista encontrarPorId(Integer codigo) {
        return turistaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el turista " + codigo));
    }

    @Transactional
    public Turista modificar(
            Usuario actor,
            Integer codigo,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_CLIENTES);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = encontrarPorId(codigo);
        String emailNormalizado = normalizarTexto(email, "email");
        if (turistaRepositorio.existsByEmailIgnoreCaseAndCodigoNot(emailNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        turista.actualizarDatos(
                nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        return turistaRepositorio.save(turista);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_CLIENTES);
        Turista turista = encontrarPorId(codigo);
        if (reservaRepositorio.existsByTuristaCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un turista que tiene reservas");
        }
        if (turistaRepositorio.existsByTitularCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un titular que tiene familiares");
        }
        if (usuarioRepositorio.contarClientesPorTurista(codigo) > 0) {
            throw new OperacionNoPermitidaException("No se puede eliminar un turista asociado a un usuario");
        }
        turistaRepositorio.delete(turista);
    }

    private Sucursal encontrarSucursal(Integer codigo) {
        return sucursalRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro la sucursal " + codigo));
    }

    private void verificarEmailDisponible(String email) {
        if (turistaRepositorio.findByEmailIgnoreCase(email).isPresent()) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
    }

    private String normalizarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }
}
