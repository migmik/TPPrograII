package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.tijetravel.tijefront.dto.SucursalResumen;

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
}
