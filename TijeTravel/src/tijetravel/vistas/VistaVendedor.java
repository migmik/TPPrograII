package tijetravel.vistas;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
import tijetravel.controladores.ControladorReservas;
import tijetravel.models.Agencia;
import tijetravel.models.ClaseVuelo;
import tijetravel.models.Hotel;
import tijetravel.models.Reserva;
import tijetravel.models.Sucursal;
import tijetravel.models.TipoHospedaje;
import tijetravel.models.Turista;
import tijetravel.models.Usuario;
import tijetravel.models.Vuelo;

public class VistaVendedor {
    private Agencia agencia;
    private ControladorDatos controladorDatos;
    private ControladorReservas controladorReservas;
    private Scanner teclado;

    public VistaVendedor(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
        this.agencia = agencia;
        this.controladorDatos = controladorDatos;
        this.controladorReservas = new ControladorReservas(agencia);
        this.teclado = teclado;
    }

    public void mostrar(Usuario usuario) {
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarTuristas();
                    break;
                case 2:
                    agregarTurista();
                    break;
                case 3:
                    buscarTurista();
                    break;
                case 4:
                    mostrarReservas();
                    break;
                case 5:
                    crearReservaParaTurista();
                    break;
                case 6:
                    cancelarReserva();
                    break;
                case 0:
                    controladorDatos.guardarTodo(agencia);
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
        System.out.println("===== MENU VENDEDOR =====");
        System.out.println("1. Ver turistas");
        System.out.println("2. Agregar turista");
        System.out.println("3. Buscar turista");
        System.out.println("4. Ver reservas");
        System.out.println("5. Crear reserva para turista");
        System.out.println("6. Cancelar reserva");
        System.out.println("0. Cerrar sesion");
    }

    private void mostrarTuristas() {
        if (agencia.getTuristas().isEmpty()) {
            System.out.println("No hay turistas cargados.");
            return;
        }

        System.out.println("===== TURISTAS =====");

        for (Turista turista : agencia.getTuristas()) {
            mostrarTurista(turista);
        }
    }

    private void mostrarTurista(Turista turista) {
        System.out.println("Codigo: " + turista.getCodigo());
        System.out.println("Nombre: " + turista.getNombre() + " " + turista.getApellido());
        System.out.println("Direccion: " + turista.getDireccion());
        System.out.println("Email: " + turista.getEmail());
        System.out.println("Telefono fijo: " + turista.getTelefonoFijo());
        System.out.println("Telefono celular: " + turista.getTelefonoCelular());
        System.out.println("Titular: " + turista.isEsTitular());
        System.out.println("Codigo titular: " + turista.getCodigoTitular());
        System.out.println("--------------------");
    }

    private void agregarTurista() {
        System.out.println("===== AGREGAR TURISTA =====");

        int codigo = agencia.generarCodigoTurista();

        System.out.print("Nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Apellido: ");
        String apellido = teclado.nextLine();

        System.out.print("Direccion: ");
        String direccion = teclado.nextLine();

        System.out.print("Email: ");
        String email = teclado.nextLine();

        System.out.print("Telefono fijo: ");
        String telefonoFijo = teclado.nextLine();

        System.out.print("Telefono celular: ");
        String telefonoCelular = teclado.nextLine();

        Turista turista = new Turista(
                codigo,
                nombre,
                apellido,
                direccion,
                email,
                telefonoFijo,
                telefonoCelular,
                true,
                null);

        if (agencia.agregarTurista(turista)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Turista agregado con codigo: " + codigo);
        } else {
            System.out.println("No se pudo agregar el turista.");
        }
    }

    private void buscarTurista() {
        int codigo = leerEntero("Codigo turista: ");
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);

        if (turista == null) {
            System.out.println("No existe un turista con ese codigo.");
            return;
        }

        mostrarTurista(turista);
    }

    private void mostrarReservas() {
        if (agencia.getReservas().isEmpty()) {
            System.out.println("No hay reservas cargadas.");
            return;
        }

        System.out.println("===== RESERVAS =====");

        for (Reserva reserva : agencia.getReservas()) {
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

    private void crearReservaParaTurista() {
        if (agencia.getTuristas().isEmpty() || agencia.getSucursales().isEmpty()
                || agencia.getVuelos().isEmpty() || agencia.getHoteles().isEmpty()) {
            System.out.println("Faltan turistas, sucursales, vuelos u hoteles cargados.");
            return;
        }

        System.out.println("===== CREAR RESERVA =====");

        mostrarTuristas();
        int codigoTurista = leerCodigoTuristaExistente();

        mostrarVuelos();
        int numeroVuelo = leerNumeroVueloExistente();

        mostrarHoteles();
        int codigoHotel = leerCodigoHotelExistente();

        mostrarSucursales();
        int codigoSucursal = leerCodigoSucursalExistente();

        ClaseVuelo claseVuelo = leerClaseVuelo();
        TipoHospedaje tipoHospedaje = leerTipoHospedaje();
        LocalDate fechaLlegada = leerFecha("Fecha llegada (yyyy-mm-dd): ");
        LocalDate fechaPartida = leerFechaPartidaValida(fechaLlegada);

        Reserva reserva = controladorReservas.crearReserva(
                codigoTurista,
                codigoSucursal,
                numeroVuelo,
                codigoHotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);

        if (reserva == null) {
            System.out.println("No se pudo crear la reserva.");
            return;
        }

        controladorDatos.guardarTodo(agencia);
        System.out.println("Reserva creada con codigo: " + reserva.getCodigo());
    }

    private void cancelarReserva() {
        if (agencia.getReservas().isEmpty()) {
            System.out.println("No hay reservas cargadas.");
            return;
        }

        mostrarReservas();
        int codigo = leerEntero("Codigo reserva: ");

        if (controladorReservas.cancelarReserva(codigo)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Reserva cancelada.");
        } else {
            System.out.println("No existe una reserva con ese codigo.");
        }
    }

    private void mostrarHoteles() {
        System.out.println("===== HOTELES =====");

        for (Hotel hotel : agencia.getHoteles()) {
            System.out.println("Codigo: " + hotel.getCodigo());
            System.out.println("Nombre: " + hotel.getNombre());
            System.out.println("Direccion: " + hotel.getDireccion());
            System.out.println("Ciudad: " + hotel.getCiudad());
            System.out.println("Telefono: " + hotel.getTelefono());
            System.out.println("Plazas disponibles: " + hotel.getPlazasDisponibles());
            System.out.println("--------------------");
        }
    }

    private void mostrarVuelos() {
        System.out.println("===== VUELOS =====");

        for (Vuelo vuelo : agencia.getVuelos()) {
            System.out.println("Numero: " + vuelo.getNumero());
            System.out.println("Fecha y hora: " + vuelo.getFechaYHora());
            System.out.println("Origen: " + vuelo.getOrigen());
            System.out.println("Destino: " + vuelo.getDestino());
            System.out.println("Plazas totales: " + vuelo.getTotalPlazas());
            System.out.println("Plazas turista: " + vuelo.getPlazasTurista());
            System.out.println("--------------------");
        }
    }

    private void mostrarSucursales() {
        System.out.println("===== SUCURSALES =====");

        for (Sucursal sucursal : agencia.getSucursales()) {
            System.out.println("Codigo: " + sucursal.getCodigo());
            System.out.println("Direccion: " + sucursal.getDireccion());
            System.out.println("Telefono: " + sucursal.getTelefono());
            System.out.println("--------------------");
        }
    }

    private int leerCodigoTuristaExistente() {
        int codigo;

        do {
            codigo = leerEntero("Codigo turista: ");

            if (agencia.buscarTuristaPorCodigo(codigo) == null) {
                System.out.println("No existe un turista con ese codigo.");
            }
        } while (agencia.buscarTuristaPorCodigo(codigo) == null);

        return codigo;
    }

    private int leerNumeroVueloExistente() {
        int numero;

        do {
            numero = leerEntero("Numero vuelo: ");

            if (agencia.buscarVueloPorNumero(numero) == null) {
                System.out.println("No existe un vuelo con ese numero.");
            }
        } while (agencia.buscarVueloPorNumero(numero) == null);

        return numero;
    }

    private int leerCodigoHotelExistente() {
        int codigo;

        do {
            codigo = leerEntero("Codigo hotel: ");

            if (agencia.buscarHotelPorCodigo(codigo) == null) {
                System.out.println("No existe un hotel con ese codigo.");
            }
        } while (agencia.buscarHotelPorCodigo(codigo) == null);

        return codigo;
    }

    private int leerCodigoSucursalExistente() {
        int codigo;

        do {
            codigo = leerEntero("Codigo sucursal: ");

            if (agencia.buscarSucursalPorCodigo(codigo) == null) {
                System.out.println("No existe una sucursal con ese codigo.");
            }
        } while (agencia.buscarSucursalPorCodigo(codigo) == null);

        return codigo;
    }

    private ClaseVuelo leerClaseVuelo() {
        while (true) {
            System.out.println("Clase de vuelo:");
            System.out.println("1. TURISTA");
            System.out.println("2. PRIMERA");

            int opcion = leerEntero("Seleccione clase: ");

            switch (opcion) {
                case 1:
                    return ClaseVuelo.TURISTA;
                case 2:
                    return ClaseVuelo.PRIMERA;
                default:
                    System.out.println("Clase invalida.");
                    break;
            }
        }
    }

    private TipoHospedaje leerTipoHospedaje() {
        while (true) {
            System.out.println("Tipo de hospedaje:");
            System.out.println("1. MEDIA_PENSION");
            System.out.println("2. PENSION_COMPLETA");

            int opcion = leerEntero("Seleccione hospedaje: ");

            switch (opcion) {
                case 1:
                    return TipoHospedaje.MEDIA_PENSION;
                case 2:
                    return TipoHospedaje.PENSION_COMPLETA;
                default:
                    System.out.println("Tipo de hospedaje invalido.");
                    break;
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        LocalDate fecha = null;
        boolean valida = false;

        while (!valida) {
            System.out.print(mensaje);

            try {
                fecha = LocalDate.parse(teclado.nextLine());
                valida = true;
            } catch (DateTimeParseException e) {
                System.out.println("Formato invalido. Use yyyy-mm-dd.");
            }
        }

        return fecha;
    }

    private LocalDate leerFechaPartidaValida(LocalDate fechaLlegada) {
        LocalDate fechaPartida;

        do {
            fechaPartida = leerFecha("Fecha partida (yyyy-mm-dd): ");

            if (!fechaPartida.isAfter(fechaLlegada)) {
                System.out.println("La fecha de partida debe ser posterior a la fecha de llegada.");
            }
        } while (!fechaPartida.isAfter(fechaLlegada));

        return fechaPartida;
    }

    private int leerEntero(String mensaje) {
        int numero = -1;
        boolean valido = false;

        while (!valido) {
            System.out.print(mensaje);

            try {
                numero = Integer.parseInt(teclado.nextLine());
                valido = true;
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }

        return numero;
    }
}
