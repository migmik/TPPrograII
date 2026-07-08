package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import tijetravel.modelos.Turista;

public class ArchivoTuristas {
    private static final String RUTA_ARCHIVO = "TijeTravel/datos/turistas.txt";

    public ArrayList<Turista> cargar() {
        ArrayList<Turista> turistas = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return turistas;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");

                int codigo = Integer.parseInt(partes[0]);
                String nombre = partes[1];
                String apellido = partes[2];
                String direccion = partes[3];
                String email = partes[4];
                String telefonoFijo = partes[5];
                String telefonoCelular = partes[6];

                Turista turista = new Turista(
                        codigo,
                        nombre,
                        apellido,
                        direccion,
                        email,
                        telefonoFijo,
                        telefonoCelular);
                turistas.add(turista);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar turistas: " + e.getMessage());
        }

        return turistas;
    }

    public void guardar(ArrayList<Turista> turistas) {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            for (Turista turista : turistas) {
                escritor.write(
                        turista.getCodigo() + ";"
                        + turista.getNombre() + ";"
                        + turista.getApellido() + ";"
                        + turista.getDireccion() + ";"
                        + turista.getEmail() + ";"
                        + turista.getTelefonoFijo() + ";"
                        + turista.getTelefonoCelular());
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar turistas: " + e.getMessage());
        }
    }
}
