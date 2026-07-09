package tijetravel.persistencia;

import java.util.ArrayList;

public interface Archivo<T> {
    ArrayList<T> cargar();

    void guardar(ArrayList<T> lista);
}
