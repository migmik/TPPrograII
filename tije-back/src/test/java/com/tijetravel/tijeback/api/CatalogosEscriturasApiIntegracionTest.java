package com.tijetravel.tijeback.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@SpringBootTest(properties = {
                "spring.datasource.url=${TEST_DB_URL_CATALOGOS:jdbc:h2:mem:tijetravel-catalogos-escrituras;MODE=MySQL;DB_CLOSE_DELAY=-1}",
                "app.seguridad.administrador-inicial.habilitado=true",
                "app.seguridad.administrador-inicial.nombre-usuario=admin-catalogos",
                "app.seguridad.administrador-inicial.contrasenia=ClaveAdmin123!"
})
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "dev" })
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
        private ReservaRepositorio reservaRepositorio;

        @Autowired
        private TuristaRepositorio turistaRepositorio;

        @Autowired
        private UsuarioRepositorio usuarioRepositorio;

        @Autowired
        private PasswordEncoder codificadorContrasenias;

        @BeforeEach
        void prepararVendedor() {
                usuarioRepositorio.save(new Vendedor(
                                USUARIO_VENDEDOR,
                                codificadorContrasenias.encode(CONTRASENIA_VENDEDOR), DnisPrueba.siguiente()));
        }

        @Test
        void laCapacidadDelHotelDebeIngresarseConElNombreActual() throws Exception {
                MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);
                mockMvc.perform(post("/api/v1/hoteles")
                                .session(sesion).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"nombre":"Hotel antiguo", "direccion":"Calle", "ciudad":"Cordoba",
                                                 "telefono":"123", "plazasDisponibles":25}
                                                """))
                                .andExpect(status().isBadRequest());
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
                                                  "telefono": "1111-1000", "ciudad": "Rosario"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", Matchers.matchesPattern(
                                                "/api/v1/sucursales/\\d+")))
                                .andExpect(jsonPath("$.direccion").value("Av. Prueba 100"));

                Sucursal sucursal = buscarSucursal("Av. Prueba 100");

                mockMvc.perform(put("/api/v1/sucursales/{codigo}", sucursal.getCodigo())
                                .session(sesion)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "direccion": "Av. Prueba 200",
                                                  "telefono": "1111-2000", "ciudad": "Rosario"
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
                                                  "capacidadTotal": 25,"precioNochePersona":35000
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", Matchers.matchesPattern(
                                                "/api/v1/hoteles/\\d+")))
                                .andExpect(jsonPath("$.capacidadTotal").value(25));

                Hotel hotel = buscarHotel("Hotel Prueba");

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
                                                  "capacidadTotal": 30,"precioNochePersona":35000
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
                                                  "plazasPrimera": 20,"precio":200000
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
                                                  "plazasPrimera": 25,"precio":200000
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

                // La baja es logica: las filas siguen en la base con su fecha de baja.
                assertFalse(vueloRepositorio.findById(9001).get().estaActivo());
                assertFalse(hotelRepositorio.findById(hotel.getCodigo()).get().estaActivo());
                assertFalse(sucursalRepositorio.findById(sucursal.getCodigo()).get().estaActivo());
                mockMvc.perform(get("/api/v1/sucursales/{codigo}", sucursal.getCodigo()))
                                .andExpect(status().isNotFound());
                mockMvc.perform(get("/api/v1/sucursales/bajas").session(sesion))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].codigo").value(sucursal.getCodigo()))
                                .andExpect(jsonPath("$[0].fechaBaja").isNotEmpty());

                mockMvc.perform(post("/api/v1/sucursales/{codigo}/reactivar", sucursal.getCodigo())
                                .session(sesion)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.fechaBaja").isEmpty());
                assertTrue(sucursalRepositorio.findById(sucursal.getCodigo()).get().estaActivo());
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
                                                  "plazasPrimera": 10,"precio":200000
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
                                                  "telefono": "otro", "ciudad": "Buenos Aires"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("ENTIDAD_DUPLICADA"));

                mockMvc.perform(delete("/api/v1/sucursales/{codigo}", 1)
                                .session(sesion)
                                .with(csrf()))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

                // Un hotel y un vuelo con reservas futuras no se pueden dar de baja.
                LocalDate salida = LocalDate.now().plusDays(30);
                Vuelo vueloFuturo = vueloRepositorio.save(new Vuelo(
                                9100, salida.atTime(10, 0), "Buenos Aires", "Cordoba", 20, 15, 5));
                Hotel hotelFuturo = hotelRepositorio.save(new Hotel(
                                "Hotel Futuro", "Calle 1", "Cordoba", "351-0000", 10));
                reservaRepositorio.save(new Reserva(turistaRepositorio.findById(1).get(), vueloFuturo, hotelFuturo,
                                ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, salida, salida.plusDays(2)));

                mockMvc.perform(delete("/api/v1/hoteles/{codigo}", hotelFuturo.getCodigo())
                                .session(sesion)
                                .with(csrf()))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

                mockMvc.perform(delete("/api/v1/vuelos/{numero}", 9100)
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
                                                  "capacidadTotal": 0,"precioNochePersona":35000
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
                                                  "plazasPrimera": 20,"precio":200000
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("CAPACIDAD_EXCEDIDA"));

                // Si las reservas ya terminaron, el hotel se da de baja y las reservas lo siguen mostrando.
                mockMvc.perform(delete("/api/v1/hoteles/{codigo}", 2)
                                .session(sesion)
                                .with(csrf()))
                                .andExpect(status().isNoContent());
                mockMvc.perform(get("/api/v1/reservas/{codigo}", 1).session(sesion))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigoHotel").value(2));
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

        private Sucursal buscarSucursal(String direccion) {
                for (Sucursal sucursal : sucursalRepositorio.findAll()) {
                        if (sucursal.getDireccion().equals(direccion)) {
                                return sucursal;
                        }
                }
                throw new AssertionError("No se encontro la sucursal " + direccion);
        }

        private Hotel buscarHotel(String nombre) {
                for (Hotel hotel : hotelRepositorio.findAll()) {
                        if (hotel.getNombre().equals(nombre)) {
                                return hotel;
                        }
                }
                throw new AssertionError("No se encontro el hotel " + nombre);
        }
}
