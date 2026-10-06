package com.tijetravel.tijeback.servicios;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class UsuarioServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final UsuarioFactory usuarioFactory;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final AutorizacionServicio autorizacion;
    private final PasswordEncoder codificadorContrasenias;

    public UsuarioServicio(
            UsuarioRepositorio usuarioRepositorio,
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            AutorizacionServicio autorizacion,
            PasswordEncoder codificadorContrasenias,
            BloqueoEscrituras bloqueoEscrituras, UsuarioFactory usuarioFactory) {
        this.usuarioFactory = usuarioFactory;
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.usuarioRepositorio = usuarioRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.autorizacion = autorizacion;
        this.codificadorContrasenias = codificadorContrasenias;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario crear(
            Usuario actor,
            String nombreUsuario,
            String contrasenia,
            RolUsuario rol,
            Integer codigoTurista, String dni) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        if (rol == null) {
            throw new IllegalArgumentException("El campo rol es obligatorio");
        }
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCase(nombreNormalizado)) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
        }

        if (rol != RolUsuario.CLIENTE && codigoTurista != null) {
            throw new IllegalArgumentException("Solo un cliente puede asociarse a un turista");
        }
        Turista turista = null;
        if (rol == RolUsuario.CLIENTE) {
            if (codigoTurista == null) {
                throw new IllegalArgumentException("Un cliente debe asociarse a un turista");
            }
            turista = turistaRepositorio.findById(codigoTurista)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No se encontro el turista " + codigoTurista));
            if (usuarioRepositorio.contarClientesPorTurista(codigoTurista) > 0) {
                throw new EntidadDuplicadaException("El turista ya tiene un usuario asociado");
            }
        }

        if (rol == RolUsuario.CLIENTE) {
            if (dni != null && !dni.isBlank()) {
                throw new IllegalArgumentException("El DNI del cliente se toma del turista asociado");
            }
            ValidacionModelo.dni(turista.getDni());
        } else {
            ValidacionModelo.dni(dni);
            if (usuarioRepositorio.existsByDni(dni)) {
                throw new EntidadDuplicadaException("Ya existe un empleado con ese DNI");
            }
        }
        Usuario usuario = usuarioFactory.crear(
                nombreNormalizado,
                codificarContrasenia(contrasenia),
                rol,
                turista, dni);
        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepositorio.findAll();
    }

    public List<Usuario> listarPara(Usuario actor) {
        return listarPara(actor, null);
    }

    public List<Usuario> listarPara(Usuario actor, RolUsuario rol) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return rol == null ? listar() : usuarioRepositorio.findByRol(rol);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario registrarCliente(
            String nombreUsuario,
            String contrasenia,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCase(nombreNormalizado)) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
        }
        String dniValidado = ValidacionModelo.dni(dni);
        if (turistaRepositorio.existsByDni(dniValidado)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
        String emailValidado = ValidacionModelo.email(email);
        if (turistaRepositorio.existsByEmailIgnoreCase(emailValidado)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        if (codigoSucursal == null || codigoSucursal < 1) {
            throw new IllegalArgumentException("El codigo de sucursal debe ser positivo");
        }
        var sucursal = sucursalRepositorio.findById(codigoSucursal)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro la sucursal " + codigoSucursal));

        Turista turista = turistaRepositorio.save(new Turista(
                dniValidado, nombre, apellido, direccion, emailValidado,
                telefonoFijo, telefonoCelular, sucursal));
        Usuario cliente = usuarioFactory.crear(
                nombreNormalizado,
                codificarContrasenia(contrasenia),
                RolUsuario.CLIENTE,
                turista,
                null);
        return usuarioRepositorio.save(cliente);
    }

    public Usuario encontrarPorId(Integer codigo) {
        return usuarioRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el usuario " + codigo));
    }

    public Usuario encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return encontrarPorId(codigo);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario modificarCredenciales(
            Usuario actor,
            Integer codigo,
            String nombreUsuario,
            String contrasenia, String dni) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndCodigoNot(nombreNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
        }

        Usuario usuario = encontrarPorId(codigo);
        if (usuario.getRol() == RolUsuario.CLIENTE) {
            if (dni != null && !dni.isBlank()) {
                throw new IllegalArgumentException("El DNI del cliente se modifica en su turista asociado");
            }
        } else {
            ValidacionModelo.dni(dni);
            if (usuarioRepositorio.existsByDniAndCodigoNot(dni, codigo)) {
                throw new EntidadDuplicadaException("Ya existe un empleado con ese DNI");
            }
            usuario.actualizarDni(dni);
        }
        usuario.actualizarCredenciales(
                nombreNormalizado,
                codificarContrasenia(contrasenia));
        return usuario;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Usuario usuario = encontrarPorId(codigo);
        if (esMismoUsuario(actor, usuario)) {
            throw new OperacionNoPermitidaException("Un usuario no puede eliminarse a si mismo");
        }
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR
                && usuarioRepositorio.countByRol(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new OperacionNoPermitidaException("No se puede eliminar el ultimo administrador");
        }
        usuarioRepositorio.delete(usuario);
    }

    private boolean esMismoUsuario(Usuario primero, Usuario segundo) {
        if (primero == segundo) {
            return true;
        }
        return primero.getCodigo() != null && primero.getCodigo().equals(segundo.getCodigo());
    }

    private String codificarContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new IllegalArgumentException("El campo contrasenia es obligatorio");
        }
        if (contrasenia.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contrasenia no puede superar 72 bytes");
        }
        return codificadorContrasenias.encode(contrasenia);
    }
}
