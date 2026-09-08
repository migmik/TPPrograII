package com.tijetravel.tijeback.modelos;

import com.tijetravel.tijeback.enums.RolUsuario;

public final class UsuarioFactory {

    private UsuarioFactory() {
    }

    public static Usuario crear(
            String nombreUsuario,
            String contrasenia,
            RolUsuario rol,
            Turista turista) {
        ValidacionModelo.obligatorio(rol, "rol");

        return switch (rol) {
            case CLIENTE -> new Cliente(nombreUsuario, contrasenia, turista);
            case VENDEDOR -> {
                validarSinTurista(turista, rol);
                yield new Vendedor(nombreUsuario, contrasenia);
            }
            case ADMINISTRADOR -> {
                validarSinTurista(turista, rol);
                yield new Administrador(nombreUsuario, contrasenia);
            }
        };
    }

    private static void validarSinTurista(Turista turista, RolUsuario rol) {
        if (turista != null) {
            throw new IllegalArgumentException("El rol " + rol + " no puede asociarse a un turista");
        }
    }
}
