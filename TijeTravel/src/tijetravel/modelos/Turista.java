package tijetravel.modelos;

public class Turista {
    private int codigo;
    private String nombre;
    private String apellido;
    private String direccion;
    private String email;
    private String telefonoFijo;
    private String telefonoCelular;
    private boolean esTitular;
    private Integer codigoTitular;
    
    public Turista(int codigo, String nombre, String apellido, String direccion, String email, String telefonoFijo,
            String telefonoCelular) {
        this(codigo, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, true, null);
    }

    public Turista(int codigo, String nombre, String apellido, String direccion, String email, String telefonoFijo,
            String telefonoCelular, boolean esTitular, Integer codigoTitular) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.email = email;
        this.telefonoFijo = telefonoFijo;
        this.telefonoCelular = telefonoCelular;
        this.esTitular = esTitular;
        this.codigoTitular = codigoTitular;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefonoFijo() {
        return telefonoFijo;
    }

    public void setTelefonoFijo(String telefonoFijo) {
        this.telefonoFijo = telefonoFijo;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public boolean isEsTitular() {
        return esTitular;
    }

    public void setEsTitular(boolean esTitular) {
        this.esTitular = esTitular;
    }

    public Integer getCodigoTitular() {
        return codigoTitular;
    }

    public void setCodigoTitular(Integer codigoTitular) {
        this.codigoTitular = codigoTitular;
    }
}
