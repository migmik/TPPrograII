package tijetravel.modelos;

import java.time.LocalDateTime;

public class Vuelo {
    private int numero;
    private LocalDateTime fechaYHora;
    private String origen;
    private String destino;
    private int totalPlazas;
    private int plazasTurista;

    public Vuelo(int numero, LocalDateTime fechaYHora, String origen, String destino, int totalPlazas,
            int plazasTurista) {
        this.numero = numero;
        this.fechaYHora = fechaYHora;
        this.origen = origen;
        this.destino = destino;
        this.totalPlazas = totalPlazas;
        this.plazasTurista = plazasTurista;
    }

    public boolean actualizarDatos(LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista) {
        if (fechaYHora == null || origen == null || destino == null || totalPlazas <= 0
                || plazasTurista < 0 || plazasTurista > totalPlazas) return false;
        this.fechaYHora = fechaYHora; this.origen = origen; this.destino = destino;
        this.totalPlazas = totalPlazas; this.plazasTurista = plazasTurista;
        return true;
    }

    public int getNumero() {
        return numero;
    }

    private void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    private void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public String getOrigen() {
        return origen;
    }

    private void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    private void setDestino(String destino) {
        this.destino = destino;
    }

    public int getTotalPlazas() {
        return totalPlazas;
    }

    private void setTotalPlazas(int totalPlazas) {
        this.totalPlazas = totalPlazas;
    }

    public int getPlazasTurista() {
        return plazasTurista;
    }

    private void setPlazasTurista(int plazasTurista) {
        this.plazasTurista = plazasTurista;
    }

}
