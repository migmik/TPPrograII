package com.tijetravel.tijeback.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.tijetravel.tijeback.DnisPrueba;

/** Roles editables: un rol "Hotelero" que solo administra hoteles. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tijetravel-roles;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "app.seguridad.administrador-inicial.habilitado=true",
        "app.seguridad.administrador-inicial.nombre-usuario=admin-roles",
        "app.seguridad.administrador-inicial.contrasenia=ClaveAdmin123!"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RolesApiIntegracionTest {
    private static final String HOTEL = """
            {"nombre":"Hotel %s","direccion":"Calle 1","ciudad":"Cordoba","telefono":"351",
             "capacidadTotal":20,"precioNochePersona":30000}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unRolHoteleroSoloAdministraHotelesYSusCambiosSeAplicanSinVolverAEntrar() throws Exception {
        MockHttpSession admin = iniciarSesion("admin-roles", "ClaveAdmin123!");

        MvcResult rolCreado = mockMvc.perform(post("/api/v1/roles").session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Hotelero","tipoUsuario":"EMPLEADO",
                         "permisos":["VER_CATALOGO_COMPLETO","ADMINISTRAR_HOTELES"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permisos.length()").value(2))
                .andReturn();
        Integer codigoRol = JsonPath.read(rolCreado.getResponse().getContentAsString(), "$.codigo");

        MvcResult usuarioCreado = mockMvc.perform(post("/api/v1/usuarios").session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombreUsuario":"hotelero","contrasenia":"ClaveHotel123!","dni":"%s","codigoRol":%d}
                        """.formatted(DnisPrueba.siguiente(), codigoRol)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("Hotelero"))
                .andReturn();
        Integer codigoUsuario = JsonPath.read(usuarioCreado.getResponse().getContentAsString(), "$.codigo");

        MockHttpSession hotelero = iniciarSesion("hotelero", "ClaveHotel123!");
        mockMvc.perform(get("/api/v1/autenticacion/sesion").session(hotelero))
                .andExpect(jsonPath("$.permisos[1]").value("ADMINISTRAR_HOTELES"));
        mockMvc.perform(post("/api/v1/hoteles").session(hotelero).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(HOTEL.formatted("Uno")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/sucursales").session(hotelero).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direccion\":\"Calle 9\",\"telefono\":\"1\",\"ciudad\":\"Cordoba\"}"))
                .andExpect(status().isForbidden());

        // El rol en uso no se puede dar de baja.
        mockMvc.perform(delete("/api/v1/roles/{codigo}", codigoRol).session(admin).with(csrf()))
                .andExpect(status().isForbidden());

        // Se le saca el permiso al rol: la sesion abierta del hotelero lo pierde en el siguiente pedido.
        mockMvc.perform(put("/api/v1/roles/{codigo}", codigoRol).session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Hotelero\",\"permisos\":[\"VER_CATALOGO_COMPLETO\"]}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/hoteles").session(hotelero).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(HOTEL.formatted("Dos")))
                .andExpect(status().isForbidden());

        // Al darlo de baja, su sesion deja de valer.
        mockMvc.perform(delete("/api/v1/usuarios/{codigo}", codigoUsuario).session(admin).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/autenticacion/sesion").session(hotelero))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unRolDeClientesNoPuedeTenerPermisosDeEmpleado() throws Exception {
        MockHttpSession admin = iniciarSesion("admin-roles", "ClaveAdmin123!");
        mockMvc.perform(post("/api/v1/roles").session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Cliente VIP","tipoUsuario":"CLIENTE","permisos":["ADMINISTRAR_USUARIOS"]}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/roles/permisos").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("CONSULTAR"))
                .andExpect(jsonPath("$[0].soloEmpleados").value(false))
                .andExpect(jsonPath("$[9].soloEmpleados").value(true));
    }

    @Test
    void elAdministradorNoPuedeQuitarseElPermisoDeAdministrarUsuarios() throws Exception {
        MockHttpSession admin = iniciarSesion("admin-roles", "ClaveAdmin123!");
        MvcResult sesion = mockMvc.perform(get("/api/v1/roles").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Administrador"))
                .andReturn();
        Integer codigoAdministrador = JsonPath.read(sesion.getResponse().getContentAsString(), "$[0].codigo");

        mockMvc.perform(put("/api/v1/roles/{codigo}", codigoAdministrador).session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Administrador\",\"permisos\":[\"CONSULTAR\"]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OPERACION_NO_PERMITIDA"));
    }

    private MockHttpSession iniciarSesion(String usuario, String contrasenia) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/autenticacion/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombreUsuario": "%s", "contrasenia": "%s"}
                        """.formatted(usuario, contrasenia)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
