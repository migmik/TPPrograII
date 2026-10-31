package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.RegistroDadoDeBaja;

/** Consulta y reactiva registros dados de baja de cualquier modulo de la API. */
@Service
public class BajasApiCliente {
    private final ConexionApiSesion conexion;

    public BajasApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<RegistroDadoDeBaja> listar(String recurso) {
        RegistroDadoDeBaja[] registros = conexion.getCliente().get().uri("/api/v1/{recurso}/bajas", recurso)
                .retrieve().body(RegistroDadoDeBaja[].class);
        if (registros == null) {
            throw new RestClientException("La API no devolvió los registros dados de baja.");
        }
        return Arrays.asList(registros);
    }

    public void reactivar(String recurso, Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/{recurso}/{codigo}/reactivar", recurso, codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }
}
