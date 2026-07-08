package tijetravel;

import tijetravel.controladores.ControladorDatos;
import tijetravel.modelos.Agencia;
import tijetravel.vistas.VistaPrincipal;

public class Main {

    public static void main(String[] args) {
        Agencia agencia = new Agencia();
        ControladorDatos controladorDatos = new ControladorDatos();

        controladorDatos.cargarTodo(agencia);

        VistaPrincipal vistaPrincipal = new VistaPrincipal(agencia, controladorDatos);
        vistaPrincipal.iniciar();
    }
}
