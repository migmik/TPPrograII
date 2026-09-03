package com.tijetravel.tije_back;

import com.tijetravel.tije_back.controladores.ControladorDatos;
import com.tijetravel.tije_back.modelos.Agencia;
import com.tijetravel.tije_back.persistencia.PersistenciaException;
import com.tijetravel.tije_back.vistas.VistaPrincipal;

public class Main {

    public static void main(String[] args) {
        ControladorDatos controladorDatos = new ControladorDatos();

        try {
            Agencia agencia = controladorDatos.cargarTodo();
            VistaPrincipal vistaPrincipal = new VistaPrincipal(agencia, controladorDatos);
            vistaPrincipal.iniciar();
        } catch (PersistenciaException e) {
            System.out.println("No se pudo iniciar o guardar el sistema: " + e.getMessage());
        }
    }
}
