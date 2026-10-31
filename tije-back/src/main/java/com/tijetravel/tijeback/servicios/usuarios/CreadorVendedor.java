package com.tijetravel.tijeback.servicios.usuarios;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;

@Component
public class CreadorVendedor implements CreadorUsuario {
    @Override
    public RolUsuario rol() {
        return RolUsuario.VENDEDOR;
    }

    @Override
    public Usuario crear(String nombre, String hash, Turista turista, String dni) {
        if (turista != null) {
            throw new IllegalArgumentException("Este rol no admite un turista asociado");
        }
        return new Vendedor(nombre, hash, dni);
    }
}
