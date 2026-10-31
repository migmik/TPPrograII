package com.tijetravel.tijeback.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
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
                Turista titular = turistaRepositorio.save(new Turista(DnisPrueba.siguiente(), "Ana",
                                "Perez",
                                "Calle 1",
                                "ana@example.com",
                                "351-2000",
                                "351-3000",
                                sucursal));
                Turista familiar = turistaRepositorio.save(new Turista(DnisPrueba.siguiente(), "Luis",
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
                usuarioRepositorio.save(new Vendedor("vendedor", "clave", DnisPrueba.siguiente()));
                usuarioRepositorio.save(new Administrador("admin", "clave", DnisPrueba.siguiente()));
                reservaRepositorio.saveAndFlush(new Reserva(
                                familiar,
                                vuelo,
                                hotel,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 1),
                                LocalDate.of(2026, 10, 3)));

                Usuario usuario = usuarioRepositorio.buscarActivoPorNombre("ANA");

                assertInstanceOf(Cliente.class, usuario);
                assertEquals(RolUsuario.CLIENTE, usuario.getRol());
                assertEquals(titular.getCodigo(), usuario.getCodigoTurista());
                assertEquals(1, usuarioRepositorio.contarClientesPorTurista(titular.getCodigo()));
                assertEquals(1, turistaRepositorio.findByTitularCodigo(titular.getCodigo()).size());
                assertEquals(titular.getCodigo(), turistaRepositorio.findByTitularIsNullAndFechaBajaIsNullOrderByCodigo().get(0).getCodigo());
                assertEquals(familiar.getCodigo(), turistaRepositorio.findByTitularIsNotNullAndFechaBajaIsNullOrderByCodigo().get(0).getCodigo());
                assertEquals(familiar.getCodigo(), turistaRepositorio.findByDniAndFechaBajaIsNull(familiar.getDni()).get(0).getCodigo());
                assertEquals(1, usuarioRepositorio.listarActivosPorRol(RolUsuario.CLIENTE).size());
                assertEquals(1, usuarioRepositorio.listarActivosPorRol(RolUsuario.VENDEDOR).size());
                assertEquals(1, usuarioRepositorio.listarActivosPorRol(RolUsuario.ADMINISTRADOR).size());
                assertEquals(1, reservaRepositorio.listarPorTitularYFamiliares(titular.getCodigo()).size());
                assertEquals(1, reservaRepositorio.findByTuristaCodigo(familiar.getCodigo()).size());
                assertEquals(1, reservaRepositorio.findByVueloNumeroAndFechaBajaIsNull(100).size());
                assertEquals(1, reservaRepositorio.findByHotelCodigoAndFechaBajaIsNull(hotel.getCodigo()).size());
                assertEquals(1, reservaRepositorio.findByFechaBajaIsNull(Sort.by("codigo")).size());
                assertEquals(1, reservaRepositorio.findAll(PageRequest.of(0, 1)).getTotalElements());
                assertEquals(1, reservaRepositorio.buscarPagina(null, familiar.getCodigo(), familiar.getDni(),
                                100, hotel.getCodigo(), null, null, false, PageRequest.of(0, 10))
                                .getTotalElements());
                assertEquals(0, reservaRepositorio.buscarPagina(null, null, titular.getDni(),
                                null, null, null, null, null, PageRequest.of(0, 10))
                                .getTotalElements());
                assertEquals(1, reservaRepositorio.buscarPagina(titular.getCodigo(), null, null,
                                null, null, null, null, null, PageRequest.of(0, 10))
                                .getTotalElements());
                assertEquals(1, reservaRepositorio.balancePagadas().getReservasPendientes());
                assertEquals(0, reservaRepositorio.balancePagadas().getReservasPagadas());
                assertEquals(1, usuarioRepositorio.buscarPagina(RolUsuario.CLIENTE, null, null,
                                titular.getDni(), PageRequest.of(0, 10)).getTotalElements());
                assertEquals(1, usuarioRepositorio.buscarPagina(RolUsuario.ADMINISTRADOR,
                                usuarioRepositorio.listarActivosPorRol(RolUsuario.ADMINISTRADOR).get(0).getCodigo(),
                                null, null, PageRequest.of(0, 10)).getTotalElements());
        }

        @Test
        void aplicaLaMigracionInicialAntesDeValidarJpa() {
                assertEquals("10", flyway.info().current().getVersion().getVersion());
        }

        @Test
        void unDniDadoDeBajaSePuedeVolverAUsarPeroNoEntreActivos() {
                Sucursal sucursal = sucursalRepositorio.save(new Sucursal("Av. Bajas 1", "351-1000"));
                String dni = DnisPrueba.siguiente();
                Turista anterior = turistaRepositorio.saveAndFlush(new Turista(dni, "Ana", "Lopez", "Calle 1",
                                "repetido@example.com", "1", "2", sucursal));
                anterior.darDeBaja(LocalDateTime.of(2026, 1, 1, 10, 0));
                turistaRepositorio.saveAndFlush(anterior);

                Turista nueva = turistaRepositorio.saveAndFlush(new Turista(dni, "Ana", "Lopez", "Calle 2",
                                "repetido@example.com", "1", "2", sucursal));
                assertTrue(nueva.estaActivo());
                assertEquals(1, turistaRepositorio.findByDniAndFechaBajaIsNull(dni).size());

                try {
                        turistaRepositorio.saveAndFlush(new Turista(dni, "Otra", "Persona", "Calle 3",
                                        "otro@example.com", "1", "2", sucursal));
                        fail("La base tenia que rechazar dos turistas activos con el mismo DNI");
                } catch (DataIntegrityViolationException esperada) {
                        // El indice unico sobre dni_activo sigue controlando a los activos.
                }
        }
}
