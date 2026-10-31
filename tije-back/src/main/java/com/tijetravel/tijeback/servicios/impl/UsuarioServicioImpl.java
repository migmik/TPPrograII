package com.tijetravel.tijeback.servicios.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.UsuarioServicio;
import com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory;

@Service
@Transactional(readOnly = true)
public class UsuarioServicioImpl implements UsuarioServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final UsuarioFactory usuarioFactory;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final AutorizacionServicio autorizacion;
    private final PasswordEncoder codificadorContrasenias;

    public UsuarioServicioImpl(
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

    @Override
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
        verificarNombreDisponible(nombreNormalizado, null);

        if (rol != RolUsuario.CLIENTE && codigoTurista != null) {
            throw new IllegalArgumentException("Solo un cliente puede asociarse a un turista");
        }
        Turista turista = null;
        if (rol == RolUsuario.CLIENTE) {
            if (codigoTurista == null) {
                throw new IllegalArgumentException("Un cliente debe asociarse a un turista");
            }
            turista = turistaRepositorio.findById(codigoTurista).orElse(null);
            if (turista == null || !turista.estaActivo()) {
                throw new EntidadNoEncontradaException("No se encontro el turista " + codigoTurista);
            }
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
            verificarDniDisponible(dni, null);
        }
        Usuario usuario = usuarioFactory.crear(
                nombreNormalizado,
                codificarContrasenia(contrasenia),
                rol,
                turista, dni);
        return usuarioRepositorio.save(usuario);
    }

    @Override
    public Page<Usuario> buscarPaginaPara(Usuario actor, RolUsuario rol, Integer codigo, String nombre, String dni,
            int pagina, int tamanio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        String nombreBuscado = FiltrosConsulta.texto(nombre);
        String dniBuscado = FiltrosConsulta.dni(dni);
        FiltrosConsulta.codigoPositivo(codigo);
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, Sort.by("codigo"));
        return usuarioRepositorio.buscarPagina(rol, codigo, nombreBuscado, dniBuscado, paginado);
    }

    @Override
    public List<Usuario> listarPara(Usuario actor, RolUsuario rol) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        if (rol == null) {
            return usuarioRepositorio.listarActivos();
        }
        return usuarioRepositorio.listarActivosPorRol(rol);
    }

    @Override
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
        verificarNombreDisponible(nombreNormalizado, null);
        // Si la persona estuvo registrada y se dio de baja, puede volver a registrarse.
        String dniValidado = ValidacionModelo.dni(dni);
        if (turistaRepositorio.existsByDniAndFechaBajaIsNull(dniValidado)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
        String emailValidado = ValidacionModelo.email(email);
        if (turistaRepositorio.existsByEmailIgnoreCaseAndFechaBajaIsNull(emailValidado)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        if (codigoSucursal == null || codigoSucursal < 1) {
            throw new IllegalArgumentException("El codigo de sucursal debe ser positivo");
        }
        Sucursal sucursal = sucursalRepositorio.findById(codigoSucursal).orElse(null);
        if (sucursal == null || !sucursal.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro la sucursal " + codigoSucursal);
        }

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

    private Usuario encontrarPorId(Integer codigo) {
        Usuario usuario = usuarioRepositorio.findById(codigo).orElse(null);
        if (usuario == null || !usuario.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro el usuario " + codigo);
        }
        return usuario;
    }

    @Override
    public Usuario encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return encontrarPorId(codigo);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario modificarCredenciales(
            Usuario actor,
            Integer codigo,
            String nombreUsuario,
            String contrasenia, String dni) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        verificarNombreDisponible(nombreNormalizado, codigo);

        Usuario usuario = encontrarPorId(codigo);
        if (usuario.tieneDniPropio()) {
            ValidacionModelo.dni(dni);
            verificarDniDisponible(dni, codigo);
            usuario.actualizarDni(dni);
        } else if (dni != null && !dni.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente se modifica en su turista asociado");
        }
        usuario.actualizarCredenciales(
                nombreNormalizado,
                codificarContrasenia(contrasenia));
        return usuario;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Usuario usuario = encontrarPorId(codigo);
        if (esMismoUsuario(actor, usuario)) {
            throw new OperacionNoPermitidaException("Un usuario no puede darse de baja a si mismo");
        }
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR
                && usuarioRepositorio.countByRolAndFechaBajaIsNull(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new OperacionNoPermitidaException("No se puede dar de baja al ultimo administrador");
        }
        usuario.darDeBaja(EntidadBase.ahora());
    }

    @Override
    public List<Usuario> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return usuarioRepositorio.listarDadosDeBaja();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario reactivar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Usuario usuario = usuarioRepositorio.findById(codigo).orElse(null);
        if (usuario == null || usuario.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay un usuario dado de baja con el codigo " + codigo);
        }
        verificarNombreDisponible(usuario.getNombreUsuario(), codigo);
        if (usuario.tieneDniPropio()) {
            verificarDniDisponible(usuario.getDni(), codigo);
        } else {
            if (!usuario.getTurista().estaActivo()) {
                throw new OperacionNoPermitidaException("El turista del cliente esta dado de baja");
            }
            if (usuarioRepositorio.contarClientesPorTurista(usuario.getCodigoTurista()) > 0) {
                throw new EntidadDuplicadaException("El turista ya tiene otro usuario activo");
            }
        }
        usuario.reactivar();
        return usuario;
    }

    private void verificarNombreDisponible(String nombreUsuario, Integer codigoPropio) {
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNull(nombreUsuario);
        } else {
            ocupado = usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNullAndCodigoNot(
                    nombreUsuario, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
        }
    }

    private void verificarDniDisponible(String dni, Integer codigoPropio) {
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = usuarioRepositorio.existsByDniAndFechaBajaIsNull(dni);
        } else {
            ocupado = usuarioRepositorio.existsByDniAndFechaBajaIsNullAndCodigoNot(dni, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe un empleado con ese DNI");
        }
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
