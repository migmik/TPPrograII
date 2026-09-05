package com.tijetravel.tijeback.modelos;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turista_codigo", unique = true)
    private Turista turista;

    protected Cliente() {
    }

    public Cliente(String nombreUsuario, String contrasenia, Turista turista) {
        super(nombreUsuario, contrasenia, RolUsuario.CLIENTE);
        if (!ValidacionModelo.obligatorio(turista, "turista").isTitular()) {
            throw new IllegalArgumentException("Un cliente solo puede asociarse a un turista titular");
        }
        this.turista = turista;
    }

    @Override
    public Turista getTurista() {
        return turista;
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso == Permiso.CONSULTAR;
    }
}
