package com.tijetravel.tije_back.controladores;

import com.tijetravel.tije_back.modelos.Agencia;
import com.tijetravel.tije_back.modelos.Hotel;
import com.tijetravel.tije_back.modelos.Reserva;
import com.tijetravel.tije_back.modelos.Sucursal;
import com.tijetravel.tije_back.modelos.Turista;
import com.tijetravel.tije_back.modelos.Usuario;
import com.tijetravel.tije_back.modelos.Vuelo;
import com.tijetravel.tije_back.persistencia.ArchivoHoteles;
import com.tijetravel.tije_back.persistencia.ArchivoReservas;
import com.tijetravel.tije_back.persistencia.ArchivoSucursales;
import com.tijetravel.tije_back.persistencia.ArchivoTuristas;
import com.tijetravel.tije_back.persistencia.ArchivoUsuarios;
import com.tijetravel.tije_back.persistencia.ArchivoVuelos;
import com.tijetravel.tije_back.persistencia.PersistenciaException;


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
