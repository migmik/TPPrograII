package com.tijetravel.tijeback.servicios.usuarios;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class CreadorEmpleado implements CreadorUsuario {
    @Override
    public TipoUsuario tipo() {
        return TipoUsuario.EMPLEADO;
    }

    @Override
    public Usuario crear(String nombre, String hash, Rol rol, Turista turista, String dni) {
        if (turista != null) {
            throw new IllegalArgumentException("Un empleado no se asocia a un turista");
        }
        return new Empleado(nombre, hash, dni, rol);
    }
}
