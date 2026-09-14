package com.tijetravel.tijeback.modelos;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VENDEDOR")
public class Vendedor extends Usuario {

    protected Vendedor() {
    }

    public Vendedor(String nombreUsuario, String contrasenia) {
        super(nombreUsuario, contrasenia, RolUsuario.VENDEDOR);
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso == Permiso.CONSULTAR
                || permiso == Permiso.ADMINISTRAR_TURISTAS
                || permiso == Permiso.ADMINISTRAR_RESERVAS;
    }
}
