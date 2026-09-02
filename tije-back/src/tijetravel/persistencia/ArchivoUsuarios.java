package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.RolUsuario;
import tijetravel.modelos.Usuario;
import tijetravel.modelos.UsuarioFactory;

public class ArchivoUsuarios extends ArchivoTexto implements Archivo<Usuario> {
    private static final Path RUTA_ARCHIVO = rutaDatos("usuarios.txt");

    public ArrayList<Usuario> cargar() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return usuarios;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";", -1);

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

                Usuario usuario = UsuarioFactory.crear(nombreUsuario, contrasenia, rol, codigoTurista);

                if (usuario == null) {
                    System.out.println("Usuario omitido por rol o asociacion invalida: " + linea);
                    continue;
                }

                usuarios.add(usuario);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return usuarios;
    }

    public void guardar(List<Usuario> usuarios) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Usuario usuario : usuarios) {
                escritor.write(
                        usuario.getNombreUsuario() + ";"
                                + usuario.getContrasenia() + ";"
                                + usuario.getRol() + ";"
                                + (usuario.getCodigoTurista() == null ? "" : usuario.getCodigoTurista()));
                escritor.newLine();
            }
        });
    }
}
