package com.tijetravel.tijefront.clientes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.RegistroClienteFormulario;

class RegistroApiClienteTest {
    private RegistroApiCliente cliente;
    private ConexionApiSesion conexion;
    private MockRestServiceServer servidor;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        conexion = mock(ConexionApiSesion.class);
        when(conexion.getCliente()).thenReturn(builder.build());
        CsrfRespuesta csrf = new CsrfRespuesta();
        csrf.setNombreEncabezado("X-CSRF-TOKEN");
        csrf.setToken("token-registro");
        when(conexion.obtenerCsrf()).thenReturn(csrf);
        cliente = new RegistroApiCliente(conexion);
    }

    @Test
    void enviaRegistroDeClienteConCsrfSinPermitirElegirRol() {
        RegistroClienteFormulario formulario = new RegistroClienteFormulario();
        formulario.setNombreUsuario("ana");
        formulario.setContrasenia("clave-segura");
        formulario.setDni("12345678");
        formulario.setNombre("Ana");
        formulario.setApellido("Perez");
        formulario.setDireccion("Calle 1");
        formulario.setEmail("ana@example.com");
        formulario.setTelefonoFijo("111");
        formulario.setTelefonoCelular("222");
        formulario.setCodigoSucursal(1);

        servidor.expect(requestTo("http://backend/api/v1/autenticacion/registro"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-CSRF-TOKEN", "token-registro"))
                .andExpect(content().json("""
                        {"nombreUsuario":"ana","contrasenia":"clave-segura","dni":"12345678",
                         "nombre":"Ana","apellido":"Perez","direccion":"Calle 1",
                         "email":"ana@example.com","telefonoFijo":"111","telefonoCelular":"222",
                         "codigoSucursal":1}
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));

        cliente.registrar(formulario);
        servidor.verify();
    }
}