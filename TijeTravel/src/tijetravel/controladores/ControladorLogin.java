package tijetravel.controladores;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.Usuario;

//controlador para autenticacion de usuarios
public class ControladorLogin {
    private Agencia agencia;

    // constructor: recibe agencia con los usuarios ya cargados
    public ControladorLogin(Agencia agencia) {
        this.agencia = agencia;
    }

    // intenta iniciar sesion con los datos dados
    public Usuario iniciarSesion(String nombreUsuario, String contrasenia) {
        // busca usuario por nombre
        Usuario usuario = agencia.buscarUsuarioPorNombre(nombreUsuario);
        // si el usuario no existe, no inicia sesion
        if (usuario == null) {
            return null;
        }
        // autoriza si la contraseña coincide
        if (!usuario.getContrasenia().equals(contrasenia)) {
            return null;
        }

        return usuario;
    }
}
