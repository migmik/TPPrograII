package com.tijetravel.tijeback.controladores;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.UsuarioFactory;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class UsuariosControlador {
    private final UsuarioRepositorio usuarioRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final AutorizacionControlador autorizacion;
    private final PasswordEncoder codificadorContrasenias;

    public UsuariosControlador(
            UsuarioRepositorio usuarioRepositorio,
            TuristaRepositorio turistaRepositorio,
            AutorizacionControlador autorizacion,
            PasswordEncoder codificadorContrasenias) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.autorizacion = autorizacion;
        this.codificadorContrasenias = codificadorContrasenias;
    }

    @Transactional
    public Usuario ingresar(
            Usuario actor,
            String nombreUsuario,
            String contrasenia,
            RolUsuario rol,
            Integer codigoTurista) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        if (rol == null) {
            throw new IllegalArgumentException("El campo rol es obligatorio");
        }
        String nombreNormalizado = normalizarNombreUsuario(nombreUsuario);
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCase(nombreNormalizado)) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
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

        Usuario usuario = UsuarioFactory.crear(
                nombreUsuario,
                codificarContrasenia(contrasenia),
                rol,
                turista);
        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepositorio.findAll();
    }

    public List<Usuario> listarPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        List<Usuario> usuarios = listar();
        usuarios.forEach(Usuario::getCodigoTurista);
        return usuarios;
    }

    public Usuario encontrarPorId(Integer codigo) {
        return usuarioRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el usuario " + codigo));
    }

    public Usuario encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        Usuario usuario = encontrarPorId(codigo);
        usuario.getCodigoTurista();
        return usuario;
    }

    public Usuario encontrarPorNombre(String nombreUsuario) {
        String nombreNormalizado = normalizarNombreUsuario(nombreUsuario);
        return usuarioRepositorio.findByNombreUsuarioIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro el usuario " + nombreNormalizado));
    }

    @Transactional
    public Usuario modificarCredenciales(
            Usuario actor,
            Integer codigo,
            String nombreUsuario,
            String contrasenia) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        String nombreNormalizado = normalizarNombreUsuario(nombreUsuario);
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndCodigoNot(nombreNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe el nombre de usuario indicado");
        }

        Usuario usuario = encontrarPorId(codigo);
        usuario.actualizarCredenciales(
                nombreUsuario,
                codificarContrasenia(contrasenia));
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
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

    private String normalizarNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("El campo nombreUsuario es obligatorio");
        }
        return nombreUsuario.trim();
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
