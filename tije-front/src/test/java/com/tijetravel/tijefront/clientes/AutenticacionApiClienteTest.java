package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class AutenticacionApiClienteTest {
    private HttpServer servidor;
    private AutenticacionApiCliente ana;
    private AutenticacionApiCliente beto;
    private ConexionApiSesion conexionAna;
    private ConexionApiSesion conexionBeto;
    private final Map<String, String> usuarios = new HashMap<>();
    private final AtomicInteger ids = new AtomicInteger();
    private int salidas;

    @BeforeEach
    void preparar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        HttpHandler manejador = new HttpHandler() {
            @Override
            public void handle(HttpExchange intercambio) throws IOException {
                responder(intercambio);
            }
        };
        servidor.createContext("/api/v1/autenticacion", manejador);
        servidor.createContext("/api/v1/usuarios", manejador);
        servidor.start();
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
        conexionAna = new ConexionApiSesion(url);
        conexionBeto = new ConexionApiSesion(url);
        ana = new AutenticacionApiCliente(conexionAna);
        beto = new AutenticacionApiCliente(conexionBeto);
    }

    @AfterEach
    void detener() {
        conexionAna.liberarConexion();
        conexionBeto.liberarConexion();
        servidor.stop(0);
    }

    @Test
    void conservaLaCookieRotadaAlIngresar() {
        assertEquals("ana", ana.iniciarSesion("ana", "clave").getNombreUsuario());
        assertEquals("ana", ana.obtenerSesion().getNombreUsuario());
    }

    @Test
    void dosClientesNoCompartenCookiesYSalirNoDesconectaAlOtro() {
        ana.iniciarSesion("ana", "clave");
        beto.iniciarSesion("beto", "clave");
        assertEquals("ana", ana.obtenerSesion().getNombreUsuario());
        assertEquals("beto", beto.obtenerSesion().getNombreUsuario());
        ana.cerrarSesion();
        try {
            ana.obtenerSesion();
            fail("Se esperaba HttpClientErrorException.Unauthorized");
        } catch (HttpClientErrorException.Unauthorized esperada) {
            // Es la excepcion que se esperaba.
        }
        assertEquals("beto", beto.obtenerSesion().getNombreUsuario());
        assertEquals(1, salidas);
    }

    @Test
    void salirUsaElTokenNuevoDeLaSesionAutenticada() {
        ana.iniciarSesion("ana", "clave");
        ana.cerrarSesion();
        assertEquals(1, salidas);
        assertEquals(0, usuarios.size());
    }

    @Test
    void elClienteDeUsuariosReutilizaLaSesionDelLogin() {
        ana.iniciarSesion("ana", "clave");
        assertEquals("ana", new UsuariosApiCliente(conexionAna).listar().get(0).getNombreUsuario());
        try {
            new UsuariosApiCliente(conexionBeto).listar();
            fail("Se esperaba HttpClientErrorException.Unauthorized");
        } catch (HttpClientErrorException.Unauthorized esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @Test
    void conservaEl401DeCredencialesIncorrectas() {
        try {
            ana.iniciarSesion("ana", "incorrecta");
            fail("Se esperaba HttpClientErrorException.Unauthorized");
        } catch (HttpClientErrorException.Unauthorized esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            ana.obtenerSesion();
            fail("Se esperaba HttpClientErrorException.Unauthorized");
        } catch (HttpClientErrorException.Unauthorized esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    private void responder(HttpExchange intercambio) throws IOException {
        String cookie = intercambio.getRequestHeaders().getFirst("Cookie");
        String id = cookie == null ? "anonima" : cookie.replace("TIJESESSION=", "");
        String ruta = intercambio.getRequestURI().getPath();
        if (ruta.endsWith("/csrf")) {
            if (cookie == null)
                intercambio.getResponseHeaders().add("Set-Cookie", "TIJESESSION=anonima; Path=/; HttpOnly");
            enviar(intercambio, 200,
                    "{\"nombreEncabezado\":\"X-CSRF-TOKEN\",\"nombreParametro\":\"_csrf\",\"token\":\"" + id + "\"}");
        } else if (ruta.endsWith("/login")) {
            String cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!id.equals(intercambio.getRequestHeaders().getFirst("X-CSRF-TOKEN"))) {
                enviar(intercambio, 403, "{}");
            } else if (cuerpo.contains("incorrecta")) {
                enviar(intercambio, 401, "{}");
            } else {
                String nombre = cuerpo.contains("beto") ? "beto" : "ana";
                String nuevoId = "autenticada" + ids.incrementAndGet();
                usuarios.put(nuevoId, nombre);
                intercambio.getResponseHeaders().add("Set-Cookie", "TIJESESSION=" + nuevoId + "; Path=/; HttpOnly");
                enviar(intercambio, 200, sesion(nombre));
            }
        } else if (ruta.endsWith("/logout")) {
            if (!id.equals(intercambio.getRequestHeaders().getFirst("X-CSRF-TOKEN"))) {
                enviar(intercambio, 403, "{}");
            } else {
                usuarios.remove(id);
                salidas++;
                enviar(intercambio, 204, "");
            }
        } else if (usuarios.containsKey(id)) {
            String datos = sesion(usuarios.get(id));
            enviar(intercambio, 200, ruta.endsWith("/usuarios") ? "[" + datos + "]" : datos);
        } else {
            enviar(intercambio, 401, "{}");
        }
    }

    private String sesion(String nombre) {
        return "{\"codigo\":1,\"nombreUsuario\":\"" + nombre + "\",\"rol\":\"VENDEDOR\",\"codigoTurista\":null}";
    }

    private void enviar(HttpExchange intercambio, int estado, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(estado, estado == 204 ? -1 : bytes.length);
        if (estado != 204)
            intercambio.getResponseBody().write(bytes);
        intercambio.close();
    }
}
