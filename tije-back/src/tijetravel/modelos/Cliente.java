package tijetravel.modelos;

public class Cliente extends Usuario {
    private Integer codigoTurista;

    public Cliente(String nombreUsuario, String contrasenia, Integer codigoTurista) {
        super(nombreUsuario, contrasenia);
        this.codigoTurista = codigoTurista;
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.CLIENTE;
    }

    @Override
    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso == Permiso.CONSULTAR;
    }
}
