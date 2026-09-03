package com.tijetravel.tije_back.vistas;

import java.util.Scanner;

import com.tijetravel.tije_back.controladores.ControladorAdministracion;
import com.tijetravel.tije_back.controladores.ControladorDatos;
import com.tijetravel.tije_back.controladores.ControladorReservas;
import com.tijetravel.tije_back.controladores.ControladorTuristas;
import com.tijetravel.tije_back.controladores.ControladorUsuarios;
import com.tijetravel.tije_back.modelos.Agencia;
import com.tijetravel.tije_back.modelos.Usuario;
import com.tijetravel.tije_back.persistencia.PersistenciaException;

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

    protected boolean guardarCambios() {
        try {
            controladorDatos.guardarTodo(agencia);
            return true;
        } catch (PersistenciaException e) {
            System.out.println("No se pudieron guardar los cambios: " + e.getMessage());
            return false;
        }
    }

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
