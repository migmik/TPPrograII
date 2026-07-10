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
            String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario)) return null;
        Turista turista = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, true, null);
        return agencia.agregarTurista(turista) ? turista : null;
    }

    public Turista agregarFamiliar(Usuario usuario, int codigoTitular, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario)) return null;
        Turista titular = agencia.buscarTuristaPorCodigo(codigoTitular);
        if (titular == null || !titular.isEsTitular()) return null;
        Turista familiar = new Turista(agencia.generarCodigoTurista(), nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular, false, codigoTitular);
        return agencia.agregarTurista(familiar) ? familiar : null;
    }

    public boolean modificar(Usuario usuario, int codigo, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular) {
        if (!permitido(usuario)) return false;
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);
        if (turista == null) return false;
        return turista.actualizarDatos(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular);
    }

    public boolean eliminar(Usuario usuario, int codigo) {
        return permitido(usuario) && agencia.eliminarTurista(codigo);
    }

    public boolean modificarNombre(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,v,t.getApellido(),t.getDireccion(),t.getEmail(),t.getTelefonoFijo(),t.getTelefonoCelular());}
    public boolean modificarApellido(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,t.getNombre(),v,t.getDireccion(),t.getEmail(),t.getTelefonoFijo(),t.getTelefonoCelular());}
    public boolean modificarDireccion(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,t.getNombre(),t.getApellido(),v,t.getEmail(),t.getTelefonoFijo(),t.getTelefonoCelular());}
    public boolean modificarEmail(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,t.getNombre(),t.getApellido(),t.getDireccion(),v,t.getTelefonoFijo(),t.getTelefonoCelular());}
    public boolean modificarTelefonoFijo(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,t.getNombre(),t.getApellido(),t.getDireccion(),t.getEmail(),v,t.getTelefonoCelular());}
    public boolean modificarTelefonoCelular(Usuario u,int c,String v){Turista t=agencia.buscarTuristaPorCodigo(c);return t!=null&&modificar(u,c,t.getNombre(),t.getApellido(),t.getDireccion(),t.getEmail(),t.getTelefonoFijo(),v);}

    private boolean permitido(Usuario usuario) {
        return autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_CLIENTES);
    }
}
