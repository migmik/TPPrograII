package com.tijetravel.tijefront.clientes;

import org.springframework.stereotype.Service;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.GuardarVueloFormulario;

@Service
public class VuelosGestionApiCliente {
    private final ConexionApiSesion conexion;

    public VuelosGestionApiCliente(ConexionApiSesion conexion) { this.conexion = conexion; }

    public void crear(GuardarVueloFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/vuelos")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void modificar(Integer numero, GuardarVueloFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/vuelos/{numero}", numero)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(new CambioVuelo(formulario)).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer numero) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/vuelos/{numero}", numero)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }

    private record CambioVuelo(java.time.LocalDateTime fechaYHora, String origen, String destino,
            Integer totalPlazas, Integer plazasTurista, Integer plazasPrimera) {
        private CambioVuelo(GuardarVueloFormulario formulario) {
            this(formulario.getFechaYHora(), formulario.getOrigen(), formulario.getDestino(),
                    formulario.getTotalPlazas(), formulario.getPlazasTurista(), formulario.getPlazasPrimera());
        }
    }
}
