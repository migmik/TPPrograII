package com.tijetravel.tijefront.clientes;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;

@Service
public class AutenticacionApiCliente {
    private final ConexionApiSesion conexion;

    public AutenticacionApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public SesionRespuesta iniciarSesion(String nombreUsuario, String contrasenia) {
        conexion.limpiarCookies();
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        SesionRespuesta sesion = conexion.getCliente().post().uri("/api/v1/autenticacion/login")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(Map.of("nombreUsuario", nombreUsuario, "contrasenia", contrasenia))
                .retrieve().body(SesionRespuesta.class);
        if (sesion == null) {
            throw new RestClientException("La API no devolvió la sesión.");
        }
        return sesion;
    }

    public SesionRespuesta obtenerSesion() {
        SesionRespuesta sesion = conexion.getCliente().get().uri("/api/v1/autenticacion/sesion")
                .retrieve().body(SesionRespuesta.class);
        if (sesion == null) {
            throw new RestClientException("La API no devolvió la sesión.");
        }
        return sesion;
    }

    public void cerrarSesion() {
        try {
            // Pedimos un token nuevo porque el backend lo renueva al autenticar.
            CsrfRespuesta csrf = conexion.obtenerCsrf();
            conexion.getCliente().post().uri("/api/v1/autenticacion/logout")
                    .header(csrf.getNombreEncabezado(), csrf.getToken())
                    .retrieve().toBodilessEntity();
        } finally {
            conexion.limpiarCookies();
        }
    }

}
