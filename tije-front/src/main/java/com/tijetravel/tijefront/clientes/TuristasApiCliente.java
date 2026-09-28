package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.dto.TuristaRespuesta;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.GuardarTuristaFormulario;

@Service
public class TuristasApiCliente {
    private final ConexionApiSesion conexion;

    public TuristasApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<TuristaRespuesta> listarDetalles() {
        TuristaRespuesta[] turistas = conexion.getCliente().get().uri("/api/v1/turistas")
                .retrieve().body(TuristaRespuesta[].class);
        if (turistas == null) {
            throw new RestClientException("La API no devolvió turistas.");
        }
        return Arrays.asList(turistas);
    }

    public TuristaRespuesta buscar(Integer codigo) {
        TuristaRespuesta turista = conexion.getCliente().get().uri("/api/v1/turistas/{codigo}", codigo)
                .retrieve().body(TuristaRespuesta.class);
        if (turista == null) {
            throw new RestClientException("La API no devolvió el turista.");
        }
        return turista;
    }

    public void crear(GuardarTuristaFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/turistas")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void modificar(Integer codigo, GuardarTuristaFormulario formulario) {
        // La API de modificación no permite cambiar el titular.
        java.util.Map<String, Object> datos = new java.util.LinkedHashMap<>();
        datos.put("nombre", formulario.getNombre());
        datos.put("apellido", formulario.getApellido());
        datos.put("direccion", formulario.getDireccion());
        datos.put("email", formulario.getEmail());
        datos.put("telefonoFijo", formulario.getTelefonoFijo());
        datos.put("telefonoCelular", formulario.getTelefonoCelular());
        datos.put("codigoSucursal", formulario.getCodigoSucursal());
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/turistas/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(datos).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/turistas/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }

    public List<TuristaResumen> listar() {
        TuristaResumen[] turistas = conexion.getCliente().get().uri("/api/v1/turistas")
                .retrieve().body(TuristaResumen[].class);
        if (turistas == null) {
            throw new RestClientException("La API no devolvió el listado de turistas.");
        }
        return Arrays.asList(turistas);
    }
}
