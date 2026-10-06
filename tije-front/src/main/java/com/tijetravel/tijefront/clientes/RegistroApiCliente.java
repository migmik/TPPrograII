package com.tijetravel.tijefront.clientes;

import org.springframework.stereotype.Service;

import com.tijetravel.tijefront.formularios.RegistroClienteFormulario;

@Service
public class RegistroApiCliente {
    private final ConexionApiSesion conexion;

    public RegistroApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public void registrar(RegistroClienteFormulario formulario) {
        var csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/autenticacion/registro")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario)
                .retrieve().toBodilessEntity();
    }
}