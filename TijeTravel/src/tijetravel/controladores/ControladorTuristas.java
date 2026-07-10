package tijetravel.controladores;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.Permiso;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;

//controlador para gestionar los turistas
//permite agregar editar y eliminar titulares y familiares
public class ControladorTuristas {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion;

    public ControladorTuristas(Agencia agencia) {
        this.agencia = agencia;
        this.autorizacion = new ControladorAutorizacion();
    }

    // agrega si tiene permisos
    public Turista agregarTitular(Usuario usuario, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario))
            return null;
        Turista turista = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, true, null);
        return agencia.agregarTurista(turista) ? turista : null;
    }

    // agrega un familiar a un titular, rechaza si el titular no existe
    public Turista agregarFamiliar(Usuario usuario, int codigoTitular, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario))
            return null;
        Turista titular = agencia.buscarTuristaPorCodigo(codigoTitular);
        if (titular == null || !titular.isEsTitular())
            return null;
        Turista familiar = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, false, codigoTitular);
        return agencia.agregarTurista(familiar) ? familiar : null;
    }

    // si tiene permisos puede modificar a turista
    public boolean modificar(Usuario usuario, int codigo, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario))
            return false;
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);
        if (turista == null)
            return false;
        return turista.actualizarDatos(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular);
    }

    public boolean eliminar(Usuario usuario, int codigo) {
        return permitido(usuario) && agencia.eliminarTurista(codigo);
    }

    private boolean permitido(Usuario usuario) {
        return autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_CLIENTES);
    }

    // de aca para abajo metodos auuxiliares para editar un solo campo del turista
    // usa logica de modificar pero conservando el resto de los campos
    public boolean modificarNombre(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, v, t.getApellido(), t.getDireccion(), t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular());
    }

    public boolean modificarApellido(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), v, t.getDireccion(), t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular());
    }

    public boolean modificarDireccion(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), v, t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular());
    }

    public boolean modificarEmail(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), v, t.getTelefonoFijo(),
                t.getTelefonoCelular());
    }

    public boolean modificarTelefonoFijo(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), t.getEmail(), v,
                t.getTelefonoCelular());
    }

    public boolean modificarTelefonoCelular(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), t.getEmail(),
                t.getTelefonoFijo(), v);
    }

}
