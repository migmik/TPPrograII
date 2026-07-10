package tijetravel;

import tijetravel.controladores.ControladorDatos;
import tijetravel.modelos.Agencia;
import tijetravel.persistencia.PersistenciaException;
import tijetravel.vistas.VistaPrincipal;

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
