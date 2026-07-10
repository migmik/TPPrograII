package tijetravel;

import tijetravel.controladores.ControladorDatos;
import tijetravel.modelos.Agencia;
import tijetravel.persistencia.PersistenciaException;
import tijetravel.vistas.VistaPrincipal;

public class Main {

    /*
     * Punto de entrada.
     * crea el controlador de datos, carga la persistencia y vista principal
     */
    public static void main(String[] args) {
        // controlador de carga y guardado.
        ControladorDatos controladorDatos = new ControladorDatos();

        try {
            // carga la info de la agencia
            Agencia agencia = controladorDatos.cargarTodo();
            // arranca vista principal con todo lo cargado
            VistaPrincipal vistaPrincipal = new VistaPrincipal(agencia, controladorDatos);
            vistaPrincipal.iniciar();
        } catch (PersistenciaException e) {
            // avisa que falla la lectura o escritura de los txt
            System.out.println("No se pudo iniciar o guardar el sistema: " + e.getMessage());
        }
    }
}
