package tijetravel.vistas;

import java.util.Scanner;

import tijetravel.controladores.ControladorDatos;
import tijetravel.controladores.ControladorReservas;
import tijetravel.controladores.ControladorTuristas;
import tijetravel.controladores.ControladorAdministracion;
import tijetravel.controladores.ControladorUsuarios;
import tijetravel.modelos.Agencia;
import tijetravel.modelos.Usuario;

public abstract class VistaUsuario {
    protected Agencia agencia;
    protected ControladorDatos controladorDatos;
    protected ControladorReservas controladorReservas;
    protected ControladorTuristas controladorTuristas;
    protected ControladorAdministracion controladorAdministracion;
    protected ControladorUsuarios controladorUsuarios;
    protected Scanner teclado;

    protected VistaUsuario(Agencia agencia, ControladorDatos controladorDatos, Scanner teclado) {
        this.agencia = agencia;
        this.controladorDatos = controladorDatos;
        this.controladorReservas = new ControladorReservas(agencia);
        this.controladorTuristas = new ControladorTuristas(agencia);
        this.controladorAdministracion = new ControladorAdministracion(agencia);
        this.controladorUsuarios = new ControladorUsuarios(agencia);
        this.teclado = teclado;
    }

    public abstract void mostrar(Usuario usuario);

    protected int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                return Integer.parseInt(teclado.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }
}
