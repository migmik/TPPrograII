package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.*;
import com.tijetravel.tijeback.excepciones.*;
import com.tijetravel.tijeback.modelos.*;
import com.tijetravel.tijeback.repositorios.*;

@SpringBootTest(properties = "spring.datasource.url=${TEST_DB_URL_REGLAS:jdbc:h2:mem:reglas-nuevas;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000}")
@ActiveProfiles("test")
class ReglasNegocioIntegracionTest {
    private static final AtomicInteger SECUENCIA = new AtomicInteger(5000);
    private static final LocalDate DIA = LocalDate.of(2027, 2, 1);
    private final Usuario admin = new Administrador("operador", "hash", DnisPrueba.siguiente());

    @Autowired
    ReservaServicio reservas;
    @Autowired
    HotelServicio hoteles;
    @Autowired
    VueloServicio vuelos;
    @Autowired
    TuristaServicio turistas;
    @Autowired
    UsuarioServicio usuarios;
    @Autowired
    DisponibilidadServicio disponibilidad;
    @Autowired
    SucursalRepositorio sucursalRepo;
    @Autowired
    TuristaRepositorio turistaRepo;
    @Autowired
    HotelRepositorio hotelRepo;
    @Autowired
    VueloRepositorio vueloRepo;
    @Autowired
    ReservaRepositorio reservaRepo;
    @Autowired
    PlatformTransactionManager transacciones;
    @Autowired
    JdbcTemplate jdbc;

    record Datos(Integer sucursal, Integer titular, Integer familiar, Integer hotel, Integer vuelo) {
    }

    private Datos preparar(final int capacidadHotel, final int capacidadVuelo) {
        final int n = SECUENCIA.incrementAndGet();
        return new TransactionTemplate(transacciones).execute(new TransactionCallback<Datos>() {
            @Override
            public Datos doInTransaction(TransactionStatus estado) {
                Sucursal sucursal = sucursalRepo.save(new Sucursal("Sucursal " + n, "123"));
                Turista titular = turistaRepo.save(new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Calle",
                        n + "a@ejemplo.com", "1", "2", sucursal));
                Turista familiar = turistaRepo.save(new Turista(DnisPrueba.siguiente(), "Luis", "Perez", "Calle",
                        n + "b@ejemplo.com", "1", "2", sucursal, titular));
                Hotel hotel = hotelRepo.save(new Hotel("Hotel " + n, "Calle", "Cordoba", "1", capacidadHotel));
                Vuelo vuelo = vueloRepo.save(
                        new Vuelo(n, DIA.atTime(10, 0), "Buenos Aires", "Cordoba", capacidadVuelo, capacidadVuelo, 0));
                return new Datos(sucursal.getCodigo(), titular.getCodigo(), familiar.getCodigo(), hotel.getCodigo(),
                        vuelo.getNumero());
            }
        });
    }

    private Reserva reservar(Datos d, Integer turista) {
        return reservas.crear(admin, turista, d.vuelo(), d.hotel(), ClaseVuelo.TURISTA,
                TipoHospedaje.MEDIA_PENSION, DIA, DIA.plusDays(4));
    }

    @Test
    void conservaHoraLocalDelVueloTantoEnSqlComoEnJava() {
        Datos d = preparar(2, 10);
        String fechaSql = jdbc.queryForObject("SELECT fecha_hora FROM vuelos WHERE numero = ?", String.class,
                d.vuelo());
        assertTrue(fechaSql.startsWith("2027-02-01 10:00:00"), fechaSql);
        assertEquals(DIA.atTime(10, 0), vueloRepo.findById(d.vuelo()).get().getFechaYHora());
    }

    @Test
    void cuentaOcupacionSimultaneaYPermiteEditarSinContarLaPropiaReserva() {
        final Datos d = preparar(2, 10);
        new TransactionTemplate(transacciones).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus estado) {
                Hotel hotel = hotelRepo.findById(d.hotel()).get();
                Vuelo vuelo = vueloRepo.findById(d.vuelo()).get();
                reservaRepo.save(new Reserva(turistaRepo.findById(d.titular()).get(), vuelo, hotel,
                        ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA, DIA.plusDays(2)));
                Vuelo otro = vueloRepo.save(new Vuelo(SECUENCIA.incrementAndGet(), DIA.plusDays(2).atTime(10, 0),
                        "Buenos Aires", "Cordoba", 10, 10, 0));
                reservaRepo.save(new Reserva(turistaRepo.findById(d.familiar()).get(), otro, hotel,
                        ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA.plusDays(2), DIA.plusDays(4)));
            }
        });
        assertEquals(1, disponibilidad.plazasDisponiblesHotel(d.hotel(), DIA, DIA.plusDays(4)));
        Reserva nueva = reservar(d, d.familiar());
        assertEquals(0, disponibilidad.plazasDisponiblesHotel(d.hotel(), DIA, DIA.plusDays(4)));
        reservas.modificar(admin, nueva.getCodigo(), d.familiar(), d.vuelo(), d.hotel(),
                ClaseVuelo.TURISTA, TipoHospedaje.PENSION_COMPLETA, DIA, DIA.plusDays(4));
        assertEquals(2, disponibilidad.plazasDisponiblesHotel(d.hotel(), DIA.plusDays(4), DIA.plusDays(6)));
    }

    @Test
    void impideCambiosIncompatiblesPeroPermiteCambiosInocuos() {
        Datos d = preparar(5, 10);
        reservar(d, d.titular());
        try {
            hoteles.modificar(admin, d.hotel(), "Otro", "Calle", "Mendoza", "1", 5, 25000);
            fail("Se esperaba OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            vuelos.modificar(admin, d.vuelo(),
                    DIA.plusDays(1).atTime(10, 0), "Buenos Aires", "Cordoba", 10, 10, 0, 150000);
            fail("Se esperaba OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            vuelos.modificar(admin, d.vuelo(), DIA.atTime(10, 0), "Buenos Aires", "Mendoza", 10, 10, 0, 150000);
            fail("Se esperaba OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException esperada) {
            // Es la excepcion que se esperaba.
        }
        hoteles.modificar(admin, d.hotel(), "Renombrado " + d.hotel(), "Nueva direccion", "Cordoba", "2", 5, 25000);
        vuelos.modificar(admin, d.vuelo(), DIA.atTime(12, 0), "Buenos Aires", "Cordoba", 10, 10, 0, 150000);
        assertEquals("Cordoba", hotelRepo.findById(d.hotel()).get().getCiudad());
        assertEquals("Renombrado " + d.hotel(), hotelRepo.findById(d.hotel()).get().getNombre());
        assertEquals(DIA.atTime(12, 0), vueloRepo.findById(d.vuelo()).get().getFechaYHora());
    }

    @Test
    void unErrorAlModificarHotelRevierteInclusoLosCamposYaAsignados() {
        Datos d = preparar(5, 10);
        Hotel original = hotelRepo.findById(d.hotel()).get();
        try {
            hoteles.modificar(
                    admin, d.hotel(), "Nombre cambiado", "Otra direccion", "Cordoba", " ", 8, 25000);
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
        Hotel guardado = hotelRepo.findById(d.hotel()).get();
        assertEquals(original.getNombre(), guardado.getNombre());
        assertEquals(original.getDireccion(), guardado.getDireccion());
        assertEquals(5, guardado.getCapacidadTotal());
    }

    @Test
    void listaElTitularYFamiliaresSinIncluirOtroGrupo() {
        Datos d = preparar(5, 10);
        preparar(5, 10);
        Cliente cliente = new Cliente("cliente", "hash", turistaRepo.findById(d.titular()).get());
        Set<Integer> codigos = new HashSet<>();
        for (Turista turista : turistas.listarPara(cliente, null, null)) {
            codigos.add(turista.getCodigo());
        }
        Set<Integer> esperados = new HashSet<>();
        esperados.add(d.titular());
        esperados.add(d.familiar());
        assertEquals(esperados, codigos);
    }

    @Test
    void elCorreoNormalizadoNoPuedeDuplicarseEnAltasNiModificaciones() {
        Datos d = preparar(5, 10);
        String email = turistaRepo.findById(d.titular()).get().getEmail();
        try {
            turistas.crearTitular(
                    admin, DnisPrueba.siguiente(), "Otra", "Persona", "Calle", " " + email.toUpperCase(Locale.ROOT) + " ",
                    "1", "2", d.sucursal());
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
        String emailFamiliar = turistaRepo.findById(d.familiar()).get().getEmail();
        try {
            turistas.modificar(admin, d.familiar(), DnisPrueba.siguiente(), "Luis", "Perez", "Calle", " " + email + " ", "1", "2", d.sucursal());
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
        assertEquals(emailFamiliar, turistaRepo.findById(d.familiar()).get().getEmail());
    }

    @Test
    void trasladaFamiliaYConservaSucursalHistoricaInclusoAlEditarReserva() {
        Datos d = preparar(5, 10);
        Reserva anterior = reservar(d, d.familiar());
        Integer nueva = sucursalRepo.save(new Sucursal("Nueva " + d.hotel(), "1")).getCodigo();
        try {
            turistas.modificar(admin, d.familiar(), DnisPrueba.siguiente(), "Luis", "Perez",
                    "Calle", d.hotel() + "f@ejemplo.com", "1", "2", nueva);
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
        turistas.modificar(admin, d.titular(), DnisPrueba.siguiente(), "Ana", "Perez", "Calle", d.hotel() + "t@ejemplo.com", "1", "2", nueva);
        assertEquals(nueva, turistaRepo.findById(d.familiar()).get().getSucursalContratacion().getCodigo());
        reservas.modificar(admin, anterior.getCodigo(), d.familiar(), d.vuelo(), d.hotel(),
                ClaseVuelo.TURISTA, TipoHospedaje.PENSION_COMPLETA, DIA, DIA.plusDays(3));
        assertEquals(d.sucursal(),
                reservaRepo.findById(anterior.getCodigo()).get().getSucursalContratacion().getCodigo());
        assertEquals(nueva, reservar(d, d.titular()).getSucursalContratacion().getCodigo());
        try {
            usuarios.crear(admin, "invalido", "clave", RolUsuario.VENDEDOR, d.titular(), null);
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @Test
    void noVendeDosVecesLaUltimaPlazaDeVuelo() throws Exception {
        comprobarUltimaPlaza(preparar(10, 1));
    }

    @Test
    void noVendeDosVecesLaUltimaPlazaDeHotel() throws Exception {
        comprobarUltimaPlaza(preparar(1, 10));
    }

    @Test
    void reducirCapacidadEsperaLaReservaPendienteYRevalidaDespuesDelCommit() throws Exception {
        final Datos d = preparar(1, 10);
        final CountDownLatch guardada = new CountDownLatch(1);
        final CountDownLatch confirmar = new CountDownLatch(1);
        final CountDownLatch modificacionIniciada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<?> primera = hilos.submit(new ReservaQueEspera(d, guardada, confirmar));
            assertTrue(guardada.await(10, TimeUnit.SECONDS));
            Future<?> modificacion = hilos.submit(new Runnable() {
                @Override
                public void run() {
                    modificacionIniciada.countDown();
                    hoteles.modificar(admin, d.hotel(), "Hotel reducido " + d.hotel(), "Calle", "Cordoba", "1", 0,
                            25000);
                }
            });
            assertTrue(modificacionIniciada.await(10, TimeUnit.SECONDS));
            try {
                modificacion.get(200, TimeUnit.MILLISECONDS);
                fail("Se esperaba TimeoutException");
            } catch (TimeoutException esperada) {
                // Es la excepcion que se esperaba.
            }
            confirmar.countDown();
            primera.get(10, TimeUnit.SECONDS);
            ExecutionException fallo = null;
            try {
                modificacion.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                fallo = e;
            }
            assertNotNull(fallo, "La modificacion tenia que fallar");
            assertInstanceOf(CapacidadExcedidaException.class, fallo.getCause());
            assertEquals(1, hotelRepo.findById(d.hotel()).get().getCapacidadTotal());
        } finally {
            confirmar.countDown();
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void comprobarUltimaPlaza(final Datos d) throws Exception {
        final CountDownLatch guardada = new CountDownLatch(1);
        final CountDownLatch confirmar = new CountDownLatch(1);
        final CountDownLatch segundaIniciada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<?> primera = hilos.submit(new ReservaQueEspera(d, guardada, confirmar));
            assertTrue(guardada.await(10, TimeUnit.SECONDS));
            Future<?> segunda = hilos.submit(new Runnable() {
                @Override
                public void run() {
                    segundaIniciada.countDown();
                    reservar(d, d.familiar());
                }
            });
            assertTrue(segundaIniciada.await(10, TimeUnit.SECONDS));
            try {
                segunda.get(200, TimeUnit.MILLISECONDS);
                fail("Se esperaba TimeoutException");
            } catch (TimeoutException esperada) {
                // Es la excepcion que se esperaba.
            }
            confirmar.countDown();
            primera.get(10, TimeUnit.SECONDS);
            ExecutionException fallo = null;
            try {
                segunda.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                fallo = e;
            }
            assertNotNull(fallo, "La segunda reserva tenia que fallar");
            assertInstanceOf(CapacidadExcedidaException.class, fallo.getCause());
            assertEquals(1, reservaRepo.countByVueloNumeroAndClaseVuelo(d.vuelo(), ClaseVuelo.TURISTA));
        } finally {
            confirmar.countDown();
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS))
                throw new IllegalStateException("Espera agotada");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Reserva para el titular dentro de una transaccion y no confirma hasta que
     * se libera el latch "confirmar". Sirve para probar dos operaciones a la vez.
     */
    private class ReservaQueEspera implements Runnable {
        private final Datos datos;
        private final CountDownLatch guardada;
        private final CountDownLatch confirmar;

        ReservaQueEspera(Datos datos, CountDownLatch guardada, CountDownLatch confirmar) {
            this.datos = datos;
            this.guardada = guardada;
            this.confirmar = confirmar;
        }

        @Override
        public void run() {
            new TransactionTemplate(transacciones).execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus estado) {
                    reservar(datos, datos.titular());
                    guardada.countDown();
                    esperar(confirmar);
                }
            });
        }
    }
}
