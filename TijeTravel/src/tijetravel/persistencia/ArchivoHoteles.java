package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import tijetravel.models.Hotel;

public class ArchivoHoteles {
    private static final String RUTA_ARCHIVO = "TijeTravel/datos/hoteles.txt";

    public ArrayList<Hotel> cargar() {
        ArrayList<Hotel> hoteles = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return hoteles;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
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
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar hoteles: " + e.getMessage());
        }

        return hoteles;
    }

    public void guardar(ArrayList<Hotel> hoteles) {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
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
        } catch (IOException e) {
            System.out.println("Error al guardar hoteles: " + e.getMessage());
        }
    }
}
