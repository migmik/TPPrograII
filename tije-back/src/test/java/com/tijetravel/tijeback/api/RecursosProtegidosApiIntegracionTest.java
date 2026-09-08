package com.tijetravel.tijeback.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL_RECURSOS:jdbc:h2:mem:tijetravel-recursos;MODE=MySQL;DB_CLOSE_DELAY=-1}",
        "app.seguridad.administrador-inicial.habilitado=true",
        "app.seguridad.administrador-inicial.nombre-usuario=admin-recursos",
        "app.seguridad.administrador-inicial.contrasenia=ClaveAdmin123!"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class RecursosProtegidosApiIntegracionTest {
    private static final String USUARIO_ADMIN = "admin-recursos";
    private static final String USUARIO_CLIENTE = "cliente-recursos";
    private static final String USUARIO_VENDEDOR = "vendedor-recursos";
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
    private VueloRepositorio vueloRepositorio;

    @Autowired
    private HotelRepositorio hotelRepositorio;

    @Autowired
    private PasswordEncoder codificadorContrasenias;

    private Integer codigoUsuarioCliente;
    private Integer codigoReservaExterna;

    @BeforeEach
    void prepararUsuariosYReservaExterna() {
        Turista titular = turistaRepositorio.findById(1).orElseThrow();
        Usuario usuarioCliente = usuarioRepositorio.findByNombreUsuarioIgnoreCase(USUARIO_CLIENTE)
                .orElseGet(() -> usuarioRepositorio.save(new Cliente(
                        USUARIO_CLIENTE,
                        codificadorContrasenias.encode(CONTRASENIA_CLIENTE),
                        titular)));
        codigoUsuarioCliente = usuarioCliente.getCodigo();
        usuarioRepositorio.findByNombreUsuarioIgnoreCase(USUARIO_VENDEDOR)
                .orElseGet(() -> usuarioRepositorio.save(new Vendedor(
                        USUARIO_VENDEDOR,
                        codificadorContrasenias.encode(CONTRASENIA_VENDEDOR))));

        Reserva reservaExterna = reservaRepositorio.findByTuristaCodigo(3).stream()
                .findFirst()
                .orElseGet(() -> reservaRepositorio.save(new Reserva(
                        turistaRepositorio.findById(3).orElseThrow(),
                        vueloRepositorio.findById(103).orElseThrow(),
                        hotelRepositorio.findById(4).orElseThrow(),
                        ClaseVuelo.TURISTA,
                        TipoHospedaje.MEDIA_PENSION,
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 5))));
        codigoReservaExterna = reservaExterna.getCodigo();
    }

    @Test
    void exigeSesionParaConsultarDatosProtegidos() throws Exception {
        mockMvc.perform(get("/api/v1/turistas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTENTICACION_REQUERIDA"));

        mockMvc.perform(get("/api/v1/reservas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTENTICACION_REQUERIDA"));
    }

    @Test
    void administradorConsultaTodosLosRecursosSinExponerContrasenias() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_ADMIN, CONTRASENIA_ADMIN);

        mockMvc.perform(get("/api/v1/turistas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].codigo").value(1))
                .andExpect(jsonPath("$[0].codigoSucursal").value(1))
                .andExpect(jsonPath("$[1].codigoTitular").value(1));

        mockMvc.perform(get("/api/v1/reservas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].codigoTurista").value(1))
                .andExpect(jsonPath("$[0].codigoSucursalContratacion").value(1))
                .andExpect(jsonPath("$[0].numeroVuelo").value(100));

        mockMvc.perform(get("/api/v1/usuarios").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].contrasenia").doesNotExist());

        mockMvc.perform(get("/api/v1/usuarios/{codigo}", codigoUsuarioCliente).session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(USUARIO_CLIENTE))
                .andExpect(jsonPath("$.codigoTurista").value(1))
                .andExpect(jsonPath("$.contrasenia").doesNotExist());
    }

    @Test
    void vendedorConsultaTuristasYReservasPeroNoUsuarios() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_VENDEDOR, CONTRASENIA_VENDEDOR);

        mockMvc.perform(get("/api/v1/turistas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/v1/reservas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/v1/usuarios").session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void clienteSoloConsultaSuGrupoFamiliarYSusReservas() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_CLIENTE, CONTRASENIA_CLIENTE);

        mockMvc.perform(get("/api/v1/turistas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value(1))
                .andExpect(jsonPath("$[1].codigo").value(2));

        mockMvc.perform(get("/api/v1/reservas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value(1))
                .andExpect(jsonPath("$[1].codigo").value(2));

        mockMvc.perform(get("/api/v1/turistas/{codigo}", 2).session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoTitular").value(1));
    }

    @Test
    void clienteNoConsultaDatosDeOtroGrupoFamiliar() throws Exception {
        MockHttpSession sesion = iniciarSesion(USUARIO_CLIENTE, CONTRASENIA_CLIENTE);

        mockMvc.perform(get("/api/v1/turistas/{codigo}", 3).session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(get("/api/v1/reservas/{codigo}", codigoReservaExterna).session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));

        mockMvc.perform(get("/api/v1/usuarios").session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    private MockHttpSession iniciarSesion(String usuario, String contrasenia) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/autenticacion/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(usuario, contrasenia)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(usuario))
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
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
