package tijetravel.controladores;

import tijetravel.models.Agencia;
import tijetravel.models.Hotel;
import tijetravel.models.Reserva;
import tijetravel.models.Sucursal;
import tijetravel.models.Turista;
import tijetravel.models.Usuario;
import tijetravel.models.Vuelo;
import tijetravel.persistencia.ArchivoHoteles;
import tijetravel.persistencia.ArchivoReservas;
import tijetravel.persistencia.ArchivoSucursales;
import tijetravel.persistencia.ArchivoTuristas;
import tijetravel.persistencia.ArchivoUsuarios;
import tijetravel.persistencia.ArchivoVuelos;

public class ControladorDatos {
    private ArchivoSucursales archivoSucursales;
    private ArchivoHoteles archivoHoteles;
    private ArchivoVuelos archivoVuelos;
    private ArchivoTuristas archivoTuristas;
    private ArchivoUsuarios archivoUsuarios;
    private ArchivoReservas archivoReservas;

    public ControladorDatos() {
        this.archivoSucursales = new ArchivoSucursales();
        this.archivoHoteles = new ArchivoHoteles();
        this.archivoVuelos = new ArchivoVuelos();
        this.archivoTuristas = new ArchivoTuristas();
        this.archivoUsuarios = new ArchivoUsuarios();
        this.archivoReservas = new ArchivoReservas();
    }

    public void cargarTodo(Agencia agencia) {
        for (Sucursal sucursal : archivoSucursales.cargar()) {
            agencia.agregarSucursal(sucursal);
        }

        for (Hotel hotel : archivoHoteles.cargar()) {
            agencia.agregarHotel(hotel);
        }

        for (Vuelo vuelo : archivoVuelos.cargar()) {
            agencia.agregarVuelo(vuelo);
        }

        for (Turista turista : archivoTuristas.cargar()) {
            agencia.agregarTurista(turista);
        }

        for (Usuario usuario : archivoUsuarios.cargar()) {
            agencia.agregarUsuario(usuario);
        }
        //esto siempre ultimo porque para poder cargar lo anterir ya debe estar
        for (Reserva reserva : archivoReservas.cargar(agencia)) {
            agencia.agregarReserva(reserva);
        }
    }

    public void guardarTodo(Agencia agencia) {
        archivoSucursales.guardar(agencia.getSucursales());
        archivoHoteles.guardar(agencia.getHoteles());
        archivoVuelos.guardar(agencia.getVuelos());
        archivoTuristas.guardar(agencia.getTuristas());
        archivoUsuarios.guardar(agencia.getUsuarios());
        archivoReservas.guardar(agencia.getReservas());
    }
}
