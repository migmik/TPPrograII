package com.tijetravel.tijeback.servicios.usuarios;

import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

/** Estrategia para crear un tipo de usuario. Hay una implementacion por cada TipoUsuario. */
public interface CreadorUsuario {
    TipoUsuario tipo();

    Usuario crear(String nombre, String hash, Rol rol, Turista turista, String dni);
}
