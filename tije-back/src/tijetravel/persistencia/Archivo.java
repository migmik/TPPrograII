package tijetravel.persistencia;

import java.util.ArrayList;
import java.util.List;

public interface Archivo<T> {
    ArrayList<T> cargar();

    void guardar(List<T> lista);
}
