package com.tijetravel.tijeback.servicios;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
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
public class TuristaServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AutorizacionServicio autorizacion;

    public TuristaServicio(
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crear(Usuario actor, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular,
            Integer codigoSucursal, Integer codigoTitular) {
        if (codigoTitular == null) {
            if (codigoSucursal == null) {
                throw new IllegalArgumentException("El codigo de sucursal es obligatorio para un turista titular");
            }
            return crearTitular(actor, nombre, apellido, direccion, email,
                    telefonoFijo, telefonoCelular, codigoSucursal);
        }
        if (codigoSucursal != null) {
            throw new IllegalArgumentException("Un turista familiar hereda la sucursal del titular");
        }
        return crearFamiliar(actor, codigoTitular, nombre, apellido, direccion, email,
                telefonoFijo, telefonoCelular);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearTitular(
            Usuario actor,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        bloqueoEscrituras.adquirir();
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = new Turista(
                nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        verificarEmailDisponible(turista.getEmail());
        return turistaRepositorio.save(turista);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearFamiliar(
            Usuario actor,
            Integer codigoTitular,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular) {
        bloqueoEscrituras.adquirir();
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
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

    public List<Turista> listarPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        if (actor.getRol() != RolUsuario.CLIENTE) {
            return listar();
        }

        Integer codigoTitular = autorizacion.codigoTitular(actor);
        List<Turista> grupoFamiliar = new ArrayList<>();
        grupoFamiliar.add(encontrarPorId(codigoTitular));
        grupoFamiliar.addAll(turistaRepositorio.findByTitularCodigo(codigoTitular));
        return List.copyOf(grupoFamiliar);
    }

    public Turista encontrarPorId(Integer codigo) {
        return turistaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el turista " + codigo));
    }

    public Turista encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        Turista turista = encontrarPorId(codigo);
        if (actor.getRol() == RolUsuario.CLIENTE && !autorizacion.perteneceAlGrupoFamiliar(actor, turista)) {
            throw new OperacionNoPermitidaException(
                    "El cliente no puede consultar turistas de otro grupo familiar");
        }
        return turista;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
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
        bloqueoEscrituras.adquirir();
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = encontrarPorId(codigo);
        String emailNormalizado = normalizarTexto(email, "email");
        if (turistaRepositorio.existsByEmailIgnoreCaseAndCodigoNot(emailNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        turista.actualizarDatos(
                nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        if (turista.isTitular()) {
            turistaRepositorio.findByTitularCodigo(codigo)
                    .forEach(familiar -> familiar.cambiarSucursal(sucursal));
        }
        return turistaRepositorio.save(turista);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        bloqueoEscrituras.adquirir();
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
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
