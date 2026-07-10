package tijetravel.modelos;

public class Hotel {
    private int codigo;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private int plazasDisponibles;

    public Hotel(int codigo, String nombre, String direccion, String ciudad, String telefono, int plazasDisponibles) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.plazasDisponibles = plazasDisponibles;
    }

    public boolean actualizarDatos(String nombre, String direccion, String ciudad, String telefono,
            int plazasDisponibles) {
        if (nombre == null || direccion == null || ciudad == null || telefono == null || plazasDisponibles < 0)
            return false;
        this.nombre = nombre; this.direccion = direccion; this.ciudad = ciudad;
        this.telefono = telefono; this.plazasDisponibles = plazasDisponibles;
        return true;
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

    public String getDireccion() {
        return direccion;
    }

    private void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    private void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    private void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getPlazasDisponibles() {
        return plazasDisponibles;
    }

    private void setPlazasDisponibles(int plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

}
