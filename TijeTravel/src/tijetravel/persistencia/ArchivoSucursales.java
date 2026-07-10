package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Sucursal;

public class ArchivoSucursales extends ArchivoTexto implements Archivo<Sucursal> {
    private static final Path RUTA_ARCHIVO = rutaDatos("sucursales.txt");

    public ArrayList<Sucursal> cargar() {
        ArrayList<Sucursal> sucursales = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return sucursales;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (lineaVacia(linea)) {
                    continue;
                }

                String[] partes = separarCampos(linea);

                if (partes.length < 3) {
                    System.out.println("Linea de sucursal incompleta: " + linea);
                    continue;
                }

                int codigo = Integer.parseInt(partes[0]);
                String direccion = partes[1];
                String telefono = partes[2];

                Sucursal sucursal = new Sucursal(codigo, direccion, telefono);
                sucursales.add(sucursal);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return sucursales;
    }

    public void guardar(List<Sucursal> sucursales) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Sucursal sucursal : sucursales) {
                escritor.write(
                        sucursal.getCodigo() + ";"
                                + sucursal.getDireccion() + ";"
                                + sucursal.getTelefono());
                escritor.newLine();
            }
        });
    }
}
