package com.tijetravel.tijeback.modelos;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * Datos comunes a todas las entidades: cuando se crearon y, si se dieron de
 * baja, cuando. Las bajas son logicas: la fila queda en la base para no perder
 * el historial (por ejemplo, las reservas de un hotel que ya no se ofrece).
 */
@MappedSuperclass
public abstract class EntidadBase {
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    /** Fecha que se usa para las bajas. Se guarda sin fracciones de segundo para poder compararla. */
    public static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @PrePersist
    protected void registrarCreacion() {
        if (fechaCreacion == null) {
            fechaCreacion = ahora();
        }
    }

    public void darDeBaja(LocalDateTime fecha) {
        if (fechaBaja != null) {
            throw new IllegalStateException("El registro ya esta dado de baja");
        }
        fechaBaja = ValidacionModelo.obligatorio(fecha, "fechaBaja");
    }

    public void reactivar() {
        if (fechaBaja == null) {
            throw new IllegalStateException("El registro ya esta activo");
        }
        fechaBaja = null;
    }

    public boolean estaActivo() {
        return fechaBaja == null;
    }

    /** Indica si este registro se dio de baja junto con otro (misma fecha de baja). */
    public boolean seDioDeBajaJuntoCon(EntidadBase otra) {
        return fechaBaja != null && fechaBaja.equals(otra.fechaBaja);
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }
}
