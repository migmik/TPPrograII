package com.tijetravel.tijefront.clientes;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.annotation.SessionScope;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;

import jakarta.annotation.PreDestroy;

@Service
@SessionScope
public class AutenticacionApiCliente {
    // Cada sesión del navegador tiene su propio cliente y sus propias cookies de la
    // API.
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private final HttpClient conexion;
    private final RestClient cliente;

    public AutenticacionApiCliente(@Value("${app.backend.url}") String urlBackend) {
        conexion = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory solicitudes = new JdkClientHttpRequestFactory(conexion);
        solicitudes.setReadTimeout(5000);
        cliente = RestClient.builder().baseUrl(urlBackend).requestFactory(solicitudes).build();
    }

    public SesionRespuesta iniciarSesion(String nombreUsuario, String contrasenia) {
        cookies.getCookieStore().removeAll();
        CsrfRespuesta csrf = obtenerCsrf();
        SesionRespuesta sesion = cliente.post().uri("/api/v1/autenticacion/login")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(Map.of("nombreUsuario", nombreUsuario, "contrasenia", contrasenia))
                .retrieve().body(SesionRespuesta.class);
        if (sesion == null) {
            throw new RestClientException("La API no devolvió la sesión.");
        }
        return sesion;
    }

    public SesionRespuesta obtenerSesion() {
        SesionRespuesta sesion = cliente.get().uri("/api/v1/autenticacion/sesion")
                .retrieve().body(SesionRespuesta.class);
        if (sesion == null) {
            throw new RestClientException("La API no devolvió la sesión.");
        }
        return sesion;
    }

    public void cerrarSesion() {
        try {
            // Pedimos un token nuevo porque el backend lo renueva al autenticar.
            CsrfRespuesta csrf = obtenerCsrf();
            cliente.post().uri("/api/v1/autenticacion/logout")
                    .header(csrf.getNombreEncabezado(), csrf.getToken())
                    .retrieve().toBodilessEntity();
        } finally {
            cookies.getCookieStore().removeAll();
        }
    }

    private CsrfRespuesta obtenerCsrf() {
        CsrfRespuesta csrf = cliente.get().uri("/api/v1/autenticacion/csrf")
                .retrieve().body(CsrfRespuesta.class);
        if (csrf == null || csrf.getToken() == null || csrf.getNombreEncabezado() == null) {
            throw new RestClientException("La API no devolvió el token del formulario.");
        }
        return csrf;
    }

    @PreDestroy
    public void liberarConexion() {
        cookies.getCookieStore().removeAll();
        conexion.shutdownNow();
    }
}
