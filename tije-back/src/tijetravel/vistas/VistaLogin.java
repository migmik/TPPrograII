package tijetravel.vistas;

import java.util.Scanner;
import tijetravel.controladores.ControladorLogin;
import tijetravel.modelos.Usuario;

public class VistaLogin {
    private ControladorLogin controladorLogin;
    private Scanner teclado;

    public VistaLogin(ControladorLogin controladorLogin, Scanner teclado) {
        this.controladorLogin = controladorLogin;
        this.teclado = teclado;
    }

    public Usuario mostrar() {
        System.out.println("===== INICIAR SESION =====");

        System.out.print("Usuario: ");
        String nombreUsuario = teclado.nextLine();

        System.out.print("Contrasenia: ");
        String contrasenia = teclado.nextLine();

        Usuario usuario = controladorLogin.iniciarSesion(nombreUsuario, contrasenia);

        if (usuario == null) {
            System.out.println("Usuario o contrasenia incorrectos.");
            return null;
        }

        System.out.println("Inicio de sesion exitoso.");
        System.out.println("Rol: " + usuario.getRol());

        return usuario;
    }
}
