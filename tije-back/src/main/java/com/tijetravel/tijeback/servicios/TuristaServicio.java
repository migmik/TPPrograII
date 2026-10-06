package com.tijetravel.tijeback.servicios;

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
import com.tijetravel.tijeback.modelos.ValidacionModelo;
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
    public Turista crear(Usuario actor, String dni, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular,
            Integer codigoSucursal, Integer codigoTitular) {
        if (codigoTitular == null) {
            if (codigoSucursal == null) {
                throw new IllegalArgumentException("El codigo de sucursal es obligatorio para un turista titular");
            }
            return crearTitular(actor, dni, nombre, apellido, direccion, email,
                    telefonoFijo, telefonoCelular, codigoSucursal);
        }
        if (codigoSucursal != null) {
            throw new IllegalArgumentException("Un turista familiar hereda la sucursal del titular");
        }
        return crearFamiliar(actor, codigoTitular, dni, nombre, apellido, direccion, email,
                telefonoFijo, telefonoCelular);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearTitular(
            Usuario actor,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = new Turista(
                dni, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        verificarDniDisponible(turista.getDni());
        verificarEmailDisponible(turista.getEmail());
        return turistaRepositorio.save(turista);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearFamiliar(
            Usuario actor,
            Integer codigoTitular,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Turista titular = encontrarPorId(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }

        Turista familiar = new Turista(
                dni,
                nombre,
                apellido,
                direccion,
                email,
                telefonoFijo,
                telefonoCelular,
                titular.getSucursalContratacion(),
                titular);
        verificarDniDisponible(familiar.getDni());
        verificarEmailDisponible(familiar.getEmail());
        return turistaRepositorio.save(familiar);
    }

    public List<Turista> listar() {
        return turistaRepositorio.findAll();
    }

    public List<Turista> listarPara(Usuario actor) {
        return listarPara(actor, null);
    }

    public List<Turista> listarPara(Usuario actor, String dni) {
        return listarPara(actor, dni, null);
    }

    public List<Turista> listarPara(Usuario actor, String dni, Boolean titular) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        String dniBusqueda = dni == null || dni.isBlank() ? null : dni.trim();
        if (actor.getRol() != RolUsuario.CLIENTE) {
            List<Turista> turistas;
            if (dniBusqueda != null) {
                turistas = turistaRepositorio.findByDni(dniBusqueda);
            } else if (titular == null) {
                return listar();
            } else if (titular) {
                turistas = turistaRepositorio.findByTitularIsNull();
            } else {
                turistas = turistaRepositorio.findByTitularIsNotNull();
            }
            if (titular != null && dniBusqueda != null) {
                return filtrarPorTipo(turistas, titular);
            }
            return turistas;
        }

        Integer codigoTitular = autorizacion.codigoTitular(actor);
        List<Turista> grupoFamiliar = turistaRepositorio.findByCodigoOrTitularCodigo(codigoTitular, codigoTitular);
        if (grupoFamiliar.isEmpty()) {
            throw new EntidadNoEncontradaException("No se encontro el turista " + codigoTitular);
        }
        List<Turista> turistasVisibles = dniBusqueda == null ? grupoFamiliar
                : grupoFamiliar.stream().filter(turista -> dniBusqueda.equals(turista.getDni())).toList();
        return titular == null ? List.copyOf(turistasVisibles) : filtrarPorTipo(turistasVisibles, titular);
    }

    public List<Turista> listarPorCodigosPara(Usuario actor, List<Integer> codigos) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        if (codigos == null || codigos.isEmpty()) {
            return List.of();
        }
        List<Turista> turistas = turistaRepositorio.findByCodigoIn(codigos);
        if (actor.getRol() != RolUsuario.CLIENTE) {
            return turistas;
        }
        return turistas.stream()
                .filter(turista -> autorizacion.perteneceAlGrupoFamiliar(actor, turista))
                .toList();
    }

    private List<Turista> filtrarPorTipo(List<Turista> turistas, boolean titular) {
        return turistas.stream().filter(turista -> turista.isTitular() == titular).toList();
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
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = encontrarPorId(codigo);
        String dniValidado = ValidacionModelo.dni(dni);
        if (turistaRepositorio.existsByDniAndCodigoNot(dniValidado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
        String emailNormalizado = ValidacionModelo.email(email);
        if (turistaRepositorio.existsByEmailIgnoreCaseAndCodigoNot(emailNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        turista.actualizarDatos(
                dniValidado, nombre, apellido, direccion, emailNormalizado, telefonoFijo, telefonoCelular, sucursal);
        if (turista.isTitular()) {
            turistaRepositorio.findByTitularCodigo(codigo)
                    .forEach(familiar -> familiar.cambiarSucursal(sucursal));
        }
        return turista;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
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

    private void verificarDniDisponible(String dni) {
        if (turistaRepositorio.existsByDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
    }

    private void verificarEmailDisponible(String email) {
        if (turistaRepositorio.existsByEmailIgnoreCase(email)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
    }

}
