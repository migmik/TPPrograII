package tijetravel.modelos;

public class Sucursal {
    private int codigo;
    private String direccion;
    private String telefono;

    public Sucursal(int codigo, String direccion, String telefono) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public boolean actualizarDatos(String direccion, String telefono) {
        if (direccion == null || telefono == null) return false;
        this.direccion = direccion;
        this.telefono = telefono;
        return true;
    }

    public int getCodigo() {
        return codigo;
    }

    private void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    private void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    private void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
