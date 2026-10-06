package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.CrearUsuarioFormulario;

class UsuariosApiClienteTest {
    private MockRestServiceServer servidor;
    private ConexionApiSesion conexion;
    private UsuariosApiCliente usuarios;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        conexion = mock(ConexionApiSesion.class);
        when(conexion.getCliente()).thenReturn(builder.build());
        usuarios = new UsuariosApiCliente(conexion);
    }

    @Test
    void leeUsuariosYSuRelacionConElTurista() {
        servidor.expect(requestTo("http://backend/api/v1/usuarios")).andRespond(withSuccess("""
                [{"codigo":2,"nombreUsuario":"ana","rol":"CLIENTE","codigoTurista":3}]
                """, MediaType.APPLICATION_JSON));
        var respuesta = usuarios.listar().get(0);
        assertEquals("ana", respuesta.getNombreUsuario());
        assertEquals("CLIENTE", respuesta.getRol());
        assertEquals(3, respuesta.getCodigoTurista());
        servidor.verify();
    }

    @Test
    void filtraUsuariosPorRol() {
        servidor.expect(requestTo("http://backend/api/v1/usuarios?rol=VENDEDOR"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"codigo\":2,\"nombreUsuario\":\"ventas\",\"rol\":\"VENDEDOR\"}]",
                        MediaType.APPLICATION_JSON));

        assertEquals("VENDEDOR", usuarios.listar("VENDEDOR").get(0).getRol());
        servidor.verify();
    }

    @Test
    void enviaElFormularioYElTokenCsrf() {
        CsrfRespuesta csrf = new CsrfRespuesta();
        csrf.setNombreEncabezado("X-CSRF-TOKEN");
        csrf.setToken("token-de-prueba");
        when(conexion.obtenerCsrf()).thenReturn(csrf);
        servidor.expect(requestTo("http://backend/api/v1/usuarios")).andExpect(method(HttpMethod.POST))
                .andExpect(header("X-CSRF-TOKEN", "token-de-prueba"))
                .andExpect(content().json("""
                        {"nombreUsuario":"ana","contrasenia":"clave","rol":"CLIENTE","codigoTurista":3}
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));
        CrearUsuarioFormulario formulario = new CrearUsuarioFormulario();
        formulario.setNombreUsuario("ana");
        formulario.setContrasenia("clave");
        formulario.setRol("CLIENTE");
        formulario.setCodigoTurista(3);
        usuarios.crear(formulario);
        servidor.verify();
    }

    @Test
    void noOcultaLaFaltaDePermisos() {
        servidor.expect(requestTo("http://backend/api/v1/usuarios")).andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThrows(HttpClientErrorException.Forbidden.class, () -> usuarios.listar());
        servidor.verify();
    }

    @Test
    void elSelectorSoloNecesitaCodigoNombreYApellidoDelTurista() {
        servidor.expect(requestTo("http://backend/api/v1/turistas")).andRespond(withSuccess("""
                [{"codigo":3,"nombre":"Ana","apellido":"Pérez","direccion":"Calle 1",
                  "email":"ana@example.test","codigoSucursal":1,"titular":true}]
                """, MediaType.APPLICATION_JSON));
        var turistas = new TuristasApiCliente(conexion).listar();
        assertEquals(3, turistas.get(0).getCodigo());
        assertEquals("Pérez", turistas.get(0).getApellido());
        servidor.verify();
    }
}
