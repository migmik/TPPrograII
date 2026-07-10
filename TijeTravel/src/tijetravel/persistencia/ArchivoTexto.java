package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class ArchivoTexto {
    protected static final String SEPARADOR = ";";

    protected boolean lineaVacia(String linea) {
        return linea == null || linea.trim().isEmpty();
    }

    protected String[] separarCampos(String linea) {
        return linea.split(SEPARADOR, -1);
    }

    protected BufferedReader abrirLector(Path ruta) throws IOException {
        return Files.newBufferedReader(ruta, StandardCharsets.UTF_8);
    }

    protected void guardarAtomico(Path ruta, EscrituraArchivo escritura) {
        Path temporal = ruta.resolveSibling(ruta.getFileName() + ".tmp");

        try {
            Files.createDirectories(ruta.getParent());

            try (BufferedWriter escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                escritura.escribir(escritor);
            }

            try {
                Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignorada) {
                // Se conserva la causa original.
            }
            throw new PersistenciaException("No se pudo guardar el archivo " + ruta, e);
        }
    }

    protected PersistenciaException errorCarga(Path ruta, Exception causa) {
        return new PersistenciaException("No se pudo cargar el archivo " + ruta, causa);
    }
}
