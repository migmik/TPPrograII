package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.tijetravel.tijefront.dto.ReservaRespuesta;
import com.tijetravel.tijefront.dto.PaginaReservasRespuesta;
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

    public PaginaReservasRespuesta listarPagina(
            Integer codigoTurista,
            Integer numeroVuelo,
            Integer codigoHotel,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            int pagina,
            int tamanio) {
        PaginaReservasRespuesta resultado = conexion.getCliente().get().uri(uriBuilder -> {
            var ruta = uriBuilder.path("/api/v1/reservas/pagina")
                    .queryParam("pagina", pagina)
                    .queryParam("tamanio", tamanio);
            if (codigoTurista != null) ruta.queryParam("codigoTurista", codigoTurista);
            if (numeroVuelo != null) ruta.queryParam("numeroVuelo", numeroVuelo);
            if (codigoHotel != null) ruta.queryParam("codigoHotel", codigoHotel);
            if (fechaDesde != null) ruta.queryParam("fechaDesde", fechaDesde);
            if (fechaHasta != null) ruta.queryParam("fechaHasta", fechaHasta);
            return ruta.build();
        }).retrieve().body(PaginaReservasRespuesta.class);
        if (resultado == null) throw new RestClientException("La API no devolvio la pagina de reservas.");
        return resultado;
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
