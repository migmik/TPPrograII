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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getPlazasDisponibles() {
        return plazasDisponibles;
    }

    public void setPlazasDisponibles(int plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

    public boolean tienePlazasDisponibles() {
        return plazasDisponibles > 0;
    }

    public boolean reservarPlaza() {
        if (!tienePlazasDisponibles()) {
            return false;
        }

        plazasDisponibles--;
        return true;
    }

    public void liberarPlaza() {
        plazasDisponibles++;
    }
}
