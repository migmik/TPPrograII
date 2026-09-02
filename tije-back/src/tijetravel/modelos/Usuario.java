package tijetravel.modelos;

public abstract class Usuario {
    private String nombreUsuario;
    private String contrasenia;

    public Usuario(String nombreUsuario, String contrasenia) {
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
    }

    public boolean actualizarCredenciales(String nombreUsuario, String contrasenia) {
        if (nombreUsuario == null || contrasenia == null)
            return false;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        return true;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    private void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public abstract RolUsuario getRol();

    public Integer getCodigoTurista() {
        return null;
    }

    public abstract boolean tienePermiso(Permiso permiso);

}
