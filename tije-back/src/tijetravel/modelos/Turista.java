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
    private Integer codigoSucursal;

    public Turista(int codigo, String nombre, String apellido, String direccion, String email, String telefonoFijo,
            String telefonoCelular) {
        this(codigo, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, true, null, null);
    }

    public boolean actualizarDatos(String nombre, String apellido, String direccion, String email,
            String telefonoFijo, String telefonoCelular, Integer codigoSucursal) {
        if (nombre == null || apellido == null || direccion == null || email == null
                || telefonoFijo == null || telefonoCelular == null)
            return false;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.email = email;
        this.telefonoFijo = telefonoFijo;
        this.telefonoCelular = telefonoCelular;
        this.codigoSucursal = codigoSucursal;
        return true;
    }

    public boolean actualizarDatos(String nombre, String apellido, String direccion, String email,
            String telefonoFijo, String telefonoCelular) {
        return actualizarDatos(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, codigoSucursal);
    }

    public Turista(int codigo, String nombre, String apellido, String direccion, String email, String telefonoFijo,
            String telefonoCelular, boolean esTitular, Integer codigoTitular) {
        this(codigo, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, esTitular, codigoTitular, null);
    }

    public Turista(int codigo, String nombre, String apellido, String direccion, String email, String telefonoFijo,
            String telefonoCelular, boolean esTitular, Integer codigoTitular, Integer codigoSucursal) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.email = email;
        this.telefonoFijo = telefonoFijo;
        this.telefonoCelular = telefonoCelular;
        this.esTitular = esTitular;
        this.codigoTitular = codigoTitular;
        this.codigoSucursal = codigoSucursal;
    }

    public int getCodigo() {
        return codigo;
    }

    private void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    private void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    private void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    private void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    public String getTelefonoFijo() {
        return telefonoFijo;
    }

    private void setTelefonoFijo(String telefonoFijo) {
        this.telefonoFijo = telefonoFijo;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    private void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public boolean isEsTitular() {
        return esTitular;
    }

    private void setEsTitular(boolean esTitular) {
        this.esTitular = esTitular;
    }

    public Integer getCodigoTitular() {
        return codigoTitular;
    }

    private void setCodigoTitular(Integer codigoTitular) {
        this.codigoTitular = codigoTitular;
    }

    public Integer getCodigoSucursal() {
        return codigoSucursal;
    }

    private void setCodigoSucursal(Integer codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }
}
