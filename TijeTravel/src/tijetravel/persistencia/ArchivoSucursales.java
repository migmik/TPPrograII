package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Sucursal;

public class ArchivoSucursales {
    private static final String RUTA_ARCHIVO = "TijeTravel/datos/sucursales.txt";

    public ArrayList<Sucursal> cargar() {
        ArrayList<Sucursal> sucursales = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return sucursales;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");

                int codigo = Integer.parseInt(partes[0]);
                String direccion = partes[1];
                String telefono = partes[2];

                Sucursal sucursal = new Sucursal(codigo, direccion, telefono);
                sucursales.add(sucursal);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar sucursales: " + e.getMessage());
        }

        return sucursales;
    }

    public void guardar(List<Sucursal> sucursales) {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            for (Sucursal sucursal : sucursales) {
                escritor.write(
                        sucursal.getCodigo() + ";"
                        + sucursal.getDireccion() + ";"
                        + sucursal.getTelefono());
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar sucursales: " + e.getMessage());
        }
    }
}
