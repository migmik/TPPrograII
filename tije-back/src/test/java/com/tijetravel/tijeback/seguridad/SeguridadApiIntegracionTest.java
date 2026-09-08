package com.tijetravel.tijeback.seguridad;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL_SEGURIDAD:jdbc:h2:mem:tijetravel-seguridad;MODE=MySQL;DB_CLOSE_DELAY=-1}",
        "app.seguridad.administrador-inicial.habilitado=true",
        "app.seguridad.administrador-inicial.nombre-usuario=admin-pruebas",
        "app.seguridad.administrador-inicial.contrasenia=ClaveSegura123!"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadApiIntegracionTest {
    private static final String USUARIO = "admin-pruebas";
    private static final String CONTRASENIA = "ClaveSegura123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder codificadorContrasenias;

    @Test
    void creaElAdministradorInicialConContraseniaHasheada() {
        Usuario administrador = usuarioRepositorio.findByNombreUsuarioIgnoreCase(USUARIO)
                .orElseThrow();

        assertNotEquals(CONTRASENIA, administrador.getContrasenia());
        assertTrue(administrador.getContrasenia().startsWith("{bcrypt}"));
        assertTrue(codificadorContrasenias.matches(CONTRASENIA, administrador.getContrasenia()));
    }

    @Test
    void entregaUnTokenCsrfAntesDelLogin() throws Exception {
        mockMvc.perform(get("/api/v1/autenticacion/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreEncabezado").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.nombreParametro").value("_csrf"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void permitePeticionesCorsSoloDesdeElFrontendConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/autenticacion/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        "true"));

        mockMvc.perform(options("/api/v1/autenticacion/login")
                        .header(HttpHeaders.ORIGIN, "http://origen-no-permitido.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void iniciaSesionYLaConservaEntrePeticiones() throws Exception {
        MvcResult login = iniciarSesion();
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/api/v1/autenticacion/sesion").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(USUARIO))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    void rechazaLoginSinTokenCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacion/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(USUARIO, CONTRASENIA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void rechazaCredencialesIncorrectasSinRevelarElMotivo() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacion/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(USUARIO, "ClaveIncorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasenia incorrectos"));
    }

    @Test
    void rechazaContraseniasQueSuperanElLimiteDeBcryptEnBytes() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacion/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(USUARIO, "\u00e1".repeat(40))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void exigeAutenticacionParaConsultarLaSesion() throws Exception {
        mockMvc.perform(get("/api/v1/autenticacion/sesion"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTENTICACION_REQUERIDA"));
    }

    @Test
    void cierraLaSesionConPostYCsrf() throws Exception {
        MvcResult login = iniciarSesion();
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/autenticacion/logout")
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void impideQueUnVendedorAccedaALaAdministracionDeUsuarios() throws Exception {
        String usuarioVendedor = "vendedor-pruebas";
        String contraseniaVendedor = "ClaveVendedor123!";
        usuarioRepositorio.findByNombreUsuarioIgnoreCase(usuarioVendedor)
                .orElseGet(() -> usuarioRepositorio.save(new Vendedor(
                        usuarioVendedor,
                        codificadorContrasenias.encode(contraseniaVendedor))));

        MvcResult login = iniciarSesion(usuarioVendedor, contraseniaVendedor);
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/api/v1/usuarios").session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    private MvcResult iniciarSesion() throws Exception {
        return iniciarSesion(USUARIO, CONTRASENIA);
    }

    private MvcResult iniciarSesion(String usuario, String contrasenia) throws Exception {
        return mockMvc.perform(post("/api/v1/autenticacion/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(usuario, contrasenia)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(usuario))
                .andReturn();
    }

    private String credenciales(String usuario, String contrasenia) {
        return """
                {
                  "nombreUsuario": "%s",
                  "contrasenia": "%s"
                }
                """.formatted(usuario, contrasenia);
    }
}
