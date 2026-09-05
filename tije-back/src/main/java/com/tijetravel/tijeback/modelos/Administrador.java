package com.tijetravel.tijeback.modelos;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    protected Administrador() {
    }

    public Administrador(String nombreUsuario, String contrasenia) {
        super(nombreUsuario, contrasenia, RolUsuario.ADMINISTRADOR);
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso != null;
    }
}
