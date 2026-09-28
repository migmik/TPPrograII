package com.tijetravel.tijefront.clientes;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.annotation.SessionScope;

import com.tijetravel.tijefront.dto.CsrfRespuesta;

import jakarta.annotation.PreDestroy;

@Service
@SessionScope
public class ConexionApiSesion {
    // Login y operaciones privadas comparten estas cookies dentro de una sola
    // sesión.
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private final HttpClient conexion;
    private final RestClient cliente;

    public ConexionApiSesion(@Value("${app.backend.url}") String urlBackend) {
        conexion = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory solicitudes = new JdkClientHttpRequestFactory(conexion);
        solicitudes.setReadTimeout(5000);
        cliente = RestClient.builder().baseUrl(urlBackend).requestFactory(solicitudes).build();
    }

    public RestClient getCliente() {
        return cliente;
    }

    public CsrfRespuesta obtenerCsrf() {
        CsrfRespuesta csrf = cliente.get().uri("/api/v1/autenticacion/csrf")
                .retrieve().body(CsrfRespuesta.class);
        if (csrf == null || csrf.getToken() == null || csrf.getNombreEncabezado() == null) {
            throw new RestClientException("La API no devolvió el token del formulario.");
        }
        return csrf;
    }

    public void limpiarCookies() {
        cookies.getCookieStore().removeAll();
    }

    @PreDestroy
    public void liberarConexion() {
        limpiarCookies();
        conexion.shutdownNow();
    }
}
