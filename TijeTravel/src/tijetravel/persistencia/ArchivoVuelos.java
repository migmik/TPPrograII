package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import tijetravel.models.Vuelo;

public class ArchivoVuelos {
    private static final String RUTA_ARCHIVO = "TijeTravel/datos/vuelos.txt";

    public ArrayList<Vuelo> cargar() {
        ArrayList<Vuelo> vuelos = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return vuelos;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");

                int numero = Integer.parseInt(partes[0]);
                LocalDateTime fechaYHora = LocalDateTime.parse(partes[1]);
                String origen = partes[2];
                String destino = partes[3];
                int totalPlazas = Integer.parseInt(partes[4]);
                int plazasTurista = Integer.parseInt(partes[5]);

                Vuelo vuelo = new Vuelo(numero, fechaYHora, origen, destino, totalPlazas, plazasTurista);
                vuelos.add(vuelo);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar vuelos: " + e.getMessage());
        }

        return vuelos;
    }

    public void guardar(ArrayList<Vuelo> vuelos) {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            for (Vuelo vuelo : vuelos) {
                escritor.write(
                        vuelo.getNumero() + ";"
                        + vuelo.getFechaYHora() + ";"
                        + vuelo.getOrigen() + ";"
                        + vuelo.getDestino() + ";"
                        + vuelo.getTotalPlazas() + ";"
                        + vuelo.getPlazasTurista());
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar vuelos: " + e.getMessage());
        }
    }
}
