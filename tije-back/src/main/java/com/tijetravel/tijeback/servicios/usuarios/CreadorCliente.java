package com.tijetravel.tijeback.servicios.usuarios;

import org.springframework.stereotype.Component;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class CreadorCliente implements CreadorUsuario {
    @Override
    public RolUsuario rol() {
        return RolUsuario.CLIENTE;
    }

    @Override
    public Usuario crear(String nombre, String hash, Turista turista) {
        return new Cliente(nombre, hash, turista);
    }
}
