package com.tijetravel.tijeback.modelos;

import com.tijetravel.tijeback.enums.TipoUsuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Persona que trabaja en la agencia (administrador, vendedor, hotelero, etc.).
 * Lo que puede hacer depende del rol que tenga asignado.
 */
@Entity
@DiscriminatorValue("EMPLEADO")
public class Empleado extends Usuario {

    protected Empleado() {
    }

    public Empleado(String nombreUsuario, String contrasenia, String dni, Rol rol) {
        super(nombreUsuario, contrasenia, rol);
        actualizarDni(dni);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.EMPLEADO;
    }
}
