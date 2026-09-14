package com.tijetravel.tijeback.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties =
        "spring.datasource.url=${TEST_DB_URL_DEV:jdbc:h2:mem:tijetravel-dev;MODE=MySQL;DB_CLOSE_DELAY=-1}")
@ActiveProfiles({"test", "dev"})
class DatosDesarrolloMigracionTest {
    @Autowired
    private Flyway flyway;

    @Autowired
    private SucursalRepositorio sucursalRepositorio;

    @Autowired
    private HotelRepositorio hotelRepositorio;

    @Autowired
    private VueloRepositorio vueloRepositorio;

    @Autowired
    private TuristaRepositorio turistaRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void cargaLosDatosDeDemostracionSinContraseniasEnTextoPlano() {
        assertEquals("5", flyway.info().current().getVersion().getVersion());
        assertEquals(2, sucursalRepositorio.count());
        assertEquals(4, hotelRepositorio.count());
        assertEquals(4, vueloRepositorio.count());
        assertEquals(3, turistaRepositorio.count());
        assertEquals(3, reservaRepositorio.count());
        assertEquals(0, usuarioRepositorio.count());
    }
}
