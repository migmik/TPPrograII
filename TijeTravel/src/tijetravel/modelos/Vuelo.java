package tijetravel.modelos;

import java.time.LocalDateTime;

public class Vuelo {
    private int numero;
    private LocalDateTime fechaYHora;
    private String origen;
    private String destino;
    private int totalPlazas;
    private int plazasTurista;
    private int plazasPrimera;

    public Vuelo(int numero, LocalDateTime fechaYHora, String origen, String destino, int totalPlazas,
            int plazasTurista, int plazasPrimera) {
        this.numero = numero;
        this.fechaYHora = fechaYHora;
        this.origen = origen;
        this.destino = destino;
        this.totalPlazas = totalPlazas;
        this.plazasTurista = plazasTurista;
        this.plazasPrimera = plazasPrimera;
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

    public int getPlazasPrimera() {
        return plazasPrimera;
    }

    public void setPlazasPrimera(int plazasPrimera) {
        this.plazasPrimera = plazasPrimera;
    }

    public boolean tienePlazasDisponibles(ClaseVuelo claseVuelo) {
        if (claseVuelo == ClaseVuelo.TURISTA) {
            return plazasTurista > 0;
        }

        if (claseVuelo == ClaseVuelo.PRIMERA) {
            return plazasPrimera > 0;
        }

        return false;
    }

    public boolean reservarPlaza(ClaseVuelo claseVuelo) {
        if (!tienePlazasDisponibles(claseVuelo)) {
            return false;
        }

        if (claseVuelo == ClaseVuelo.TURISTA) {
            plazasTurista--;
        } else {
            plazasPrimera--;
        }

        return true;
    }

    public void liberarPlaza(ClaseVuelo claseVuelo) {
        if (claseVuelo == ClaseVuelo.TURISTA) {
            plazasTurista++;
        } else if (claseVuelo == ClaseVuelo.PRIMERA) {
            plazasPrimera++;
        }
    }
}
