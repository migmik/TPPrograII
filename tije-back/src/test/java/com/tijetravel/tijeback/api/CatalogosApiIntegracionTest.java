package com.tijetravel.tijeback.api;

import static org.hamcrest.Matchers.hasItems;
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
    void entregaOpcionesDeFiltrosSinCargarLosCatalogosCompletos() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/origenes"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(hasItems("Buenos Aires")));
        mockMvc.perform(get("/api/v1/vuelos/destinos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(hasItems("Cordoba")));
        mockMvc.perform(get("/api/v1/hoteles/ciudades"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(hasItems("Goya")));
        mockMvc.perform(get("/api/v1/hoteles/nombres"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(hasItems("Hotel Taragui")));
        mockMvc.perform(get("/api/v1/sucursales/ciudades"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

        @Test
        void listaCiudadesDistintasDeHotelesYVuelos() throws Exception {
                mockMvc.perform(get("/api/v1/ciudades"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").value(hasItems("Goya", "Buenos Aires")));
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
    void paginaVuelosFiltradosComoDto() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/pagina").queryParam("origen", "Buenos Aires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elementos[0].numero").value(100))
                .andExpect(jsonPath("$.elementos[0].fechaYHora").value("2026-08-15T10:30:00"))
                .andExpect(jsonPath("$.elementos[0].origen").value("Buenos Aires"))
                .andExpect(jsonPath("$.elementos[0].destino").value("Cordoba"));
    }

    @Test
    void noEntregaCatalogosCompletosSinSesion() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/hoteles")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/vuelos/pagina")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/hoteles/pagina")).andExpect(status().isForbidden());
    }

    @Test
    void filtraYPaginaHotelesEnLaBase() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/pagina")
                .queryParam("ciudad", "Goya").queryParam("tamanio", "1")
                .queryParam("orden", "precioAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elementos.length()").value(1))
                .andExpect(jsonPath("$.elementos[0].ciudad").value("Goya"))
                .andExpect(jsonPath("$.pagina").value(0));
    }

    @Test
    void rechazaPaginaDemasiadoGrandeYFiltrosConComodines() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/pagina")
                .queryParam("origen", "Buenos Aires").queryParam("tamanio", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/hoteles/pagina")
                .queryParam("ciudad", "%"))
                .andExpect(status().isBadRequest());
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
    void buscaVuelosPorTrayectoFechaYPlazasSinListarTodo() throws Exception {
        mockMvc.perform(get("/api/v1/vuelos/buscar")
                .queryParam("origen", "Buenos Aires").queryParam("destino", "Córdoba")
                .queryParam("fecha", "2026-08-15").queryParam("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value(100));

        mockMvc.perform(get("/api/v1/vuelos/buscar")
                .queryParam("origen", "Buenos Aires").queryParam("destino", "Cordoba")
                .queryParam("fecha", "2026-08-16").queryParam("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/vuelos/buscar")
                .queryParam("origen", "Buenos Aires").queryParam("destino", "Cordoba")
                .queryParam("fecha", "2099-01-01").queryParam("personas", "2")
                .queryParam("sugerencias", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Sin fecha busca los proximos vuelos; el de los datos de prueba ya salio
        mockMvc.perform(get("/api/v1/vuelos/buscar")
                .queryParam("origen", "Buenos Aires").queryParam("destino", "Cordoba")
                .queryParam("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscaHotelesDeLaCiudadConCapacidadParaLasFechas() throws Exception {
        mockMvc.perform(get("/api/v1/hoteles/buscar")
                .queryParam("ciudad", "Córdoba")
                .queryParam("fechaLlegada", "2026-08-15")
                .queryParam("fechaPartida", "2026-08-20")
                .queryParam("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value(2));

        mockMvc.perform(get("/api/v1/hoteles/buscar")
                .queryParam("ciudad", "Salta")
                .queryParam("fechaLlegada", "2026-08-15")
                .queryParam("fechaPartida", "2026-08-20")
                .queryParam("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
