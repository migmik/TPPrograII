package tijetravel.models;

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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    public void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
    }
    
}
