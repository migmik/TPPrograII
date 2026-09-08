package com.tijetravel.tijeback.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoriosIntegracionTest {
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
    void persisteRelacionesHerenciaYConsultasDelDominio() {
        Sucursal sucursal = sucursalRepositorio.save(new Sucursal("Av. Colon 100", "351-1000"));
        Turista titular = turistaRepositorio.save(new Turista(
                "Ana",
                "Perez",
                "Calle 1",
                "ana@example.com",
                "351-2000",
                "351-3000",
                sucursal));
        Turista familiar = turistaRepositorio.save(new Turista(
                "Luis",
                "Perez",
                "Calle 1",
                "luis@example.com",
                "351-2000",
                "351-4000",
                sucursal,
                titular));
        Hotel hotel = hotelRepositorio.save(new Hotel(
                "Hotel Centro", "Calle 2", "Cordoba", "351-5000", 10));
        Vuelo vuelo = vueloRepositorio.save(new Vuelo(
                100,
                LocalDateTime.of(2026, 10, 1, 10, 0),
                "Buenos Aires",
                "Cordoba",
                20,
                15,
                5));
        usuarioRepositorio.save(new Cliente("ana", "clave", titular));
        reservaRepositorio.saveAndFlush(new Reserva(
                familiar,
                vuelo,
                hotel,
                ClaseVuelo.TURISTA,
                TipoHospedaje.MEDIA_PENSION,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3)));

        Usuario usuario = usuarioRepositorio.findByNombreUsuarioIgnoreCase("ANA").orElseThrow();

        assertInstanceOf(Cliente.class, usuario);
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertEquals(titular.getCodigo(), usuario.getCodigoTurista());
        assertEquals(1, usuarioRepositorio.contarClientesPorTurista(titular.getCodigo()));
        assertEquals(1, turistaRepositorio.findByTitularCodigo(titular.getCodigo()).size());
        assertEquals(1, reservaRepositorio.listarPorTitularYFamiliares(titular.getCodigo()).size());
    }

    @Test
    void aplicaLaMigracionInicialAntesDeValidarJpa() {
        assertEquals("1", flyway.info().current().getVersion().getVersion());
    }
}
