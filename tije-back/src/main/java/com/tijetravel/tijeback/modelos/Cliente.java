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
    public String getDni() {
        if (turista == null) {
            return null;
        }
        return turista.getDni();
    }

    @Override
    public boolean tieneDniPropio() {
        return false;
    }

    /** El cliente solo ve a su turista titular y a los familiares de ese titular. */
    @Override
    public boolean puedeVerTurista(Turista otro) {
        Integer codigoTitular = turista.getCodigo();
        return codigoTitular.equals(otro.getCodigo()) || codigoTitular.equals(otro.getCodigoTitular());
    }

    @Override
    public Integer getCodigoGrupoFamiliar() {
        return turista.getCodigo();
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        return permiso == Permiso.CONSULTAR || permiso == Permiso.CREAR_RESERVAS;
    }
}
