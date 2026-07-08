package tijetravel.modelos;

import java.util.ArrayList;

public class Agencia {
    private ArrayList<Sucursal> sucursales;
    private ArrayList<Hotel> hoteles;
    private ArrayList<Vuelo> vuelos;
    private ArrayList<Turista> turistas;
    private ArrayList<Usuario> usuarios;
    private ArrayList<Reserva> reservas;

    public Agencia() {
        this.sucursales = new ArrayList<>();
        this.hoteles = new ArrayList<>();
        this.vuelos = new ArrayList<>();
        this.turistas = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.usuarios = new ArrayList<>();
    }

    public ArrayList<Sucursal> getSucursales() {
        return sucursales;
    }

    public ArrayList<Hotel> getHoteles() {
        return hoteles;
    }

    public ArrayList<Vuelo> getVuelos() {
        return vuelos;
    }

    public ArrayList<Turista> getTuristas() {
        return turistas;
    }

    public ArrayList<Reserva> getReservas() {
        return reservas;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public boolean agregarSucursal(Sucursal sucursal) {
        if (buscarSucursalPorCodigo(sucursal.getCodigo()) != null) {
            return false;

        }

        this.sucursales.add(sucursal);
        return true;
    }

    public boolean agregarHotel(Hotel hotel) {
        if (buscarHotelPorCodigo(hotel.getCodigo()) != null) {
            return false;
        }
        this.hoteles.add(hotel);
        return true;

    }

    public boolean agregarVuelo(Vuelo vuelo) {
        if (buscarVueloPorNumero(vuelo.getNumero()) != null) {

            return false;
        }
        this.vuelos.add(vuelo);
        return true;
    }

    public boolean agregarTurista(Turista turista) {
        if (buscarTuristaPorCodigo(turista.getCodigo()) != null) {
            return false;
        }
        this.turistas.add(turista);
        return true;
    }

    public boolean agregarReserva(Reserva reserva) {
        if (buscarReservaPorCodigo(reserva.getCodigo()) != null) {
            return false;
        }
        this.reservas.add(reserva);
        return true;
    }

    public boolean agregarUsuario(Usuario usuario) {
        if (buscarUsuarioPorNombre(usuario.getNombreUsuario()) != null) {
            return false;
        }
        this.usuarios.add(usuario);
        return true;
    }

    // BUSQUEDAS
    public Turista buscarTuristaPorCodigo(int codigo) {
        for (Turista turista : turistas) {
            if (turista.getCodigo() == codigo) {
                return turista;
            }
        }
        return null;
    }

    public Sucursal buscarSucursalPorCodigo(int codigo) {
        for (Sucursal sucursal : sucursales) {
            if (sucursal.getCodigo() == codigo) {
                return sucursal;
            }
        }
        return null;
    }

    public Vuelo buscarVueloPorNumero(int numero) {
        for (Vuelo vuelo : vuelos) {
            if (vuelo.getNumero() == numero) {
                return vuelo;
            }
        }
        return null;
    }

    public Hotel buscarHotelPorCodigo(int codigo) {
        for (Hotel hotel : hoteles) {
            if (hotel.getCodigo() == codigo) {
                return hotel;
            }
        }
        return null;
    }

    public Reserva buscarReservaPorCodigo(int codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo() == codigo) {
                return reserva;
            }
        }
        return null;
    }

    public Usuario buscarUsuarioPorNombre(String nombreUsuario) {
        for (Usuario usuario : usuarios) {
            if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                return usuario;
            }
        }
        return null;
    }
}
