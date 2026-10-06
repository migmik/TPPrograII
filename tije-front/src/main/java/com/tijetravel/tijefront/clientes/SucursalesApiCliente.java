package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.tijetravel.tijefront.dto.SucursalResumen;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.GuardarSucursalFormulario;

@Service
public class SucursalesApiCliente {
    private final ConexionApiSesion conexion;

    public SucursalesApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<SucursalResumen> listar() {
        SucursalResumen[] sucursales = conexion.getCliente().get().uri("/api/v1/sucursales")
                .retrieve().body(SucursalResumen[].class);
        if (sucursales == null) {
            throw new RestClientException("La API no devolvió sucursales.");
        }
        return Arrays.asList(sucursales);
    }

    public SucursalResumen buscar(Integer codigo) {
        SucursalResumen sucursal = conexion.getCliente().get()
                .uri("/api/v1/sucursales/{codigo}", codigo)
                .retrieve().body(SucursalResumen.class);
        if (sucursal == null) {
            throw new RestClientException("La API no devolvió la sucursal.");
        }
        return sucursal;
    }

    public void crear(GuardarSucursalFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/sucursales")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void modificar(Integer codigo, GuardarSucursalFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/sucursales/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/sucursales/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }
}
