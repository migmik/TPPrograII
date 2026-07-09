package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import tijetravel.modelos.RolUsuario;
import tijetravel.modelos.Usuario;

public class ArchivoUsuarios extends ArchivoTexto implements Archivo<Usuario> {
    private static final String RUTA_ARCHIVO = "TijeTravel/datos/usuarios.txt";

    @Override
    public ArrayList<Usuario> cargar() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return usuarios;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (lineaVacia(linea)) {
                    continue;
                }

                String[] partes = separarCampos(linea);

                if (partes.length < 3) {
                    System.out.println("Linea de usuario incompleta: " + linea);
                    continue;
                }

                String nombreUsuario = partes[0];
                String contrasenia = partes[1];
                RolUsuario rol = RolUsuario.valueOf(partes[2]);
                Integer codigoTurista = null;

                if (partes.length > 3 && !partes[3].isEmpty()) {
                    codigoTurista = Integer.parseInt(partes[3]);
                }

                Usuario usuario = new Usuario(nombreUsuario, contrasenia, rol, codigoTurista);
                usuarios.add(usuario);
            }
        } catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    @Override
    public void guardar(ArrayList<Usuario> usuarios) {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            for (Usuario usuario : usuarios) {
                escritor.write(
                        usuario.getNombreUsuario() + ";"
                        + usuario.getContrasenia() + ";"
                        + usuario.getRol() + ";"
                        + (usuario.getCodigoTurista() == null ? "" : usuario.getCodigoTurista()));
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar usuarios: " + e.getMessage());
        }
    }
}
