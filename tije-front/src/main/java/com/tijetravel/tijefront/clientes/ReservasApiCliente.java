package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.tijetravel.tijefront.dto.ReservaRespuesta;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
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

    public ReservaRespuesta buscar(Integer codigo) {
        ReservaRespuesta reserva = conexion.getCliente().get().uri("/api/v1/reservas/{codigo}", codigo)
                .retrieve().body(ReservaRespuesta.class);
        if (reserva == null) throw new RestClientException("La API no devolvio la reserva.");
        return reserva;
    }

    public void crear(GuardarReservaFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/reservas")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
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
