package tijetravel.vistas;

import java.util.ArrayList;
import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
import tijetravel.modelos.Agencia;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.Vuelo;

public class VistaCliente extends VistaUsuario {

    public VistaCliente(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
        super(agencia, controladorDatos, teclado);
    }

    @Override
    public void mostrar(Usuario usuario) {
        Turista titular = agencia.buscarTuristaDeUsuario(usuario);

        if (titular == null) {
            System.out.println("El usuario no tiene un turista asociado.");
            return;
        }

        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarHoteles();
                    break;
                case 2:
                    mostrarVuelos();
                    break;
                case 3:
                    mostrarReservas(titular);
                    break;
                case 4:
                    mostrarFamiliares(titular);
                    break;
                case 0:
                    System.out.println("Sesion cerrada.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println("===== MENU CLIENTE =====");
        System.out.println("1. Ver hoteles disponibles");
        System.out.println("2. Ver vuelos disponibles");
        System.out.println("3. Ver mis reservas");
        System.out.println("4. Ver familiares");
        System.out.println("0. Cerrar sesion");
    }

    private void mostrarHoteles() {
        if (agencia.getHoteles().isEmpty()) {
            System.out.println("No hay hoteles cargados.");
            return;
        }

        System.out.println("===== HOTELES =====");

        for (Hotel hotel : agencia.getHoteles()) {
            System.out.println("Codigo: " + hotel.getCodigo());
            System.out.println("Nombre: " + hotel.getNombre());
            System.out.println("Direccion: " + hotel.getDireccion());
            System.out.println("Ciudad: " + hotel.getCiudad());
            System.out.println("Telefono: " + hotel.getTelefono());
            System.out.println("Capacidad de plazas: " + hotel.getPlazasDisponibles());
            System.out.println("--------------------");
        }
    }

    private void mostrarVuelos() {
        if (agencia.getVuelos().isEmpty()) {
            System.out.println("No hay vuelos cargados.");
            return;
        }

        System.out.println("===== VUELOS =====");

        for (Vuelo vuelo : agencia.getVuelos()) {
            System.out.println("Numero: " + vuelo.getNumero());
            System.out.println("Fecha y hora: " + vuelo.getFechaYHora());
            System.out.println("Origen: " + vuelo.getOrigen());
            System.out.println("Destino: " + vuelo.getDestino());
            System.out.println("Plazas totales: " + vuelo.getTotalPlazas());
            System.out.println("Turista disponibles: "
                    + controladorReservas.plazasDisponiblesVuelo(vuelo, tijetravel.modelos.ClaseVuelo.TURISTA)
                    + " de " + vuelo.getPlazasTurista());
            System.out.println("Primera disponibles: "
                    + controladorReservas.plazasDisponiblesVuelo(vuelo, tijetravel.modelos.ClaseVuelo.PRIMERA)
                    + " de " + vuelo.getPlazasPrimera());
            System.out.println("--------------------");
        }
    }

    private void mostrarReservas(Turista titular) {
        ArrayList<Reserva> reservas = controladorReservas.buscarReservasDeTitularYFamiliares(titular.getCodigo());

        if (reservas.isEmpty()) {
            System.out.println("No hay reservas cargadas.");
            return;
        }

        System.out.println("===== RESERVAS =====");

        for (Reserva reserva : reservas) {
            mostrarReserva(reserva);
        }
    }

    private void mostrarReserva(Reserva reserva) {
        System.out.println("Codigo reserva: " + reserva.getCodigo());
        System.out.println("Turista: " + reserva.getTurista().getNombre() + " " + reserva.getTurista().getApellido());
        System.out.println("Sucursal: " + reserva.getSucursal().getCodigo());
        System.out.println("Vuelo: " + reserva.getVuelo().getNumero());
        System.out.println("Hotel: " + reserva.getHotel().getNombre());
        System.out.println("Clase vuelo: " + reserva.getClaseVuelo());
        System.out.println("Hospedaje: " + reserva.getTipoHospedaje());
        System.out.println("Llegada: " + reserva.getFechaLlegada());
        System.out.println("Partida: " + reserva.getFechaPartida());
        System.out.println("--------------------");
    }

    private void mostrarFamiliares(Turista titular) {
        ArrayList<Turista> familiares = agencia.buscarFamiliaresDe(titular.getCodigo());

        if (familiares.isEmpty()) {
            System.out.println("No hay familiares cargados.");
            return;
        }

        System.out.println("===== FAMILIARES =====");

        for (Turista familiar : familiares) {
            System.out.println("Codigo: " + familiar.getCodigo());
            System.out.println("Nombre: " + familiar.getNombre() + " " + familiar.getApellido());
            System.out.println("Email: " + familiar.getEmail());
            System.out.println("Celular: " + familiar.getTelefonoCelular());
            System.out.println("--------------------");
        }
    }
}
