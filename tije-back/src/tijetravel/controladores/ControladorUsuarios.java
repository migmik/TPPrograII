package tijetravel.controladores;

import tijetravel.modelos.*;

public class ControladorUsuarios {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion = new ControladorAutorizacion();

    public ControladorUsuarios(Agencia agencia) {
        this.agencia = agencia;
    }

    public boolean agregar(Usuario actor, Usuario nuevo) {
        return permitido(actor) && nuevo != null && asociacionValida(nuevo.getRol(), nuevo.getCodigoTurista())
                && agencia.agregarUsuario(nuevo);
    }

    public boolean modificar(Usuario actor, String actual, String nuevoNombre, String contrasenia,
            RolUsuario rol, Integer codigoTurista) {
        if (!permitido(actor) || rol == null || !asociacionValida(rol, codigoTurista))
            return false;
        Usuario objetivo = agencia.buscarUsuarioPorNombre(actual);
        Usuario existente = agencia.buscarUsuarioPorNombre(nuevoNombre);
        if (objetivo == null || (existente != null && existente != objetivo))
            return false;
        if (objetivo.getRol() == RolUsuario.ADMINISTRADOR && rol != RolUsuario.ADMINISTRADOR
                && agencia.contarAdministradores() <= 1)
            return false;
        Usuario modificado = UsuarioFactory.crear(nuevoNombre, contrasenia, rol,
                rol == RolUsuario.CLIENTE ? codigoTurista : null);
        return modificado != null && agencia.reemplazarUsuario(actual, modificado);
    }

    public boolean eliminar(Usuario actor, String nombre) {
        if (!permitido(actor))
            return false;
        Usuario objetivo = agencia.buscarUsuarioPorNombre(nombre);
        return objetivo != null && objetivo != actor && agencia.eliminarUsuario(nombre);
    }

    private boolean permitido(Usuario u) {
        return autorizacion.tienePermiso(u, Permiso.ADMINISTRAR_USUARIOS);
    }

    private boolean asociacionValida(RolUsuario rol, Integer codigo) {
        if (rol != RolUsuario.CLIENTE)
            return codigo == null;
        Turista t = codigo == null ? null : agencia.buscarTuristaPorCodigo(codigo);
        return t != null && t.isEsTitular();
    }
}
