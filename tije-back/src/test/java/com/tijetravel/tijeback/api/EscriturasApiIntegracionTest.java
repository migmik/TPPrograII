package com.tijetravel.tijeback.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tijetravel-escrituras;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "app.seguridad.administrador-inicial.habilitado=true",
        "app.seguridad.administrador-inicial.nombre-usuario=admin-escrituras",
        "app.seguridad.administrador-inicial.contrasenia=ClaveAdmin123!"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
@Transactional
class EscriturasApiIntegracionTest {
    private static final String USUARIO_ADMIN = "admin-escrituras";
    private static final String USUARIO_CLIENTE = "cliente-escrituras";
    private static final String USUARIO_VENDEDOR = "vendedor-escrituras";
    private static final String CONTRASENIA_ADMIN = "ClaveAdmin123!";
    private static final String CONTRASENIA_CLIENTE = "ClaveCliente123!";
    private static final String CONTRASENIA_VENDEDOR = "ClaveVendedor123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private TuristaRepositorio turistaRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Autowired
    private PasswordEncoder codificadorContrasenias;

    @BeforeEach
    void prepararUsuarios() {
        Turista titular = turistaRepositorio.findById(1).orElseThrow();
        usuarioRepositorio.save(new Cliente(
                USUARIO_CLIENTE,
                codificadorContrasenias.encode(CONTRASENIA_CLIENTE),
                titular));
        usuarioRepositorio.save(new Vendedor(
                USUARIO_VENDEDOR,
                codificadorContrasenias.encode(CONTRASENIA_VENDEDOR)));
    }

    @Test
    void vendedorGestionaTuristaYReservaDePuntaAPunta() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_VENDEDOR, CONTRASENIA_VENDEDOR);

        mockMvc.perform(post("/api/v1/turistas")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lucia",
                                  "apellido": "Diaz",
                                  "direccion": "Mitre 500",
                                  "email": "lucia.escrituras@example.com",
                                  "telefonoFijo": "1111-2222",
                                  "telefonoCelular": "1133334444",
                                  "codigoSucursal": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/v1/turistas/\\d+")))
                .andExpect(jsonPath("$.titular").value(true))
                .andExpect(jsonPath("$.codigoSucursal").value(2));

        Turista turista = turistaRepositorio
                .findByEmailIgnoreCase("lucia.escrituras@example.com")
                .orElseThrow();

        mockMvc.perform(put("/api/v1/turistas/{codigo}", turista.getCodigo())
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lucia Elena",
                                  "apellido": "Diaz",
                                  "direccion": "Mitre 550",
                                  "email": "lucia.actualizada@example.com",
                                  "telefonoFijo": "1111-5555",
                                  "telefonoCelular": "1166667777",
                                  "codigoSucursal": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lucia Elena"))
                .andExpect(jsonPath("$.email").value("lucia.actualizada@example.com"));

        mockMvc.perform(post("/api/v1/reservas")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoTurista": %d,
                                  "numeroVuelo": 103,
                                  "codigoHotel": 4,
                                  "claseVuelo": "PRIMERA",
                                  "tipoHospedaje": "MEDIA_PENSION",
                                  "fechaLlegada": "2026-11-01",
                                  "fechaPartida": "2026-11-06"
                                }
                                """.formatted(turista.getCodigo())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/v1/reservas/\\d+")))
                .andExpect(jsonPath("$.codigoTurista").value(turista.getCodigo()))
                .andExpect(jsonPath("$.codigoSucursalContratacion").value(2))
                .andExpect(jsonPath("$.claseVuelo").value("PRIMERA"));

        Integer codigoReserva = reservaRepositorio
                .findByTuristaCodigo(turista.getCodigo())
                .getFirst()
                .getCodigo();

        mockMvc.perform(put("/api/v1/reservas/{codigo}", codigoReserva)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoTurista": %d,
                                  "numeroVuelo": 103,
                                  "codigoHotel": 4,
                                  "claseVuelo": "TURISTA",
                                  "tipoHospedaje": "PENSION_COMPLETA",
                                  "fechaLlegada": "2026-11-01",
                                  "fechaPartida": "2026-11-07"
                                }
                                """.formatted(turista.getCodigo())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claseVuelo").value("TURISTA"))
                .andExpect(jsonPath("$.tipoHospedaje").value("PENSION_COMPLETA"));

        mockMvc.perform(delete("/api/v1/reservas/{codigo}", codigoReserva)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/turistas/{codigo}", turista.getCodigo())
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(reservaRepositorio.existsById(codigoReserva));
        assertFalse(turistaRepositorio.existsById(turista.getCodigo()));
    }

    @Test
    void administradorGestionaUsuariosSinExponerContrasenia() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(post("/api/v1/usuarios")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreUsuario": "operador-nuevo",
                                  "contrasenia": "ClaveOperador123!",
                                  "rol": "VENDEDOR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/v1/usuarios/\\d+")))
                .andExpect(jsonPath("$.nombreUsuario").value("operador-nuevo"))
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.contrasenia").doesNotExist());

        Usuario usuario = usuarioRepositorio
                .findByNombreUsuarioIgnoreCase("operador-nuevo")
                .orElseThrow();
        assertTrue(codificadorContrasenias.matches("ClaveOperador123!", usuario.getContrasenia()));

        mockMvc.perform(put("/api/v1/usuarios/{codigo}", usuario.getCodigo())
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreUsuario": "operador-actualizado",
                                  "contrasenia": "NuevaClaveOperador123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("operador-actualizado"))
                .andExpect(jsonPath("$.contrasenia").doesNotExist());

        assertTrue(codificadorContrasenias.matches(
                "NuevaClaveOperador123!",
                usuarioRepositorio.findById(usuario.getCodigo()).orElseThrow().getContrasenia()));

        mockMvc.perform(delete("/api/v1/usuarios/{codigo}", usuario.getCodigo())
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(usuarioRepositorio.existsById(usuario.getCodigo()));
    }

    @Test
    void clienteNoPuedeEjecutarOperacionesDeEscritura() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_CLIENTE, CONTRASENIA_CLIENTE);

        mockMvc.perform(post("/api/v1/turistas")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(delete("/api/v1/reservas/{codigo}", 1)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(post("/api/v1/usuarios")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void exigeCsrfYValidaElCuerpoAntesDeGuardar() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(post("/api/v1/turistas")
                        .session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));

        mockMvc.perform(post("/api/v1/turistas")
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": " ",
                                  "apellido": "Diaz",
                                  "direccion": "Mitre 500",
                                  "email": "email-invalido",
                                  "telefonoFijo": "1111",
                                  "telefonoCelular": "2222",
                                  "codigoSucursal": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.detalles.nombre").exists())
                .andExpect(jsonPath("$.detalles.email").exists());
    }

    @Test
    void conservaLasRestriccionesDeNegocioEnLaApi() throws Exception {
        MockHttpSession sesionAdmin = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);
        MockHttpSession sesionVendedor = iniciarSesion(USUARIO_VENDEDOR, CONTRASENIA_VENDEDOR);
        Integer codigoAdmin = usuarioRepositorio
                .findByNombreUsuarioIgnoreCase(USUARIO_ADMIN)
                .orElseThrow()
                .getCodigo();

        mockMvc.perform(delete("/api/v1/usuarios/{codigo}", codigoAdmin)
                        .session(sesionAdmin)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(delete("/api/v1/turistas/{codigo}", 1)
                        .session(sesionVendedor)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(post("/api/v1/turistas")
                        .session(sesionVendedor)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Duplicado",
                                  "apellido": "Perez",
                                  "direccion": "Otra direccion",
                                  "email": "juan.perez@mail.com",
                                  "telefonoFijo": "1111",
                                  "telefonoCelular": "2222",
                                  "codigoSucursal": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTIDAD_DUPLICADA"));
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
