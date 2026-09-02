package tijetravel.modelos;

public class UsuarioFactory {
    private UsuarioFactory() {
    }

    public static Usuario crear(String nombreUsuario, String contrasenia, RolUsuario rol, Integer codigoTurista) {
        if (nombreUsuario == null || contrasenia == null || rol == null) {
            return null;
        }

        switch (rol) {
            case CLIENTE:
                if (codigoTurista == null) {
                    return null;
                }
                return new Cliente(nombreUsuario, contrasenia, codigoTurista);
            case VENDEDOR:
                if (codigoTurista != null) {
                    return null;
                }
                return new Vendedor(nombreUsuario, contrasenia);
            case ADMINISTRADOR:
                if (codigoTurista != null) {
                    return null;
                }
                return new Administrador(nombreUsuario, contrasenia);
            default:
                return null;
        }
    }
}
