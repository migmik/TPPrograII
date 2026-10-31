package com.tijetravel.tijefront.clientes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.BalanceRespuesta;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.PaginaReservasRespuesta;
import com.tijetravel.tijefront.dto.ReservaRespuesta;
import com.tijetravel.tijefront.formularios.GuardarReservaFormulario;

@Service
public class ReservasApiCliente {
    private final ConexionApiSesion conexion;

    public ReservasApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<ReservaRespuesta> listar() {
        ReservaRespuesta[] reservas = conexion.getCliente().get().uri("/api/v1/reservas")
                .retrieve().body(ReservaRespuesta[].class);
        if (reservas == null) throw new RestClientException("La API no devolvio las reservas.");
        return Arrays.asList(reservas);
    }

    public PaginaReservasRespuesta listarPagina(
            Integer codigoTurista,
            String dni,
            Integer numeroVuelo,
            Integer codigoHotel,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Boolean pagada,
            int pagina,
            int tamanio) {
        RutaConsulta ruta = new RutaConsulta("/api/v1/reservas/pagina")
                .parametro("pagina", pagina)
                .parametro("tamanio", tamanio)
                .parametro("codigoTurista", codigoTurista)
                .parametro("dni", dni)
                .parametro("numeroVuelo", numeroVuelo)
                .parametro("codigoHotel", codigoHotel)
                .parametro("fechaDesde", fechaDesde)
                .parametro("fechaHasta", fechaHasta)
                .parametro("pagada", pagada);
        PaginaReservasRespuesta resultado = conexion.getCliente().get().uri(ruta.getPlantilla(), ruta.getValores())
                .retrieve().body(PaginaReservasRespuesta.class);
        if (resultado == null) throw new RestClientException("La API no devolvio la pagina de reservas.");
        return resultado;
    }

    public ReservaRespuesta buscar(Integer codigo) {
        ReservaRespuesta reserva = conexion.getCliente().get().uri("/api/v1/reservas/{codigo}", codigo)
                .retrieve().body(ReservaRespuesta.class);
        if (reserva == null) throw new RestClientException("La API no devolvio la reserva.");
        return reserva;
    }

    public ReservaRespuesta crear(GuardarReservaFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        ReservaRespuesta reserva = conexion.getCliente().post().uri("/api/v1/reservas")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().body(ReservaRespuesta.class);
        if (reserva == null) throw new RestClientException("La API no devolvio la reserva creada.");
        return reserva;
    }

    public ReservaRespuesta pagar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        ReservaRespuesta reserva = conexion.getCliente().post().uri("/api/v1/reservas/{codigo}/pagar", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().body(ReservaRespuesta.class);
        if (reserva == null) throw new RestClientException("La API no confirmo el pago.");
        return reserva;
    }

    public BalanceRespuesta balance() {
        BalanceRespuesta balance = conexion.getCliente().get().uri("/api/v1/reservas/balance")
                .retrieve().body(BalanceRespuesta.class);
        if (balance == null) throw new RestClientException("La API no devolvio el balance.");
        return balance;
    }

    public void modificar(Integer codigo, GuardarReservaFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/reservas/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/reservas/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }
}
