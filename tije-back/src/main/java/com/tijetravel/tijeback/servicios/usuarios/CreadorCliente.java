package com.tijetravel.tijeback.servicios.usuarios;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class CreadorCliente implements CreadorUsuario {
    @Override
    public TipoUsuario tipo() {
        return TipoUsuario.CLIENTE;
    }

    @Override
    public Usuario crear(String nombre, String hash, Rol rol, Turista turista, String dni) {
        return new Cliente(nombre, hash, turista, rol);
    }
}
