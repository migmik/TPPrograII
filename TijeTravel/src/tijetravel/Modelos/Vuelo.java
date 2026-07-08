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

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public int getTotalPlazas() {
        return totalPlazas;
    }

    public void setTotalPlazas(int totalPlazas) {
        this.totalPlazas = totalPlazas;
    }

    public int getPlazasTurista() {
        return plazasTurista;
    }

    public void setPlazasTurista(int plazasTurista) {
        this.plazasTurista = plazasTurista;
    }

}
