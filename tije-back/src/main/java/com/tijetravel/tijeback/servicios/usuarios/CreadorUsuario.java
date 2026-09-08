package com.tijetravel.tijeback.servicios.usuarios;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

public interface CreadorUsuario {
    RolUsuario rol();
    Usuario crear(String nombre, String hash, Turista turista);
}
