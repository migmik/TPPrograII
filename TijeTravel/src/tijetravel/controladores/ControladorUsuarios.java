package tijetravel.controladores;

import tijetravel.modelos.*;

//controlador para administrar usuarios
//valida permisos y operaciones
public class ControladorUsuarios {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion = new ControladorAutorizacion();

    public ControladorUsuarios(Agencia agencia) {
        this.agencia = agencia;
    }

    // agrega un usuario si tiene permiso y turista asignado
    public boolean agregar(Usuario actor, Usuario nuevo) {
        return permitido(actor) && nuevo != null && asociacionValida(nuevo.getRol(), nuevo.getCodigoTurista())
                && agencia.agregarUsuario(nuevo);
    }

    // modifica un usuario existente
    // verifica permiso, confirma que el usuario existe
    // evita duplicar nombres y no permite eliminar al ultimo administrador.
    public boolean modificar(Usuario actor, String actual, String nuevoNombre, String contrasenia,
            RolUsuario rol, Integer codigoTurista) {
        // no permite cambiar a rol invalido ni asociar incorrectamente a un cliente
        if (!permitido(actor) || rol == null || !asociacionValida(rol, codigoTurista))
            return false;
        Usuario objetivo = agencia.buscarUsuarioPorNombre(actual);
        Usuario existente = agencia.buscarUsuarioPorNombre(nuevoNombre);
        // si el nombre nuevo ya pertenece a otro usuario rechaza el cambio
        if (objetivo == null || (existente != null && existente != objetivo))
            return false;
        // evita dejar el sistema sin administrador
        if (objetivo.getRol() == RolUsuario.ADMINISTRADOR && rol != RolUsuario.ADMINISTRADOR
                && agencia.contarAdministradores() <= 1)
            return false;
        return objetivo.actualizarDatos(nuevoNombre, contrasenia, rol,
                rol == RolUsuario.CLIENTE ? codigoTurista : null);
    }

    // elimina un usuario por nombre, pero no permite eliminarse a si mismo
    public boolean eliminar(Usuario actor, String nombre) {
        if (!permitido(actor))
            return false;
        Usuario objetivo = agencia.buscarUsuarioPorNombre(nombre);
        return objetivo != null && objetivo != actor && agencia.eliminarUsuario(nombre);
    }

    // comprueba si el usuario tiene permisos para administrar
    private boolean permitido(Usuario u) {
        return autorizacion.tienePermiso(u, Permiso.ADMINISTRAR_USUARIOS);
    }

    // verifica la asociacion entre rol y turista
    // para un cliente necesita un codido de turista y titular
    // para otros roles no debe tener codigo de turista (null)
    private boolean asociacionValida(RolUsuario rol, Integer codigo) {
        if (rol != RolUsuario.CLIENTE)
            return codigo == null;
        Turista t = codigo == null ? null : agencia.buscarTuristaPorCodigo(codigo);
        return t != null && t.isEsTitular();
    }
}
