package tijetravel.vistas;

import java.util.Scanner;
import tijetravel.controladores.ControladorDatos;
import tijetravel.controladores.ControladorLogin;
import tijetravel.modelos.Agencia;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.Vuelo;

/*Vista principal del programa
muesta menu inicial, permite ver hoteles,vuelos.
Maneja el inicio de sesion */
public class VistaPrincipal {
    private Agencia agencia;
    private ControladorDatos controladorDatos;
    private Scanner teclado;

    // constructor de la vista principal
    public VistaPrincipal(Agencia agencia, ControladorDatos controladorDatos) {
        this.agencia = agencia;
        this.controladorDatos = controladorDatos;
        this.teclado = new Scanner(System.in);
    }

    // CICLO PRINCIPAL
    // muestra menu, ejecuta la opcion hasta salir
    public void iniciar() {
        int opcion;

        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    mostrarHoteles();
                    break;
                case 2:
                    mostrarVuelos();
                    break;
                case 3:
                    iniciarSesion();
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    // muestra menu principal
    private void mostrarMenuPrincipal() {
        System.out.println("===== TIJE TRAVEL =====");
        System.out.println("1. Ver hoteles disponibles");
        System.out.println("2. Ver vuelos disponibles");
        System.out.println("3. Iniciar sesion");
        System.out.println("0. Salir");
    }

    // muestra hoteles disponibles
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

    // muestra vuelos disponibles
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
            System.out.println("Capacidad turista: " + vuelo.getPlazasTurista());
            System.out.println("Capacidad primera: " + vuelo.getPlazasPrimera());
            System.out.println("--------------------");
        }
    }

    // inicia el login y redirige segun el rol
    private void iniciarSesion() {
        ControladorLogin controladorLogin = new ControladorLogin(agencia);
        VistaLogin vistaLogin = new VistaLogin(controladorLogin, teclado);
        Usuario usuario = vistaLogin.mostrar();

        if (usuario == null) {
            return;
        }

        VistaUsuario vistaUsuario;

        switch (usuario.getRol()) {
            case CLIENTE:
                vistaUsuario = new VistaCliente(agencia, controladorDatos, teclado);
                break;
            case VENDEDOR:
                vistaUsuario = new VistaVendedor(agencia, controladorDatos, teclado);
                break;
            case ADMINISTRADOR:
                vistaUsuario = new VistaAdministrador(agencia, controladorDatos, teclado);
                break;
            default:
                System.out.println("Rol no reconocido.");
                return;
        }

        vistaUsuario.mostrar(usuario);
    }

    // valida entrada de numeros enteros en un ciclo
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
