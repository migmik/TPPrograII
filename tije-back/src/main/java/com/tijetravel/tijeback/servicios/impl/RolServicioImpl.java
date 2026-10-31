package com.tijetravel.tijeback.servicios.impl;

import java.util.ArrayList;
import java.util.List;

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
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.RolRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.RolServicio;

@Service
@Transactional(readOnly = true)
public class RolServicioImpl implements RolServicio {
    private final RolRepositorio rolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AutorizacionServicio autorizacion;
    private final BloqueoEscrituras bloqueoEscrituras;

    public RolServicioImpl(
            RolRepositorio rolRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.rolRepositorio = rolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.autorizacion = autorizacion;
        this.bloqueoEscrituras = bloqueoEscrituras;
    }

    @Override
    public List<Rol> listarPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return rolRepositorio.findByFechaBajaIsNullOrderByCodigo();
    }

    @Override
    public List<Permiso> listarPermisos() {
        List<Permiso> permisos = new ArrayList<>();
        for (Permiso permiso : Permiso.values()) {
            permisos.add(permiso);
        }
        return permisos;
    }

    @Override
    public Rol encontrarPara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return encontrarActivo(codigo);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Rol crear(Usuario actor, String nombre, TipoUsuario tipoUsuario, List<Permiso> permisos) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Rol rol = new Rol(nombre, tipoUsuario, permisos);
        verificarNombreDisponible(rol.getNombre(), null);
        return rolRepositorio.save(rol);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Rol modificar(Usuario actor, Integer codigo, String nombre, List<Permiso> permisos) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Rol rol = encontrarActivo(codigo);
        String nombreValidado = ValidacionModelo.textoObligatorio(nombre, "nombre");
        verificarNombreDisponible(nombreValidado, codigo);
        if (permisos == null) {
            throw new IllegalArgumentException("El campo permisos es obligatorio");
        }
        // Si el actor se quita este permiso a si mismo, podria quedar sin poder volver a darselo.
        if (codigo.equals(actor.getRol().getCodigo()) && !permisos.contains(Permiso.ADMINISTRAR_USUARIOS)) {
            throw new OperacionNoPermitidaException(
                    "No podes quitarle a tu propio rol el permiso de administrar usuarios");
        }
        rol.actualizar(nombreValidado, permisos);
        return rol;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Rol rol = encontrarActivo(codigo);
        if (usuarioRepositorio.countByRolCodigoAndFechaBajaIsNull(codigo) > 0) {
            throw new OperacionNoPermitidaException(
                    "No se puede dar de baja un rol que tienen usuarios activos; primero cambiales el rol");
        }
        // El registro publico necesita al menos un rol de clientes.
        if (rol.getTipoUsuario() == TipoUsuario.CLIENTE
                && rolRepositorio.countByTipoUsuarioAndFechaBajaIsNull(TipoUsuario.CLIENTE) <= 1) {
            throw new OperacionNoPermitidaException("Tiene que quedar al menos un rol de clientes");
        }
        rol.darDeBaja(EntidadBase.ahora());
    }

    @Override
    public List<Rol> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return rolRepositorio.findByFechaBajaIsNotNullOrderByFechaBajaDesc();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Rol reactivar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        bloqueoEscrituras.adquirir();
        Rol rol = rolRepositorio.findById(codigo).orElse(null);
        if (rol == null || rol.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay un rol dado de baja con el codigo " + codigo);
        }
        verificarNombreDisponible(rol.getNombre(), codigo);
        rol.reactivar();
        return rol;
    }

    private Rol encontrarActivo(Integer codigo) {
        Rol rol = rolRepositorio.findById(codigo).orElse(null);
        if (rol == null || !rol.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro el rol " + codigo);
        }
        return rol;
    }

    private void verificarNombreDisponible(String nombre, Integer codigoPropio) {
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = rolRepositorio.existsByNombreIgnoreCaseAndFechaBajaIsNull(nombre);
        } else {
            ocupado = rolRepositorio.existsByNombreIgnoreCaseAndFechaBajaIsNullAndCodigoNot(nombre, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe un rol con ese nombre");
        }
    }
}
