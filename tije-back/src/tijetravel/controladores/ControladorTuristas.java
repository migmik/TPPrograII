package tijetravel.controladores;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.Permiso;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;

public class ControladorTuristas {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion;

    public ControladorTuristas(Agencia agencia) {
        this.agencia = agencia;
        this.autorizacion = new ControladorAutorizacion();
    }

    public Turista agregarTitular(Usuario usuario, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular, int codigoSucursal) {
        if (!permitido(usuario))
            return null;
        if (agencia.buscarSucursalPorCodigo(codigoSucursal) == null)
            return null;
        Turista turista = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, true, null, codigoSucursal);
        return agencia.agregarTurista(turista) ? turista : null;
    }

    public Turista agregarTitular(Usuario usuario, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular) {
        if (agencia.getSucursales().isEmpty())
            return null;
        return agregarTitular(usuario, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular,
                agencia.getSucursales().get(0).getCodigo());
    }

    public Turista agregarFamiliar(Usuario usuario, int codigoTitular, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario))
            return null;
        Turista titular = agencia.buscarTuristaPorCodigo(codigoTitular);
        if (titular == null || !titular.isEsTitular())
            return null;
        Turista familiar = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, false, codigoTitular, titular.getCodigoSucursal());
        return agencia.agregarTurista(familiar) ? familiar : null;
    }

    public boolean modificar(Usuario usuario, int codigo, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular, Integer codigoSucursal) {
        if (!permitido(usuario))
            return false;
        if (codigoSucursal != null && agencia.buscarSucursalPorCodigo(codigoSucursal) == null)
            return false;
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);
        if (turista == null)
            return false;
        return turista.actualizarDatos(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular,
                codigoSucursal);
    }

    public boolean modificar(Usuario usuario, int codigo, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular) {
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);
        Integer codigoSucursal = turista == null ? null : turista.getCodigoSucursal();
        return modificar(usuario, codigo, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular,
                codigoSucursal);
    }

    public boolean eliminar(Usuario usuario, int codigo) {
        return permitido(usuario) && agencia.eliminarTurista(codigo);
    }

    private boolean permitido(Usuario usuario) {
        return autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_CLIENTES);
    }

    public boolean modificarNombre(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, v, t.getApellido(), t.getDireccion(), t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular(), t.getCodigoSucursal());
    }

    public boolean modificarApellido(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), v, t.getDireccion(), t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular(), t.getCodigoSucursal());
    }

    public boolean modificarDireccion(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), v, t.getEmail(), t.getTelefonoFijo(),
                t.getTelefonoCelular(), t.getCodigoSucursal());
    }

    public boolean modificarEmail(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), v, t.getTelefonoFijo(),
                t.getTelefonoCelular(), t.getCodigoSucursal());
    }

    public boolean modificarTelefonoFijo(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), t.getEmail(), v,
                t.getTelefonoCelular(), t.getCodigoSucursal());
    }

    public boolean modificarTelefonoCelular(Usuario u, int c, String v) {
        Turista t = agencia.buscarTuristaPorCodigo(c);
        return t != null && modificar(u, c, t.getNombre(), t.getApellido(), t.getDireccion(), t.getEmail(),
                t.getTelefonoFijo(), v, t.getCodigoSucursal());
    }

}
