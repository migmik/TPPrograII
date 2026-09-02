package tijetravel.modelos;

public class Administrador extends Usuario {
    public Administrador(String nombreUsuario, String contrasenia) {
        super(nombreUsuario, contrasenia);
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.ADMINISTRADOR;
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso != null;
    }
}
