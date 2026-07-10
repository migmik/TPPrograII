package tijetravel.controladores;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.Vuelo;
import tijetravel.persistencia.ArchivoHoteles;
import tijetravel.persistencia.ArchivoReservas;
import tijetravel.persistencia.ArchivoSucursales;
import tijetravel.persistencia.ArchivoTuristas;
import tijetravel.persistencia.ArchivoUsuarios;
import tijetravel.persistencia.ArchivoVuelos;
import tijetravel.persistencia.PersistenciaException;

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

    public Agencia cargarTodo() {
        Agencia agencia = new Agencia();
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

        return agencia;
    }

    public void guardarTodo(Agencia agencia) {
        if (agencia == null) {
            throw new PersistenciaException("No se puede guardar una agencia nula");
        }
        archivoSucursales.guardar(agencia.getSucursales());
        archivoHoteles.guardar(agencia.getHoteles());
        archivoVuelos.guardar(agencia.getVuelos());
        archivoTuristas.guardar(agencia.getTuristas());
        archivoUsuarios.guardar(agencia.getUsuarios());
        archivoReservas.guardar(agencia.getReservas());
    }
}
