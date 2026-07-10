package tijetravel.modelos;

public class Usuario {
    private String nombreUsuario;
    private String contrasenia;
    private RolUsuario rol;
    private Integer codigoTurista;
    
    public Usuario(String nombreUsuario, String contrasenia, RolUsuario rol) {
        this(nombreUsuario, contrasenia, rol, null);
    }

    public Usuario(String nombreUsuario, String contrasenia, RolUsuario rol, Integer codigoTurista) {
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.codigoTurista = codigoTurista;
    }

    public boolean actualizarDatos(String nombreUsuario, String contrasenia, RolUsuario rol,
            Integer codigoTurista) {
        if (nombreUsuario == null || contrasenia == null || rol == null) return false;
        if (rol == RolUsuario.CLIENTE && codigoTurista == null) return false;
        if (rol != RolUsuario.CLIENTE && codigoTurista != null) return false;
        this.nombreUsuario = nombreUsuario; this.contrasenia = contrasenia;
        this.rol = rol; this.codigoTurista = codigoTurista;
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

    private void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public RolUsuario getRol() {
        return rol;
    }

    private void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    private void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
    }
    
}
