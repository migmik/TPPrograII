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
import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.RolRepositorio;
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
    private final RolRepositorio rolRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final AutorizacionServicio autorizacion;
    private final PasswordEncoder codificadorContrasenias;

    public UsuarioServicioImpl(
            UsuarioRepositorio usuarioRepositorio,
            RolRepositorio rolRepositorio,
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            AutorizacionServicio autorizacion,
            PasswordEncoder codificadorContrasenias,
            BloqueoEscrituras bloqueoEscrituras, UsuarioFactory usuarioFactory) {
        this.usuarioFactory = usuarioFactory;
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
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
            Integer codigoRol,
            Integer codigoTurista, String dni) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Rol rol = encontrarRol(codigoRol);
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        verificarNombreDisponible(nombreNormalizado, null);

        Turista turista = null;
        if (rol.getTipoUsuario() == TipoUsuario.CLIENTE) {
            if (codigoTurista == null) {
                throw new IllegalArgumentException("Un cliente debe asociarse a un turista");
            }
            if (dni != null && !dni.isBlank()) {
                throw new IllegalArgumentException("El DNI del cliente se toma del turista asociado");
            }
            turista = turistaRepositorio.findById(codigoTurista).orElse(null);
            if (turista == null || !turista.estaActivo()) {
                throw new EntidadNoEncontradaException("No se encontro el turista " + codigoTurista);
            }
            if (usuarioRepositorio.contarClientesPorTurista(codigoTurista) > 0) {
                throw new EntidadDuplicadaException("El turista ya tiene un usuario asociado");
            }
            ValidacionModelo.dni(turista.getDni());
        } else {
            if (codigoTurista != null) {
                throw new IllegalArgumentException("Solo un cliente puede asociarse a un turista");
            }
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
    public Page<Usuario> buscarPaginaPara(Usuario actor, Integer codigoRol, Integer codigo, String nombre,
            String dni, int pagina, int tamanio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        String nombreBuscado = FiltrosConsulta.texto(nombre);
        String dniBuscado = FiltrosConsulta.dni(dni);
        FiltrosConsulta.codigoPositivo(codigo);
        FiltrosConsulta.codigoPositivo(codigoRol);
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, Sort.by("codigo"));
        return usuarioRepositorio.buscarPagina(codigoRol, codigo, nombreBuscado, dniBuscado, paginado);
    }

    @Override
    public List<Usuario> listarPara(Usuario actor, Integer codigoRol) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        if (codigoRol == null) {
            return usuarioRepositorio.listarActivos();
        }
        return usuarioRepositorio.listarActivosPorRol(codigoRol);
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
        Rol rolCliente = rolRepositorio.findFirstByTipoUsuarioAndFechaBajaIsNullOrderByCodigo(TipoUsuario.CLIENTE);
        if (rolCliente == null) {
            throw new OperacionNoPermitidaException("El registro de clientes no esta disponible");
        }

        Turista turista = turistaRepositorio.save(new Turista(
                dniValidado, nombre, apellido, direccion, emailValidado,
                telefonoFijo, telefonoCelular, sucursal));
        Usuario cliente = usuarioFactory.crear(
                nombreNormalizado,
                codificarContrasenia(contrasenia),
                rolCliente,
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

    private Rol encontrarRol(Integer codigoRol) {
        if (codigoRol == null) {
            throw new IllegalArgumentException("El campo rol es obligatorio");
        }
        Rol rol = rolRepositorio.findById(codigoRol).orElse(null);
        if (rol == null || !rol.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro el rol " + codigoRol);
        }
        return rol;
    }

    @Override
    public Usuario encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return encontrarPorId(codigo);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Usuario modificar(
            Usuario actor,
            Integer codigo,
            String nombreUsuario,
            String contrasenia,
            String dni,
            Integer codigoRol) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        verificarNombreDisponible(nombreNormalizado, codigo);

        Usuario usuario = encontrarPorId(codigo);
        Rol rol = encontrarRol(codigoRol);
        if (esMismoUsuario(actor, usuario) && !rol.tienePermiso(Permiso.ADMINISTRAR_USUARIOS)) {
            throw new OperacionNoPermitidaException(
                    "No podes cambiarte a un rol sin el permiso de administrar usuarios");
        }
        if (usuario.tieneDniPropio()) {
            ValidacionModelo.dni(dni);
            verificarDniDisponible(dni, codigo);
        } else if (dni != null && !dni.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente se modifica en su turista asociado");
        }

        // Si no se escribe una contrasenia nueva, se conserva la actual.
        String contraseniaFinal = usuario.getContrasenia();
        if (contrasenia != null && !contrasenia.isBlank()) {
            contraseniaFinal = codificarContrasenia(contrasenia);
        }
        usuario.cambiarRol(rol);
        if (usuario.tieneDniPropio()) {
            usuario.actualizarDni(dni);
        }
        usuario.actualizarCredenciales(nombreNormalizado, contraseniaFinal);
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
        if (usuario.tienePermiso(Permiso.ADMINISTRAR_USUARIOS)
                && usuarioRepositorio.contarActivosConPermiso(Permiso.ADMINISTRAR_USUARIOS) <= 1) {
            throw new OperacionNoPermitidaException("Tiene que quedar al menos un usuario que administre usuarios");
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
        if (!usuario.getRol().estaActivo()) {
            throw new OperacionNoPermitidaException("El rol del usuario esta dado de baja");
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
