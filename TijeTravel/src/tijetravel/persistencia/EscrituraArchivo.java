package tijetravel.persistencia;

import java.io.BufferedWriter;
import java.io.IOException;

@FunctionalInterface
public interface EscrituraArchivo {
    void escribir(BufferedWriter escritor) throws IOException;
}
