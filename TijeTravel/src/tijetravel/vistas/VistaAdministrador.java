package tijetravel.vistas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
import tijetravel.controladores.ControladorReservas;
import tijetravel.modelos.Agencia;
import tijetravel.modelos.ClaseVuelo;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.RolUsuario;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.TipoHospedaje;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.Vuelo;

public class VistaAdministrador {
    private Agencia agencia;
    private ControladorDatos controladorDatos;
    private ControladorReservas controladorReservas;
    private Scanner teclado;

    public VistaAdministrador(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
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
                    administrarSucursales();
                    break;
                case 2:
                    administrarHoteles();
                    break;
                case 3:
                    administrarVuelos();
                    break;
                case 4:
                    administrarTuristas();
                    break;
                case 5:
                    administrarReservas();
                    break;
                case 6:
                    administrarUsuarios();
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
        System.out.println("===== MENU ADMINISTRADOR =====");
        System.out.println("1. Administrar sucursales");
        System.out.println("2. Administrar hoteles");
        System.out.println("3. Administrar vuelos");
        System.out.println("4. Administrar turistas");
        System.out.println("5. Administrar reservas");
        System.out.println("6. Administrar usuarios");
        System.out.println("0. Cerrar sesion");
    }

    private void administrarSucursales() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR SUCURSALES =====");
            System.out.println("1. Listar sucursales");
            System.out.println("2. Agregar sucursal");
            System.out.println("3. Modificar sucursal");
            System.out.println("4. Eliminar sucursal");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarSucursales();
                    break;
                case 2:
                    agregarSucursal();
                    break;
                case 3:
                    modificarSucursal();
                    break;
                case 4:
                    eliminarSucursal();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void administrarHoteles() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR HOTELES =====");
            System.out.println("1. Listar hoteles");
            System.out.println("2. Agregar hotel");
            System.out.println("3. Modificar hotel");
            System.out.println("4. Eliminar hotel");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarHoteles();
                    break;
                case 2:
                    agregarHotel();
                    break;
                case 3:
                    modificarHotel();
                    break;
                case 4:
                    eliminarHotel();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void administrarVuelos() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR VUELOS =====");
            System.out.println("1. Listar vuelos");
            System.out.println("2. Agregar vuelo");
            System.out.println("3. Modificar vuelo");
            System.out.println("4. Eliminar vuelo");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarVuelos();
                    break;
                case 2:
                    agregarVuelo();
                    break;
                case 3:
                    modificarVuelo();
                    break;
                case 4:
                    eliminarVuelo();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void administrarTuristas() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR TURISTAS =====");
            System.out.println("1. Listar turistas");
            System.out.println("2. Agregar turista");
            System.out.println("3. Buscar turista");
            System.out.println("0. Volver");
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
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void administrarReservas() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR RESERVAS =====");
            System.out.println("1. Listar reservas");
            System.out.println("2. Crear reserva");
            System.out.println("3. Cancelar reserva");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarReservas();
                    break;
                case 2:
                    crearReservaParaTurista();
                    break;
                case 3:
                    cancelarReserva();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void administrarUsuarios() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR USUARIOS =====");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Agregar usuario");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarUsuarios();
                    break;
                case 2:
                    agregarUsuario();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }
        } while (opcion != 0);
    }

    private void mostrarSucursales() {
        if (agencia.getSucursales().isEmpty()) {
            System.out.println("No hay sucursales cargadas.");
            return;
        }

        System.out.println("===== SUCURSALES =====");

        for (Sucursal sucursal : agencia.getSucursales()) {
            System.out.println("Codigo: " + sucursal.getCodigo());
            System.out.println("Direccion: " + sucursal.getDireccion());
            System.out.println("Telefono: " + sucursal.getTelefono());
            System.out.println("--------------------");
        }
    }

    private void agregarSucursal() {
        System.out.println("===== AGREGAR SUCURSAL =====");

        int codigo = leerCodigoSucursalNuevo();

        System.out.print("Direccion: ");
        String direccion = teclado.nextLine();

        System.out.print("Telefono: ");
        String telefono = teclado.nextLine();

        if (agencia.agregarSucursal(new Sucursal(codigo, direccion, telefono))) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Sucursal agregada.");
        } else {
            System.out.println("No se pudo agregar la sucursal.");
        }
    }

    private void modificarSucursal() {
        if (agencia.getSucursales().isEmpty()) {
            System.out.println("No hay sucursales cargadas.");
            return;
        }

        mostrarSucursales();
        int codigo = leerCodigoSucursalExistente();
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(codigo);

        System.out.print("Nueva direccion: ");
        sucursal.setDireccion(teclado.nextLine());

        System.out.print("Nuevo telefono: ");
        sucursal.setTelefono(teclado.nextLine());

        controladorDatos.guardarTodo(agencia);
        System.out.println("Sucursal modificada.");
    }

    private void eliminarSucursal() {
        if (agencia.getSucursales().isEmpty()) {
            System.out.println("No hay sucursales cargadas.");
            return;
        }

        mostrarSucursales();
        int codigo = leerCodigoSucursalExistente();

        if (agencia.existeReservaConSucursal(codigo)) {
            System.out.println("No se puede eliminar la sucursal porque tiene reservas asociadas.");
            return;
        }

        if (agencia.eliminarSucursalPorCodigo(codigo)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Sucursal eliminada.");
        } else {
            System.out.println("No se pudo eliminar la sucursal.");
        }
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

    private void agregarHotel() {
        System.out.println("===== AGREGAR HOTEL =====");

        int codigo = leerCodigoHotelNuevo();

        System.out.print("Nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Direccion: ");
        String direccion = teclado.nextLine();

        System.out.print("Ciudad: ");
        String ciudad = teclado.nextLine();

        System.out.print("Telefono: ");
        String telefono = teclado.nextLine();

        int plazasDisponibles = leerEntero("Plazas disponibles: ");

        Hotel hotel = new Hotel(codigo, nombre, direccion, ciudad, telefono, plazasDisponibles);

        if (agencia.agregarHotel(hotel)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Hotel agregado.");
        } else {
            System.out.println("No se pudo agregar el hotel.");
        }
    }

    private void modificarHotel() {
        if (agencia.getHoteles().isEmpty()) {
            System.out.println("No hay hoteles cargados.");
            return;
        }

        mostrarHoteles();
        int codigo = leerCodigoHotelExistente();
        Hotel hotel = agencia.buscarHotelPorCodigo(codigo);

        System.out.print("Nuevo nombre: ");
        hotel.setNombre(teclado.nextLine());

        System.out.print("Nueva direccion: ");
        hotel.setDireccion(teclado.nextLine());

        System.out.print("Nueva ciudad: ");
        hotel.setCiudad(teclado.nextLine());

        System.out.print("Nuevo telefono: ");
        hotel.setTelefono(teclado.nextLine());

        hotel.setPlazasDisponibles(leerEntero("Nuevas plazas disponibles: "));

        controladorDatos.guardarTodo(agencia);
        System.out.println("Hotel modificado.");
    }

    private void eliminarHotel() {
        if (agencia.getHoteles().isEmpty()) {
            System.out.println("No hay hoteles cargados.");
            return;
        }

        mostrarHoteles();
        int codigo = leerCodigoHotelExistente();

        if (agencia.existeReservaConHotel(codigo)) {
            System.out.println("No se puede eliminar el hotel porque tiene reservas asociadas.");
            return;
        }

        if (agencia.eliminarHotelPorCodigo(codigo)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Hotel eliminado.");
        } else {
            System.out.println("No se pudo eliminar el hotel.");
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
            System.out.println("Plazas primera: " + vuelo.getPlazasPrimera());
            System.out.println("--------------------");
        }
    }

    private void agregarVuelo() {
        System.out.println("===== AGREGAR VUELO =====");

        int numero = leerNumeroVueloNuevo();
        LocalDateTime fechaYHora = leerFechaYHora("Fecha y hora (yyyy-mm-ddThh:mm): ");

        System.out.print("Origen: ");
        String origen = teclado.nextLine();

        System.out.print("Destino: ");
        String destino = teclado.nextLine();

        int totalPlazas = leerEntero("Plazas totales: ");
        int plazasTurista = leerEntero("Plazas turista: ");
        int plazasPrimera = leerEntero("Plazas primera: ");

        Vuelo vuelo = new Vuelo(numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera);

        if (agencia.agregarVuelo(vuelo)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Vuelo agregado.");
        } else {
            System.out.println("No se pudo agregar el vuelo.");
        }
    }

    private void modificarVuelo() {
        if (agencia.getVuelos().isEmpty()) {
            System.out.println("No hay vuelos cargados.");
            return;
        }

        mostrarVuelos();
        int numero = leerNumeroVueloExistente();
        Vuelo vuelo = agencia.buscarVueloPorNumero(numero);

        vuelo.setFechaYHora(leerFechaYHora("Nueva fecha y hora (yyyy-mm-ddThh:mm): "));

        System.out.print("Nuevo origen: ");
        vuelo.setOrigen(teclado.nextLine());

        System.out.print("Nuevo destino: ");
        vuelo.setDestino(teclado.nextLine());

        vuelo.setTotalPlazas(leerEntero("Nuevas plazas totales: "));
        vuelo.setPlazasTurista(leerEntero("Nuevas plazas turista: "));
        vuelo.setPlazasPrimera(leerEntero("Nuevas plazas primera: "));

        controladorDatos.guardarTodo(agencia);
        System.out.println("Vuelo modificado.");
    }

    private void eliminarVuelo() {
        if (agencia.getVuelos().isEmpty()) {
            System.out.println("No hay vuelos cargados.");
            return;
        }

        mostrarVuelos();
        int numero = leerNumeroVueloExistente();

        if (agencia.existeReservaConVuelo(numero)) {
            System.out.println("No se puede eliminar el vuelo porque tiene reservas asociadas.");
            return;
        }

        if (agencia.eliminarVueloPorNumero(numero)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Vuelo eliminado.");
        } else {
            System.out.println("No se pudo eliminar el vuelo.");
        }
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

        Turista turista = new Turista(codigo, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular,
                true, null);

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

    private void mostrarUsuarios() {
        if (agencia.getUsuarios().isEmpty()) {
            System.out.println("No hay usuarios cargados.");
            return;
        }

        System.out.println("===== USUARIOS =====");

        for (Usuario usuario : agencia.getUsuarios()) {
            System.out.println("Usuario: " + usuario.getNombreUsuario());
            System.out.println("Rol: " + usuario.getRol());
            System.out.println("Codigo turista: " + usuario.getCodigoTurista());
            System.out.println("--------------------");
        }
    }

    private void agregarUsuario() {
        System.out.println("===== AGREGAR USUARIO =====");

        System.out.print("Nombre de usuario: ");
        String nombreUsuario = teclado.nextLine();

        if (agencia.buscarUsuarioPorNombre(nombreUsuario) != null) {
            System.out.println("Ya existe un usuario con ese nombre.");
            return;
        }

        System.out.print("Contrasenia: ");
        String contrasenia = teclado.nextLine();

        RolUsuario rol = leerRolUsuario();
        Integer codigoTurista = null;

        if (rol == RolUsuario.CLIENTE) {
            if (agencia.getTuristas().isEmpty()) {
                System.out.println("No hay turistas cargados para asociar al usuario cliente.");
                return;
            }

            mostrarTuristas();
            codigoTurista = leerCodigoTuristaExistente();
        }

        Usuario usuario = new Usuario(nombreUsuario, contrasenia, rol, codigoTurista);

        if (agencia.agregarUsuario(usuario)) {
            controladorDatos.guardarTodo(agencia);
            System.out.println("Usuario agregado.");
        } else {
            System.out.println("No se pudo agregar el usuario.");
        }
    }

    private int leerCodigoSucursalNuevo() {
        int codigo;

        do {
            codigo = leerEntero("Codigo sucursal: ");

            if (agencia.buscarSucursalPorCodigo(codigo) != null) {
                System.out.println("Ya existe una sucursal con ese codigo.");
            }
        } while (agencia.buscarSucursalPorCodigo(codigo) != null);

        return codigo;
    }

    private int leerCodigoHotelNuevo() {
        int codigo;

        do {
            codigo = leerEntero("Codigo hotel: ");

            if (agencia.buscarHotelPorCodigo(codigo) != null) {
                System.out.println("Ya existe un hotel con ese codigo.");
            }
        } while (agencia.buscarHotelPorCodigo(codigo) != null);

        return codigo;
    }

    private int leerNumeroVueloNuevo() {
        int numero;

        do {
            numero = leerEntero("Numero vuelo: ");

            if (agencia.buscarVueloPorNumero(numero) != null) {
                System.out.println("Ya existe un vuelo con ese numero.");
            }
        } while (agencia.buscarVueloPorNumero(numero) != null);

        return numero;
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

    private RolUsuario leerRolUsuario() {
        while (true) {
            System.out.println("Rol:");
            System.out.println("1. CLIENTE");
            System.out.println("2. VENDEDOR");
            System.out.println("3. ADMINISTRADOR");

            int opcion = leerEntero("Seleccione rol: ");

            switch (opcion) {
                case 1:
                    return RolUsuario.CLIENTE;
                case 2:
                    return RolUsuario.VENDEDOR;
                case 3:
                    return RolUsuario.ADMINISTRADOR;
                default:
                    System.out.println("Rol invalido.");
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

    private LocalDateTime leerFechaYHora(String mensaje) {
        LocalDateTime fechaYHora = null;
        boolean valida = false;

        while (!valida) {
            System.out.print(mensaje);

            try {
                fechaYHora = LocalDateTime.parse(teclado.nextLine());
                valida = true;
            } catch (DateTimeParseException e) {
                System.out.println("Formato invalido. Use yyyy-mm-ddThh:mm.");
            }
        }

        return fechaYHora;
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
