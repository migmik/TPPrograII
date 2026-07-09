package tijetravel.controladores;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.Usuario;

public class ControladorLogin {
    private Agencia agencia;

    public ControladorLogin(Agencia agencia) {
        this.agencia = agencia;
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasenia) {
        Usuario usuario = agencia.buscarUsuarioPorNombre(nombreUsuario);

        if (usuario == null) {
            return null;
        }

        if (!usuario.getContrasenia().equals(contrasenia)) {
            return null;
        }

        return usuario;
    }
}
