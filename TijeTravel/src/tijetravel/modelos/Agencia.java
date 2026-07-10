package tijetravel.modelos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<Sucursal> getSucursales() {
        return Collections.unmodifiableList(sucursales);
    }

    public List<Hotel> getHoteles() {
        return Collections.unmodifiableList(hoteles);
    }

    public List<Vuelo> getVuelos() {
        return Collections.unmodifiableList(vuelos);
    }

    public List<Turista> getTuristas() {
        return Collections.unmodifiableList(turistas);
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
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

    public boolean eliminarReserva(int codigo) {
        Reserva reserva = buscarReservaPorCodigo(codigo);
        return reserva != null && reservas.remove(reserva);
    }

    public boolean agregarUsuario(Usuario usuario) {
        if (buscarUsuarioPorNombre(usuario.getNombreUsuario()) != null) {
            return false;
        }
        this.usuarios.add(usuario);
        return true;
    }

    public boolean eliminarSucursalPorCodigo(int codigo) {
        Sucursal sucursal = buscarSucursalPorCodigo(codigo);

        if (sucursal == null) {
            return false;
        }

        return sucursales.remove(sucursal);
    }

    public boolean eliminarHotelPorCodigo(int codigo) {
        Hotel hotel = buscarHotelPorCodigo(codigo);

        if (hotel == null) {
            return false;
        }

        return hoteles.remove(hotel);
    }

    public boolean eliminarVueloPorNumero(int numero) {
        Vuelo vuelo = buscarVueloPorNumero(numero);

        if (vuelo == null) {
            return false;
        }

        return vuelos.remove(vuelo);
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

    public Turista buscarTuristaDeUsuario(Usuario usuario) {
        if (usuario == null || usuario.getCodigoTurista() == null) {
            return null;
        }

        return buscarTuristaPorCodigo(usuario.getCodigoTurista());
    }

    public ArrayList<Turista> buscarFamiliaresDe(int codigoTitular) {
        ArrayList<Turista> familiares = new ArrayList<>();

        for (Turista turista : turistas) {
            if (turista.getCodigoTitular() != null && turista.getCodigoTitular().equals(codigoTitular)) {
                familiares.add(turista);
            }
        }

        return familiares;
    }

    public boolean turistaPerteneceATitular(int codigoTurista, int codigoTitular) {
        Turista turista = buscarTuristaPorCodigo(codigoTurista);

        if (turista == null) {
            return false;
        }

        if (turista.isEsTitular() && turista.getCodigo() == codigoTitular) {
            return true;
        }

        return turista.getCodigoTitular() != null && turista.getCodigoTitular().equals(codigoTitular);
    }

    public boolean turistaTieneReservas(int codigoTurista) {
        for (Reserva reserva : reservas) {
            if (reserva.getTurista().getCodigo() == codigoTurista) {
                return true;
            }
        }

        return false;
    }

    public boolean titularTieneFamiliares(int codigoTitular) {
        return !buscarFamiliaresDe(codigoTitular).isEmpty();
    }

    public boolean turistaTieneUsuario(int codigoTurista) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCodigoTurista() != null && usuario.getCodigoTurista().equals(codigoTurista)) {
                return true;
            }
        }

        return false;
    }

    public boolean eliminarTurista(int codigoTurista) {
        Turista turista = buscarTuristaPorCodigo(codigoTurista);

        if (turista == null || turistaTieneReservas(codigoTurista)
                || titularTieneFamiliares(codigoTurista) || turistaTieneUsuario(codigoTurista)) {
            return false;
        }

        return turistas.remove(turista);
    }

    public boolean sucursalTieneReservas(int codigoSucursal) {
        for (Reserva reserva : reservas) {
            if (reserva.getSucursal().getCodigo() == codigoSucursal) return true;
        }
        return false;
    }

    public boolean hotelTieneReservas(int codigoHotel) {
        for (Reserva reserva : reservas) {
            if (reserva.getHotel().getCodigo() == codigoHotel) return true;
        }
        return false;
    }

    public boolean vueloTieneReservas(int numeroVuelo) {
        for (Reserva reserva : reservas) {
            if (reserva.getVuelo().getNumero() == numeroVuelo) return true;
        }
        return false;
    }

    public boolean eliminarSucursal(int codigo) {
        Sucursal sucursal = buscarSucursalPorCodigo(codigo);
        return sucursal != null && !sucursalTieneReservas(codigo) && sucursales.remove(sucursal);
    }

    public boolean eliminarHotel(int codigo) {
        Hotel hotel = buscarHotelPorCodigo(codigo);
        return hotel != null && !hotelTieneReservas(codigo) && hoteles.remove(hotel);
    }

    public boolean eliminarVuelo(int numero) {
        Vuelo vuelo = buscarVueloPorNumero(numero);
        return vuelo != null && !vueloTieneReservas(numero) && vuelos.remove(vuelo);
    }

    public int contarAdministradores() {
        int cantidad = 0;
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.ADMINISTRADOR) cantidad++;
        }
        return cantidad;
    }

    public boolean eliminarUsuario(String nombreUsuario) {
        Usuario usuario = buscarUsuarioPorNombre(nombreUsuario);
        if (usuario == null) return false;
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR && contarAdministradores() <= 1) return false;
        return usuarios.remove(usuario);
    }

    public int generarCodigoTurista() {
        int mayorCodigo = 0;

        for (Turista turista : turistas) {
            if (turista.getCodigo() > mayorCodigo) {
                mayorCodigo = turista.getCodigo();
            }
        }

        return mayorCodigo + 1;
    }

    public boolean existeReservaConSucursal(int codigoSucursal) {
        for (Reserva reserva : reservas) {
            if (reserva.getSucursal().getCodigo() == codigoSucursal) {
                return true;
            }
        }

        return false;
    }

    public boolean existeReservaConHotel(int codigoHotel) {
        for (Reserva reserva : reservas) {
            if (reserva.getHotel().getCodigo() == codigoHotel) {
                return true;
            }
        }

        return false;
    }

    public boolean existeReservaConVuelo(int numeroVuelo) {
        for (Reserva reserva : reservas) {
            if (reserva.getVuelo().getNumero() == numeroVuelo) {
                return true;
            }
        }

        return false;
    }
}
