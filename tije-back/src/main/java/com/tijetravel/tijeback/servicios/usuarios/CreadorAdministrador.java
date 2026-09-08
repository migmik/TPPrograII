package com.tijetravel.tijeback.servicios.usuarios;

import org.springframework.stereotype.Component;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class CreadorAdministrador implements CreadorUsuario {
    @Override
    public RolUsuario rol() { return RolUsuario.ADMINISTRADOR; }

    @Override
    public Usuario crear(String nombre, String hash, Turista turista) {
        if (turista != null) {
            throw new IllegalArgumentException("Este rol no admite un turista asociado");
        }
        return new Administrador(nombre, hash);
    }
}
