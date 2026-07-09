package tijetravel.vistas;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

public class VistaCliente {
    private Agencia agencia;
    private ControladorDatos controladorDatos;
    private ControladorReservas controladorReservas;
    private Scanner teclado;

    public VistaCliente(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
        this.agencia = agencia;
        this.controladorDatos = controladorDatos;
        this.controladorReservas = new ControladorReservas(agencia);
        this.teclado = teclado;
    }

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
                case 5:
                    agregarFamiliar(titular);
                    break;
                case 6:
                    crearReservaParaTurista(titular, titular);
                    break;
                case 7:
                    crearReservaParaFamiliar(titular);
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
        System.out.println("===== MENU CLIENTE =====");
        System.out.println("1. Ver hoteles disponibles");
        System.out.println("2. Ver vuelos disponibles");
        System.out.println("3. Ver mis reservas");
        System.out.println("4. Ver familiares");
        System.out.println("5. Agregar familiar");
        System.out.println("6. Crear reserva para mi");
        System.out.println("7. Crear reserva para familiar");
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
            System.out.println("Plazas disponibles: " + hotel.getPlazasDisponibles());
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
            System.out.println("Plazas turista: " + vuelo.getPlazasTurista());
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

    private void agregarFamiliar(Turista titular) {
        System.out.println("===== AGREGAR FAMILIAR =====");

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

        Turista familiar = new Turista(
                codigo,
                nombre,
                apellido,
                direccion,
                email,
                telefonoFijo,
                telefonoCelular,
                false,
                titular.getCodigo());

        if (agencia.agregarTurista(familiar)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Familiar agregado con codigo: " + codigo);
        } else {
            System.out.println("No se pudo agregar el familiar.");
        }
    }

    private void crearReservaParaFamiliar(Turista titular) {
        ArrayList<Turista> familiares = agencia.buscarFamiliaresDe(titular.getCodigo());

        if (familiares.isEmpty()) {
            System.out.println("No hay familiares cargados.");
            return;
        }

        mostrarFamiliares(titular);
        int codigoFamiliar = leerCodigoFamiliar(titular);
        Turista familiar = agencia.buscarTuristaPorCodigo(codigoFamiliar);

        crearReservaParaTurista(titular, familiar);
    }

    private void crearReservaParaTurista(Turista titular, Turista turistaReserva) {
        if (agencia.getSucursales().isEmpty() || agencia.getVuelos().isEmpty() || agencia.getHoteles().isEmpty()) {
            System.out.println("Faltan sucursales, vuelos u hoteles cargados.");
            return;
        }

        System.out.println("===== CREAR RESERVA =====");
        System.out.println("Turista: " + turistaReserva.getNombre() + " " + turistaReserva.getApellido());

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

        Reserva reserva = controladorReservas.crearReservaCliente(
                titular.getCodigo(),
                turistaReserva.getCodigo(),
                codigoSucursal,
                numeroVuelo,
                codigoHotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);

        if (reserva == null) {
            System.out.println("No se pudo crear la reserva. Verifique los datos ingresados.");
            return;
        }

        controladorDatos.guardarTodo(agencia);
        System.out.println("Reserva creada con codigo: " + reserva.getCodigo());
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

    private int leerCodigoFamiliar(Turista titular) {
        int codigo;

        do {
            codigo = leerEntero("Codigo del familiar: ");

            if (!agencia.turistaPerteneceATitular(codigo, titular.getCodigo()) || codigo == titular.getCodigo()) {
                System.out.println("No existe un familiar con ese codigo.");
            }
        } while (!agencia.turistaPerteneceATitular(codigo, titular.getCodigo()) || codigo == titular.getCodigo());

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
