package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Turista;

public class ArchivoTuristas extends ArchivoTexto implements Archivo<Turista> {
    private static final Path RUTA_ARCHIVO = Path.of("TijeTravel", "datos", "turistas.txt");

    public ArrayList<Turista> cargar() {
        ArrayList<Turista> turistas = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return turistas;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";", -1);

                if (partes.length < 7) {
                    System.out.println("Linea de turista incompleta: " + linea);
                    continue;
                }

                int codigo = Integer.parseInt(partes[0]);
                String nombre = partes[1];
                String apellido = partes[2];
                String direccion = partes[3];
                String email = partes[4];
                String telefonoFijo = partes[5];
                String telefonoCelular = partes[6];
                boolean esTitular = true;
                Integer codigoTitular = null;

                if (partes.length > 7 && !partes[7].isEmpty()) {
                    esTitular = Boolean.parseBoolean(partes[7]);
                }

                if (partes.length > 8 && !partes[8].isEmpty()) {
                    codigoTitular = Integer.parseInt(partes[8]);
                }

                Turista turista = new Turista(
                        codigo,
                        nombre,
                        apellido,
                        direccion,
                        email,
                        telefonoFijo,
                        telefonoCelular,
                        esTitular,
                        codigoTitular);
                turistas.add(turista);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return turistas;
    }

    public void guardar(List<Turista> turistas) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Turista turista : turistas) {
                escritor.write(
                        turista.getCodigo() + ";"
                                + turista.getNombre() + ";"
                                + turista.getApellido() + ";"
                                + turista.getDireccion() + ";"
                                + turista.getEmail() + ";"
                                + turista.getTelefonoFijo() + ";"
                                + turista.getTelefonoCelular() + ";"
                                + turista.isEsTitular() + ";"
                                + (turista.getCodigoTitular() == null ? "" : turista.getCodigoTitular()));
                escritor.newLine();
            }
        });
    }
}
