package tijetravel.controladores;

import java.time.LocalDateTime;
import tijetravel.modelos.*;

//controlador para operaciones del administrador
public class ControladorAdministracion {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion = new ControladorAutorizacion();
    private ControladorReservas reservas;

    // constructor: tambien crea un controlador de reservas para cambios de
    // capacidad
    public ControladorAdministracion(Agencia agencia) {
        this.agencia = agencia;
        this.reservas = new ControladorReservas(agencia);
    }

    // agrega sucursal si el usuario tiene los persimos
    public boolean agregarSucursal(Usuario u, Sucursal s) {
        return puede(u, Permiso.ADMINISTRAR_SUCURSALES) && agencia.agregarSucursal(s);
    }

    // administrador puede modificar sucursal si existe
    public boolean modificarSucursal(Usuario u, int c, String d, String t) {
        if (!puede(u, Permiso.ADMINISTRAR_SUCURSALES))
            return false;
        Sucursal s = agencia.buscarSucursalPorCodigo(c);
        return s != null && s.actualizarDatos(d, t);
    }

    public boolean eliminarSucursal(Usuario u, int c) {
        return puede(u, Permiso.ADMINISTRAR_SUCURSALES) && agencia.eliminarSucursal(c);
    }

    public boolean agregarHotel(Usuario u, Hotel h) {
        return puede(u, Permiso.ADMINISTRAR_HOTELES) && valido(h) && agencia.agregarHotel(h);
    }

    // permite modificar solo si la nueva capacidad no es menor a la ocupacion
    // actual
    public boolean modificarHotel(Usuario u, int c, String n, String d, String ciudad, String tel, int plazas) {
        if (!puede(u, Permiso.ADMINISTRAR_HOTELES) || plazas < reservas.ocupacionMaximaHotel(c))
            return false;
        Hotel h = agencia.buscarHotelPorCodigo(c);
        if (h == null)
            return false;
        return h.actualizarDatos(n, d, ciudad, tel, plazas);
    }

    public boolean eliminarHotel(Usuario u, int c) {
        return puede(u, Permiso.ADMINISTRAR_HOTELES) && agencia.eliminarHotel(c);
    }

    public boolean agregarVuelo(Usuario u, Vuelo v) {
        return puede(u, Permiso.ADMINISTRAR_VUELOS) && valido(v) && agencia.agregarVuelo(v);
    }

    // modifica solo si las plazas no son menores que las reservas actuales
    public boolean modificarVuelo(Usuario u, int n, LocalDateTime f, String o, String d, int total, int turista,
            int primera) {
        if (!puede(u, Permiso.ADMINISTRAR_VUELOS) || total <= 0
                || turista < reservas.contarReservasVuelo(n, ClaseVuelo.TURISTA)
                || primera < reservas.contarReservasVuelo(n, ClaseVuelo.PRIMERA) || turista + primera > total)
            return false;
        Vuelo v = agencia.buscarVueloPorNumero(n);
        return v != null && v.actualizarDatos(f, o, d, total, turista, primera);
    }

    public boolean eliminarVuelo(Usuario u, int n) {
        return puede(u, Permiso.ADMINISTRAR_VUELOS) && agencia.eliminarVuelo(n);
    }

    private boolean puede(Usuario u, Permiso p) {
        return autorizacion.tienePermiso(u, p);
    }

    private boolean valido(Hotel h) {
        return h != null && h.getPlazasDisponibles() >= 0;
    }

    private boolean valido(Vuelo v) {
        return v != null && v.getTotalPlazas() > 0 && v.getPlazasTurista() >= 0 && v.getPlazasPrimera() >= 0
                && v.getPlazasTurista() + v.getPlazasPrimera() <= v.getTotalPlazas();
    }
}
