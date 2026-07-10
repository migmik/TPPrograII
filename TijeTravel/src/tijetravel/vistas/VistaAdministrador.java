package tijetravel.vistas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
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

public class VistaAdministrador extends VistaUsuario {
    private Usuario usuarioActual;

    public VistaAdministrador(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
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
        System.out.println("4. Administrar clientes");
        System.out.println("5. Administrar reservas");
        System.out.println("6. Administrar usuarios");
        System.out.println("0. Cerrar sesion");
    }

    private void administrarSucursales() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR SUCURSALES =====");
            System.out.println("1. Listar sucursales");
            System.out.println("2. Buscar sucursal");
            System.out.println("3. Agregar sucursal");
            System.out.println("4. Modificar sucursal");
            System.out.println("5. Eliminar sucursal");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarSucursales();
                    break;
                case 2:
                    buscarSucursal();
                    break;
                case 3:
                    agregarSucursal();
                    break;
                case 4:
                    modificarSucursal();
                    break;
                case 5:
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
            System.out.println("2. Buscar hotel");
            System.out.println("3. Agregar hotel");
            System.out.println("4. Modificar hotel");
            System.out.println("5. Eliminar hotel");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarHoteles();
                    break;
                case 2:
                    buscarHotel();
                    break;
                case 3:
                    agregarHotel();
                    break;
                case 4:
                    modificarHotel();
                    break;
                case 5:
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
            System.out.println("2. Buscar vuelo");
            System.out.println("3. Agregar vuelo");
            System.out.println("4. Modificar vuelo");
            System.out.println("5. Eliminar vuelo");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarVuelos();
                    break;
                case 2:
                    buscarVuelo();
                    break;
                case 3:
                    agregarVuelo();
                    break;
                case 4:
                    modificarVuelo();
                    break;
                case 5:
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
                    agregarTurista();
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
        } while (opcion != 0);
    }

    private void administrarUsuarios() {
        int opcion;

        do {
            System.out.println("===== ADMINISTRAR USUARIOS =====");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Buscar usuario");
            System.out.println("3. Agregar usuario");
            System.out.println("4. Modificar usuario");
            System.out.println("5. Eliminar usuario");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarUsuarios();
                    break;
                case 2:
                    buscarUsuario();
                    break;
                case 3:
                    agregarUsuario();
                    break;
                case 4:
                    modificarUsuario();
                    break;
                case 5:
                    eliminarUsuario();
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

        if (controladorAdministracion.agregarSucursal(usuarioActual, new Sucursal(codigo, direccion, telefono))) {
            guardarCambios();
            System.out.println("Sucursal agregada.");
        } else {
            System.out.println("No se pudo agregar la sucursal.");
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
            System.out.println("Capacidad de plazas: " + hotel.getPlazasDisponibles());
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

        int plazasDisponibles = leerEnteroNoNegativo("Plazas disponibles: ");

        Hotel hotel = new Hotel(codigo, nombre, direccion, ciudad, telefono, plazasDisponibles);

        if (controladorAdministracion.agregarHotel(usuarioActual, hotel)) {
            guardarCambios();
            System.out.println("Hotel agregado.");
        } else {
            System.out.println("No se pudo agregar el hotel.");
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
            System.out.println("Turista disponibles: " + controladorReservas.plazasDisponiblesVuelo(vuelo, ClaseVuelo.TURISTA)
                    + " de " + vuelo.getPlazasTurista());
            System.out.println("Primera disponibles: " + controladorReservas.plazasDisponiblesVuelo(vuelo, ClaseVuelo.PRIMERA)
                    + " de " + vuelo.getPlazasPrimera());
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

        int totalPlazas = leerEnteroPositivo("Plazas totales: ");
        int plazasTurista = leerPlazasTurista(totalPlazas);
        int plazasPrimera = leerPlazasPrimera(totalPlazas, plazasTurista);

        Vuelo vuelo = new Vuelo(numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera);

        if (controladorAdministracion.agregarVuelo(usuarioActual, vuelo)) {
            guardarCambios();
            System.out.println("Vuelo agregado.");
        } else {
            System.out.println("No se pudo agregar el vuelo.");
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

        Turista turista = controladorTuristas.agregarTitular(usuarioActual, nombre, apellido, direccion, email,
                telefonoFijo, telefonoCelular);

        if (turista != null) {
            guardarCambios();
            System.out.println("Titular agregado con codigo: " + codigo);
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

        guardarCambios();
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
            guardarCambios();
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
            if (!hayTitulares()) {
                System.out.println("No hay titulares cargados para asociar al usuario cliente.");
                return;
            }

            mostrarTuristas();
            codigoTurista = leerCodigoTitularExistente();
        }

        Usuario usuario = new Usuario(nombreUsuario, contrasenia, rol, codigoTurista);

        if (controladorUsuarios.agregar(usuarioActual, usuario)) {
            guardarCambios();
            System.out.println("Usuario agregado.");
        } else {
            System.out.println("No se pudo agregar el usuario.");
        }
    }

    private void buscarSucursal() {
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(leerEntero("Codigo sucursal: "));
        if (sucursal == null) {
            System.out.println("No existe una sucursal con ese codigo.");
            return;
        }
        System.out.println("Codigo: " + sucursal.getCodigo());
        System.out.println("Direccion: " + sucursal.getDireccion());
        System.out.println("Telefono: " + sucursal.getTelefono());
    }

    private void modificarSucursal() {
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(leerEntero("Codigo sucursal: "));
        if (sucursal == null) {
            System.out.println("No existe una sucursal con ese codigo.");
            return;
        }
        System.out.print("Nueva direccion: ");
        String direccion = teclado.nextLine();
        System.out.print("Nuevo telefono: ");
        String telefono = teclado.nextLine();
        if (controladorAdministracion.modificarSucursal(usuarioActual, sucursal.getCodigo(), direccion, telefono))
            guardarYMostrar("Sucursal actualizada.");
    }

    private void eliminarSucursal() {
        int codigo = leerEntero("Codigo sucursal: ");
        if (agencia.sucursalTieneReservas(codigo)) {
            System.out.println("No se puede eliminar: la sucursal tiene reservas.");
        } else if (controladorAdministracion.eliminarSucursal(usuarioActual, codigo)) {
            guardarYMostrar("Sucursal eliminada.");
        } else {
            System.out.println("No existe una sucursal con ese codigo.");
        }
    }

    private void buscarHotel() {
        Hotel hotel = agencia.buscarHotelPorCodigo(leerEntero("Codigo hotel: "));
        if (hotel == null) {
            System.out.println("No existe un hotel con ese codigo.");
            return;
        }
        System.out.println("Codigo: " + hotel.getCodigo());
        System.out.println("Nombre: " + hotel.getNombre());
        System.out.println("Ciudad: " + hotel.getCiudad());
        System.out.println("Capacidad de plazas: " + hotel.getPlazasDisponibles());
    }

    private void modificarHotel() {
        Hotel hotel = agencia.buscarHotelPorCodigo(leerEntero("Codigo hotel: "));
        if (hotel == null) {
            System.out.println("No existe un hotel con ese codigo.");
            return;
        }
        System.out.print("Nuevo nombre: ");
        String nombre = teclado.nextLine();
        System.out.print("Nueva direccion: ");
        String direccion = teclado.nextLine();
        System.out.print("Nueva ciudad: ");
        String ciudad = teclado.nextLine();
        System.out.print("Nuevo telefono: ");
        String telefono = teclado.nextLine();
        int plazas = leerEnteroNoNegativo("Plazas disponibles: ");
        if (controladorAdministracion.modificarHotel(usuarioActual, hotel.getCodigo(), nombre, direccion,
                ciudad, telefono, plazas)) guardarYMostrar("Hotel actualizado.");
    }

    private void eliminarHotel() {
        int codigo = leerEntero("Codigo hotel: ");
        if (agencia.hotelTieneReservas(codigo)) {
            System.out.println("No se puede eliminar: el hotel tiene reservas.");
        } else if (controladorAdministracion.eliminarHotel(usuarioActual, codigo)) {
            guardarYMostrar("Hotel eliminado.");
        } else {
            System.out.println("No existe un hotel con ese codigo.");
        }
    }

    private void buscarVuelo() {
        Vuelo vuelo = agencia.buscarVueloPorNumero(leerEntero("Numero vuelo: "));
        if (vuelo == null) {
            System.out.println("No existe un vuelo con ese numero.");
            return;
        }
        System.out.println("Numero: " + vuelo.getNumero());
        System.out.println("Fecha y hora: " + vuelo.getFechaYHora());
        System.out.println("Origen: " + vuelo.getOrigen());
        System.out.println("Destino: " + vuelo.getDestino());
    }

    private void modificarVuelo() {
        Vuelo vuelo = agencia.buscarVueloPorNumero(leerEntero("Numero vuelo: "));
        if (vuelo == null) {
            System.out.println("No existe un vuelo con ese numero.");
            return;
        }
        LocalDateTime fecha = leerFechaYHora("Nueva fecha y hora (yyyy-mm-ddThh:mm): ");
        System.out.print("Nuevo origen: ");
        String origen = teclado.nextLine();
        System.out.print("Nuevo destino: ");
        String destino = teclado.nextLine();
        int total = leerEnteroPositivo("Plazas totales: ");
        int turista = leerPlazasTurista(total);
        int primera = leerPlazasPrimera(total, turista);
        if (controladorAdministracion.modificarVuelo(usuarioActual, vuelo.getNumero(), fecha, origen,
                destino, total, turista, primera)) guardarYMostrar("Vuelo actualizado.");
    }

    private void eliminarVuelo() {
        int numero = leerEntero("Numero vuelo: ");
        if (agencia.vueloTieneReservas(numero)) {
            System.out.println("No se puede eliminar: el vuelo tiene reservas.");
        } else if (controladorAdministracion.eliminarVuelo(usuarioActual, numero)) {
            guardarYMostrar("Vuelo eliminado.");
        } else {
            System.out.println("No existe un vuelo con ese numero.");
        }
    }

    private void agregarFamiliar() {
        if (!hayTitulares()) {
            System.out.println("No hay titulares cargados.");
            return;
        }
        int codigoTitular = leerCodigoTitularExistente();
        int codigo = agencia.generarCodigoTurista();
        System.out.print("Nombre: "); String nombre = teclado.nextLine();
        System.out.print("Apellido: "); String apellido = teclado.nextLine();
        System.out.print("Direccion: "); String direccion = teclado.nextLine();
        System.out.print("Email: "); String email = teclado.nextLine();
        System.out.print("Telefono fijo: "); String fijo = teclado.nextLine();
        System.out.print("Telefono celular: "); String celular = teclado.nextLine();
        Turista familiar = controladorTuristas.agregarFamiliar(usuarioActual, codigoTitular, nombre, apellido,
                direccion, email, fijo, celular);
        if (familiar != null) guardarYMostrar("Familiar agregado con codigo: " + familiar.getCodigo());
    }

    private void modificarTurista() {
        Turista turista = agencia.buscarTuristaPorCodigo(leerEntero("Codigo turista: "));
        if (turista == null) {
            System.out.println("No existe un turista con ese codigo.");
            return;
        }
        System.out.print("Nuevo nombre: "); String nombre = teclado.nextLine();
        System.out.print("Nuevo apellido: "); String apellido = teclado.nextLine();
        System.out.print("Nueva direccion: "); String direccion = teclado.nextLine();
        System.out.print("Nuevo email: "); String email = teclado.nextLine();
        System.out.print("Nuevo telefono fijo: "); String fijo = teclado.nextLine();
        System.out.print("Nuevo telefono celular: "); String celular = teclado.nextLine();
        if (controladorTuristas.modificar(usuarioActual, turista.getCodigo(), nombre, apellido, direccion,
                email, fijo, celular)) guardarYMostrar("Turista actualizado.");
    }

    private void eliminarTurista() {
        int codigo = leerEntero("Codigo turista: ");
        if (agencia.turistaTieneReservas(codigo)) System.out.println("No se puede eliminar: tiene reservas.");
        else if (agencia.titularTieneFamiliares(codigo)) System.out.println("No se puede eliminar: tiene familiares.");
        else if (agencia.turistaTieneUsuario(codigo)) System.out.println("No se puede eliminar: tiene usuario.");
        else if (controladorTuristas.eliminar(usuarioActual, codigo)) guardarYMostrar("Turista eliminado.");
        else System.out.println("No existe un turista con ese codigo.");
    }

    private void buscarReserva() {
        Reserva reserva = agencia.buscarReservaPorCodigo(leerEntero("Codigo reserva: "));
        if (reserva == null) System.out.println("No existe una reserva con ese codigo.");
        else mostrarReserva(reserva);
    }

    private void buscarUsuario() {
        System.out.print("Nombre de usuario: ");
        Usuario usuario = agencia.buscarUsuarioPorNombre(teclado.nextLine());
        if (usuario == null) System.out.println("No existe ese usuario.");
        else {
            System.out.println("Usuario: " + usuario.getNombreUsuario());
            System.out.println("Rol: " + usuario.getRol());
            System.out.println("Codigo turista: " + usuario.getCodigoTurista());
        }
    }

    private void modificarUsuario() {
        System.out.print("Nombre de usuario actual: ");
        Usuario usuario = agencia.buscarUsuarioPorNombre(teclado.nextLine());
        if (usuario == null) {
            System.out.println("No existe ese usuario.");
            return;
        }
        System.out.print("Nuevo nombre de usuario: ");
        String nuevoNombre = teclado.nextLine();
        Usuario existente = agencia.buscarUsuarioPorNombre(nuevoNombre);
        if (existente != null && existente != usuario) {
            System.out.println("Ya existe un usuario con ese nombre.");
            return;
        }
        System.out.print("Nueva contrasenia: ");
        String contrasenia = teclado.nextLine();
        RolUsuario nuevoRol = leerRolUsuario();
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR && nuevoRol != RolUsuario.ADMINISTRADOR
                && agencia.contarAdministradores() <= 1) {
            System.out.println("No se puede cambiar el rol del ultimo administrador.");
            return;
        }
        Integer codigoTurista = null;
        if (nuevoRol == RolUsuario.CLIENTE) codigoTurista = leerCodigoTitularExistente();
        if (controladorUsuarios.modificar(usuarioActual, usuario.getNombreUsuario(), nuevoNombre, contrasenia,
                nuevoRol, codigoTurista)) guardarYMostrar("Usuario actualizado.");
    }

    private void eliminarUsuario() {
        System.out.print("Nombre de usuario: ");
        String nombre = teclado.nextLine();
        Usuario usuario = agencia.buscarUsuarioPorNombre(nombre);
        if (usuario == null) System.out.println("No existe ese usuario.");
        else if (usuario == usuarioActual) System.out.println("No puede eliminar el usuario de la sesion actual.");
        else if (usuario.getRol() == RolUsuario.ADMINISTRADOR && agencia.contarAdministradores() <= 1)
            System.out.println("No se puede eliminar el ultimo administrador.");
        else if (controladorUsuarios.eliminar(usuarioActual, nombre)) guardarYMostrar("Usuario eliminado.");
    }

    private boolean hayTitulares() {
        for (Turista turista : agencia.getTuristas()) if (turista.isEsTitular()) return true;
        return false;
    }

    private int leerCodigoTitularExistente() {
        int codigo;
        Turista titular;
        do {
            codigo = leerEntero("Codigo titular: ");
            titular = agencia.buscarTuristaPorCodigo(codigo);
            if (titular == null || !titular.isEsTitular()) System.out.println("No existe un titular con ese codigo.");
        } while (titular == null || !titular.isEsTitular());
        return codigo;
    }

    private int leerEnteroPositivo(String mensaje) {
        int valor;
        do {
            valor = leerEntero(mensaje);
            if (valor <= 0) System.out.println("Debe ser mayor que cero.");
        } while (valor <= 0);
        return valor;
    }

    private int leerEnteroNoNegativo(String mensaje) {
        int valor;
        do {
            valor = leerEntero(mensaje);
            if (valor < 0) System.out.println("No puede ser negativo.");
        } while (valor < 0);
        return valor;
    }

    private int leerPlazasTurista(int total) {
        int valor;
        do {
            valor = leerEnteroNoNegativo("Plazas turista: ");
            if (valor > total) System.out.println("No puede superar las plazas totales.");
        } while (valor > total);
        return valor;
    }

    private int leerPlazasPrimera(int total, int turista) {
        int valor;
        do {
            valor = leerEnteroNoNegativo("Plazas primera: ");
            if (turista + valor > total) {
                System.out.println("La suma de plazas turista y primera no puede superar el total.");
            }
        } while (turista + valor > total);
        return valor;
    }

    private void guardarYMostrar(String mensaje) {
        guardarCambios();
        System.out.println(mensaje);
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

}
