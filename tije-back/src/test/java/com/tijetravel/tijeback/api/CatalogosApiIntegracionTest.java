package com.tijetravel.tijeback.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=${TEST_DB_URL_API:jdbc:h2:mem:tijetravel-api;MODE=MySQL;DB_CLOSE_DELAY=-1}")
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "dev" })
class CatalogosApiIntegracionTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaSucursalesComoDto() throws Exception {
        mockMvc.perform(get("/api/v1/sucursales"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value(1))
                .andExpect(jsonPath("$[0].direccion").value("Av Corrientes 1234"))
                .andExpect(jsonPath("$[0].telefono").value("011-4321-1000"));
    }

    @Test
    void encuentraHotelPorCodigo() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/{codigo}", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(4))
                .andExpect(jsonPath("$.nombre").value("Hotel Taragui"))
                .andExpect(jsonPath("$.ciudad").value("Goya"))
                .andExpect(jsonPath("$.capacidadTotal").value(10))
                .andExpect(jsonPath("$.plazasDisponibles").doesNotExist());
    }

    @Test
    void listaVuelosComoDto() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].numero").value(100))
                .andExpect(jsonPath("$[0].fechaYHora").value("2026-08-15T10:30:00"))
                .andExpect(jsonPath("$[0].origen").value("Buenos Aires"))
                .andExpect(jsonPath("$[0].destino").value("Cordoba"));
    }

    @Test
    void consultaDisponibilidadDeVueloPorClase() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/{numero}/disponibilidad", 100)
                .queryParam("clase", "TURISTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroVuelo").value(100))
                .andExpect(jsonPath("$.claseVuelo").value("TURISTA"))
                .andExpect(jsonPath("$.plazasDisponibles").value(79));
    }

    @Test
    void consultaDisponibilidadDeHotelPorRangoDeFechas() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/{codigo}/disponibilidad", 2)
                .queryParam("fechaLlegada", "2026-08-15")
                .queryParam("fechaPartida", "2026-08-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoHotel").value(2))
                .andExpect(jsonPath("$.fechaLlegada").value("2026-08-15"))
                .andExpect(jsonPath("$.fechaPartida").value("2026-08-20"))
                .andExpect(jsonPath("$.plazasDisponibles").value(34));
    }

    @Test
    void rechazaRangoDeDisponibilidadInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/{codigo}/disponibilidad", 2)
                .queryParam("fechaLlegada", "2026-08-20")
                .queryParam("fechaPartida", "2026-08-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ARGUMENTO_INVALIDO"));
    }

    @Test
    void respondeErrorUniformeCuandoNoExisteLaEntidad() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/{codigo}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.fechaHora").exists())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("ENTIDAD_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.mensaje").value("No se encontro el hotel 999"))
                .andExpect(jsonPath("$.ruta").value("/api/v1/hoteles/999"))
                .andExpect(jsonPath("$.detalles").isEmpty());
    }

    @Test
    void rechazaIdentificadoresNoPositivos() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/{numero}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("PARAMETROS_INVALIDOS"))
                .andExpect(jsonPath("$.detalles.numero").value("El numero debe ser positivo"));
    }

    @Test
    void rechazaIdentificadoresConFormatoIncorrecto() throws Exception {
        mockMvc.perform(get("/api/v1/sucursales/{codigo}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void protegeOperacionesDeEscrituraConCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/hoteles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403))
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void exigeAutenticacionParaAccederAUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401))
                .andExpect(jsonPath("$.error").value("AUTENTICACION_REQUERIDA"));
    }
}
