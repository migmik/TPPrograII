package tijetravel.modelos;

public class Vendedor extends Usuario {
    public Vendedor(String nombreUsuario, String contrasenia) {
        super(nombreUsuario, contrasenia);
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.VENDEDOR;
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso == Permiso.CONSULTAR
                || permiso == Permiso.ADMINISTRAR_CLIENTES
                || permiso == Permiso.ADMINISTRAR_RESERVAS;
    }
}
