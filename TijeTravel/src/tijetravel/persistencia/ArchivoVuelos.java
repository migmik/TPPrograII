package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Vuelo;

public class ArchivoVuelos extends ArchivoTexto implements Archivo<Vuelo> {
    private static final Path RUTA_ARCHIVO = Path.of("TijeTravel", "datos", "vuelos.txt");

    public ArrayList<Vuelo> cargar() {
        ArrayList<Vuelo> vuelos = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return vuelos;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");

                int numero = Integer.parseInt(partes[0]);
                LocalDateTime fechaYHora = LocalDateTime.parse(partes[1]);
                String origen = partes[2];
                String destino = partes[3];
                int totalPlazas = Integer.parseInt(partes[4]);
                int plazasTurista = Integer.parseInt(partes[5]);
                int plazasPrimera = partes.length > 6 && !partes[6].isEmpty()
                        ? Integer.parseInt(partes[6])
                        : totalPlazas - plazasTurista;

                Vuelo vuelo = new Vuelo(numero, fechaYHora, origen, destino, totalPlazas,
                        plazasTurista, plazasPrimera);
                vuelos.add(vuelo);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return vuelos;
    }

    public void guardar(List<Vuelo> vuelos) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Vuelo vuelo : vuelos) {
                escritor.write(
                        vuelo.getNumero() + ";"
                        + vuelo.getFechaYHora() + ";"
                        + vuelo.getOrigen() + ";"
                        + vuelo.getDestino() + ";"
                        + vuelo.getTotalPlazas() + ";"
                        + vuelo.getPlazasTurista() + ";"
                        + vuelo.getPlazasPrimera());
                escritor.newLine();
            }
        });
    }
}
