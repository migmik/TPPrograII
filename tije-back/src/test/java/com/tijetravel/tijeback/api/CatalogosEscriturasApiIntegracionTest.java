package com.tijetravel.tijeback.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL_CATALOGOS:jdbc:h2:mem:tijetravel-catalogos-escrituras;MODE=MySQL;DB_CLOSE_DELAY=-1}",
        "app.seguridad.administrador-inicial.habilitado=true",
        "app.seguridad.administrador-inicial.nombre-usuario=admin-catalogos",
        "app.seguridad.administrador-inicial.contrasenia=ClaveAdmin123!"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
@Transactional
class CatalogosEscriturasApiIntegracionTest {
    private static final String USUARIO_ADMIN = "admin-catalogos";
    private static final String USUARIO_VENDEDOR = "vendedor-catalogos";
    private static final String CONTRASENIA_ADMIN = "ClaveAdmin123!";
    private static final String CONTRASENIA_VENDEDOR = "ClaveVendedor123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SucursalRepositorio sucursalRepositorio;

    @Autowired
    private HotelRepositorio hotelRepositorio;

    @Autowired
    private VueloRepositorio vueloRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder codificadorContrasenias;

    @BeforeEach
    void prepararVendedor() {
        usuarioRepositorio.save(new Vendedor(
                USUARIO_VENDEDOR,
                codificadorContrasenias.encode(CONTRASENIA_VENDEDOR)));
    }

    @Test
    void administradorGestionaCatalogosDePuntaAPunta() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(post("/api/v1/sucursales")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "direccion": "Av. Prueba 100",
                                  "telefono": "1111-1000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesPattern(
                        "/api/v1/sucursales/\\d+")))
                .andExpect(jsonPath("$.direccion").value("Av. Prueba 100"));

        Sucursal sucursal = sucursalRepositorio
                .findAll()
                .stream()
                .filter(elemento -> elemento.getDireccion().equals("Av. Prueba 100"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/v1/sucursales/{codigo}", sucursal.getCodigo())
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "direccion": "Av. Prueba 200",
                                  "telefono": "1111-2000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Av. Prueba 200"));

        mockMvc.perform(post("/api/v1/hoteles")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Hotel Prueba",
                                  "direccion": "Costanera 100",
                                  "ciudad": "Rosario",
                                  "telefono": "2222-1000",
                                  "plazasDisponibles": 25
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesPattern(
                        "/api/v1/hoteles/\\d+")))
                .andExpect(jsonPath("$.capacidadTotal").value(25));

        Hotel hotel = hotelRepositorio
                .findAll()
                .stream()
                .filter(elemento -> elemento.getNombre().equals("Hotel Prueba"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/v1/hoteles/{codigo}", hotel.getCodigo())
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Hotel Prueba Actualizado",
                                  "direccion": "Costanera 200",
                                  "ciudad": "Rosario",
                                  "telefono": "2222-2000",
                                  "capacidadTotal": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hotel Prueba Actualizado"))
                .andExpect(jsonPath("$.capacidadTotal").value(30));

        mockMvc.perform(post("/api/v1/vuelos")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numero": 9001,
                                  "fechaYHora": "2027-01-15T10:30:00",
                                  "origen": "Buenos Aires",
                                  "destino": "Rosario",
                                  "totalPlazas": 100,
                                  "plazasTurista": 80,
                                  "plazasPrimera": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/vuelos/9001"))
                .andExpect(jsonPath("$.numero").value(9001));

        mockMvc.perform(put("/api/v1/vuelos/{numero}", 9001)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaYHora": "2027-01-15T11:45:00",
                                  "origen": "Buenos Aires",
                                  "destino": "Rosario",
                                  "totalPlazas": 110,
                                  "plazasTurista": 85,
                                  "plazasPrimera": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaYHora").value("2027-01-15T11:45:00"))
                .andExpect(jsonPath("$.totalPlazas").value(110));

        mockMvc.perform(delete("/api/v1/vuelos/{numero}", 9001)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/hoteles/{codigo}", hotel.getCodigo())
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/sucursales/{codigo}", sucursal.getCodigo())
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(vueloRepositorio.existsById(9001));
        assertFalse(hotelRepositorio.existsById(hotel.getCodigo()));
        assertFalse(sucursalRepositorio.existsById(sucursal.getCodigo()));
    }

    @Test
    void vendedorNoPuedeEscribirCatalogos() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_VENDEDOR, CONTRASENIA_VENDEDOR);

        mockMvc.perform(post("/api/v1/sucursales")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(put("/api/v1/hoteles/{codigo}", 1)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(delete("/api/v1/vuelos/{numero}", 100)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void exigeSesionCsrfYDatosValidos() throws Exception {
        mockMvc.perform(post("/api/v1/sucursales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTENTICACION_REQUERIDA"));

        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(post("/api/v1/hoteles")
                        .session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(post("/api/v1/vuelos")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numero": -1,
                                  "fechaYHora": "2027-01-15T10:30:00",
                                  "origen": " ",
                                  "destino": "Rosario",
                                  "totalPlazas": 0,
                                  "plazasTurista": -1,
                                  "plazasPrimera": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.detalles.numero").exists())
                .andExpect(jsonPath("$.detalles.origen").exists())
                .andExpect(jsonPath("$.detalles.totalPlazas").exists())
                .andExpect(jsonPath("$.detalles.plazasTurista").exists());
    }

    @Test
    void rechazaDuplicadosReferenciasYReduccionesDeCapacidad() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(post("/api/v1/sucursales")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "direccion": "Av Corrientes 1234",
                                  "telefono": "otro"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTIDAD_DUPLICADA"));

        mockMvc.perform(delete("/api/v1/sucursales/{codigo}", 1)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(delete("/api/v1/hoteles/{codigo}", 2)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(delete("/api/v1/vuelos/{numero}", 100)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(put("/api/v1/hoteles/{codigo}", 2)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Hotel Sierras",
                                  "direccion": "Ruta Provincial 5 Km 70",
                                  "ciudad": "Cordoba",
                                  "telefono": "0351-555-1111",
                                  "capacidadTotal": 0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CAPACIDAD_EXCEDIDA"));

        mockMvc.perform(put("/api/v1/vuelos/{numero}", 100)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaYHora": "2026-08-15T10:30:00",
                                  "origen": "Buenos Aires",
                                  "destino": "Cordoba",
                                  "totalPlazas": 20,
                                  "plazasTurista": 0,
                                  "plazasPrimera": 20
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CAPACIDAD_EXCEDIDA"));
    }

    private MockHttpSession iniciarSesion(String usuario, String contrasenia) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/autenticacion/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreUsuario": "%s",
                                  "contrasenia": "%s"
                                }
                                """.formatted(usuario, contrasenia)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
