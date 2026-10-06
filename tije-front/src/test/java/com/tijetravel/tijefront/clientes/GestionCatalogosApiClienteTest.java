package com.tijetravel.tijefront.clientes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.GuardarHotelFormulario;
import com.tijetravel.tijefront.formularios.GuardarVueloFormulario;
import com.tijetravel.tijefront.formularios.GuardarSucursalFormulario;

class GestionCatalogosApiClienteTest {
    @Test
    void sucursalEnviaDireccionTelefonoYCsrf() {
        RestClient.Builder constructor = RestClient.builder().baseUrl("http://api.test");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(constructor).build();
        SucursalesApiCliente cliente = new SucursalesApiCliente(conexion(constructor.build()));
        GuardarSucursalFormulario sucursal = new GuardarSucursalFormulario();
        sucursal.setDireccion("Calle 1");
        sucursal.setTelefono("123");
        servidor.expect(once(), requestTo("http://api.test/api/v1/sucursales"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("X-CSRF-TOKEN", "token"))
                .andExpect(jsonPath("$.direccion").value("Calle 1"))
                .andExpect(jsonPath("$.telefono").value("123"))
                .andRespond(withSuccess());
        cliente.crear(sucursal);
        servidor.verify();
    }

    @Test
    void hotelEnviaFormularioYCsrfPorSesion() {
        RestClient.Builder constructor = RestClient.builder().baseUrl("http://api.test");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(constructor).build();
        ConexionApiSesion conexion = conexion(constructor.build());
        HotelesGestionApiCliente cliente = new HotelesGestionApiCliente(conexion);
        GuardarHotelFormulario hotel = new GuardarHotelFormulario();
        hotel.setNombre("Hotel A"); hotel.setDireccion("Calle 1"); hotel.setCiudad("Córdoba");
        hotel.setTelefono("123"); hotel.setCapacidadTotal(0);
        servidor.expect(once(), requestTo("http://api.test/api/v1/hoteles"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("X-CSRF-TOKEN", "token"))
                .andExpect(jsonPath("$.capacidadTotal").value(0)).andRespond(withSuccess());
        cliente.crear(hotel);
        servidor.verify();
    }

    @Test
    void edicionDeVueloNoEnviaNumeroEnElCuerpo() {
        RestClient.Builder constructor = RestClient.builder().baseUrl("http://api.test");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(constructor).build();
        ConexionApiSesion conexion = conexion(constructor.build());
        VuelosGestionApiCliente cliente = new VuelosGestionApiCliente(conexion);
        GuardarVueloFormulario vuelo = new GuardarVueloFormulario();
        vuelo.setNumero(20); vuelo.setFechaYHora(LocalDateTime.of(2027, 3, 10, 14, 30));
        vuelo.setOrigen("Buenos Aires"); vuelo.setDestino("Córdoba");
        vuelo.setTotalPlazas(10); vuelo.setPlazasTurista(8); vuelo.setPlazasPrimera(2);
        servidor.expect(once(), requestTo("http://api.test/api/v1/vuelos/20"))
                .andExpect(method(HttpMethod.PUT)).andExpect(header("X-CSRF-TOKEN", "token"))
                .andExpect(jsonPath("$.numero").doesNotExist())
                .andExpect(jsonPath("$.fechaYHora").value("2027-03-10T14:30:00"))
                .andRespond(withSuccess());
        cliente.modificar(20, vuelo);
        servidor.verify();
    }

    private ConexionApiSesion conexion(RestClient restClient) {
        ConexionApiSesion conexion = mock(ConexionApiSesion.class);
        CsrfRespuesta csrf = new CsrfRespuesta();
        csrf.setNombreEncabezado("X-CSRF-TOKEN");
        csrf.setToken("token");
        when(conexion.getCliente()).thenReturn(restClient);
        when(conexion.obtenerCsrf()).thenReturn(csrf);
        return conexion;
    }
}
