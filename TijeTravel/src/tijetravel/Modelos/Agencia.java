package tijetravel.modelos;

import java.util.ArrayList;

public class Agencia {
    private ArrayList<Sucursal> sucursales;
    private ArrayList<Hotel> hoteles;
    private ArrayList<Vuelo> vuelos;
    private ArrayList<Turista> turistas;
    private ArrayList<Reserva> reservas;
    private ArrayList<Usuario> usuarios;

    public Agencia() {
        this.sucursales = new ArrayList<>();
        this.hoteles = new ArrayList<>();
        this.vuelos = new ArrayList<>();
        this.turistas = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.usuarios = new ArrayList<>();
    }
}
