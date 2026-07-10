package tijetravel.vistas;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
import tijetravel.modelos.Agencia;
import tijetravel.modelos.ClaseVuelo;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.TipoHospedaje;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.Vuelo;

public class VistaVendedor extends VistaUsuario {
    private Usuario usuarioActual;

    public VistaVendedor(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
        super(agencia, controladorDatos, teclado);
    }

    @Override
    public void mostrar(Usuario usuario) {
        this.usuarioActual = usuario;
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    administrarClientes();
                    break;
                case 2:
                    administrarReservas();
                    break;
                case 3:
                    mostrarHoteles();
                    break;
                case 4:
                    mostrarVuelos();
                    break;
                case 5:
                    mostrarSucursales();
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
        System.out.println("1. Administrar clientes");
        System.out.println("2. Administrar reservas");
        System.out.println("3. Consultar hoteles");
        System.out.println("4. Consultar vuelos");
        System.out.println("5. Consultar sucursales");
        System.out.println("0. Cerrar sesion");
    }

    private void administrarClientes() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR CLIENTES =====");
            System.out.println("1. Listar clientes");
            System.out.println("2. Buscar cliente");
            System.out.println("3. Agregar titular");
            System.out.println("4. Agregar familiar");
            System.out.println("5. Modificar cliente");
            System.out.println("6. Eliminar cliente");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarTuristas();
                    break;
                case 2:
                    buscarTurista();
                    break;
                case 3:
                    agregarTitular();
                    break;
                case 4:
                    agregarFamiliar();
                    break;
                case 5:
                    modificarTurista();
                    break;
                case 6:
                    eliminarTurista();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private void administrarReservas() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR RESERVAS =====");
            System.out.println("1. Listar reservas");
            System.out.println("2. Buscar reserva");
            System.out.println("3. Crear reserva");
            System.out.println("4. Cancelar reserva");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarReservas();
                    break;
                case 2:
                    buscarReserva();
                    break;
                case 3:
                    crearReservaParaTurista();
                    break;
                case 4:
                    cancelarReserva();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
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

    private void agregarTitular() {
        System.out.println("===== AGREGAR TITULAR =====");

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

        Turista turista = controladorTuristas.agregarTitular(usuarioActual, nombre, apellido, direccion,
                email, telefonoFijo, telefonoCelular);

        if (turista != null) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Titular agregado con codigo: " + turista.getCodigo());
        } else {
            System.out.println("No se pudo agregar el titular.");
        }
    }

    private void agregarFamiliar() {
        if (!hayTitulares()) {
            System.out.println("No hay titulares cargados.");
            return;
        }

        System.out.println("===== AGREGAR FAMILIAR =====");
        mostrarTitulares();
        int codigoTitular = leerCodigoTitularExistente();
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

        Turista familiar = controladorTuristas.agregarFamiliar(usuarioActual, codigoTitular, nombre, apellido,
                direccion, email, telefonoFijo, telefonoCelular);

        if (familiar != null) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Familiar agregado con codigo: " + familiar.getCodigo());
        } else {
            System.out.println("No se pudo agregar el familiar.");
        }
    }

    private boolean hayTitulares() {
        for (Turista turista : agencia.getTuristas()) {
            if (turista.isEsTitular()) {
                return true;
            }
        }

        return false;
    }

    private void modificarTurista() {
        if (agencia.getTuristas().isEmpty()) {
            System.out.println("No hay clientes cargados.");
            return;
        }

        mostrarTuristas();
        int codigo = leerCodigoTuristaExistente();
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);
        int opcion;

        do {
            System.out.println("===== MODIFICAR CLIENTE =====");
            mostrarTurista(turista);
            System.out.println("1. Nombre");
            System.out.println("2. Apellido");
            System.out.println("3. Direccion");
            System.out.println("4. Email");
            System.out.println("5. Telefono fijo");
            System.out.println("6. Telefono celular");
            System.out.println("0. Terminar");
            opcion = leerEntero("Seleccione un campo: ");

            switch (opcion) {
                case 1:
                    System.out.print("Nuevo nombre: ");
                    controladorTuristas.modificarNombre(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 2:
                    System.out.print("Nuevo apellido: ");
                    controladorTuristas.modificarApellido(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 3:
                    System.out.print("Nueva direccion: ");
                    controladorTuristas.modificarDireccion(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 4:
                    System.out.print("Nuevo email: ");
                    controladorTuristas.modificarEmail(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 5:
                    System.out.print("Nuevo telefono fijo: ");
                    controladorTuristas.modificarTelefonoFijo(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 6:
                    System.out.print("Nuevo telefono celular: ");
                    controladorTuristas.modificarTelefonoCelular(usuarioActual, codigo, teclado.nextLine());
                    break;
                case 0:
                    controladorDatos.guardarTodo(agencia);
                    System.out.println("Cliente actualizado.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void eliminarTurista() {
        if (agencia.getTuristas().isEmpty()) {
            System.out.println("No hay clientes cargados.");
            return;
        }

        mostrarTuristas();
        int codigo = leerEntero("Codigo del cliente a eliminar: ");
        Turista turista = agencia.buscarTuristaPorCodigo(codigo);

        if (turista == null) {
            System.out.println("No existe un cliente con ese codigo.");
            return;
        }
        if (agencia.turistaTieneReservas(codigo)) {
            System.out.println("No se puede eliminar: el cliente tiene reservas.");
            return;
        }
        if (agencia.titularTieneFamiliares(codigo)) {
            System.out.println("No se puede eliminar: el titular tiene familiares asociados.");
            return;
        }
        if (agencia.turistaTieneUsuario(codigo)) {
            System.out.println("No se puede eliminar: el cliente tiene un usuario asociado.");
            return;
        }

        if (controladorTuristas.eliminar(usuarioActual, codigo)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Cliente eliminado.");
        } else {
            System.out.println("No se pudo eliminar el cliente.");
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

    private void mostrarTitulares() {
        System.out.println("===== TITULARES =====");

        for (Turista turista : agencia.getTuristas()) {
            if (turista.isEsTitular()) {
                System.out.println("Codigo: " + turista.getCodigo());
                System.out.println("Nombre: " + turista.getNombre() + " " + turista.getApellido());
                System.out.println("--------------------");
            }
        }
    }

    private int leerCodigoTitularExistente() {
        int codigo;
        Turista titular;

        do {
            codigo = leerEntero("Codigo titular: ");
            titular = agencia.buscarTuristaPorCodigo(codigo);

            if (titular == null || !titular.isEsTitular()) {
                System.out.println("No existe un titular con ese codigo.");
            }
        } while (titular == null || !titular.isEsTitular());

        return codigo;
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

    private void buscarReserva() {
        int codigo = leerEntero("Codigo reserva: ");
        Reserva reserva = agencia.buscarReservaPorCodigo(codigo);

        if (reserva == null) {
            System.out.println("No existe una reserva con ese codigo.");
            return;
        }

        mostrarReserva(reserva);
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
                usuarioActual,
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

        if (controladorReservas.cancelarReserva(usuarioActual, codigo)) {
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

}
