package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Hotel;

public class ArchivoHoteles extends ArchivoTexto implements Archivo<Hotel> {
    private static final Path RUTA_ARCHIVO = Path.of("TijeTravel", "datos", "hoteles.txt");

    public ArrayList<Hotel> cargar() {
        ArrayList<Hotel> hoteles = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return hoteles;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");

                int codigo = Integer.parseInt(partes[0]);
                String nombre = partes[1];
                String direccion = partes[2];
                String ciudad = partes[3];
                String telefono = partes[4];
                int plazasDisponibles = Integer.parseInt(partes[5]);

                Hotel hotel = new Hotel(codigo, nombre, direccion, ciudad, telefono, plazasDisponibles);
                hoteles.add(hotel);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return hoteles;
    }

    public void guardar(List<Hotel> hoteles) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Hotel hotel : hoteles) {
                escritor.write(
                        hotel.getCodigo() + ";"
                                + hotel.getNombre() + ";"
                                + hotel.getDireccion() + ";"
                                + hotel.getCiudad() + ";"
                                + hotel.getTelefono() + ";"
                                + hotel.getPlazasDisponibles());
                escritor.newLine();
            }
        });
    }
}
