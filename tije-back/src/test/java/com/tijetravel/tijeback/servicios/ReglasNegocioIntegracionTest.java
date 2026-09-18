package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.tijetravel.tijeback.enums.*;
import com.tijetravel.tijeback.excepciones.*;
import com.tijetravel.tijeback.modelos.*;
import com.tijetravel.tijeback.repositorios.*;

@SpringBootTest(properties = "spring.datasource.url=${TEST_DB_URL_REGLAS:jdbc:h2:mem:reglas-nuevas;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000}")
@ActiveProfiles("test")
class ReglasNegocioIntegracionTest {
    private static final AtomicInteger SECUENCIA = new AtomicInteger(5000);
    private static final LocalDate DIA = LocalDate.of(2027, 2, 1);
    private final Usuario admin = new Administrador("operador", "hash");

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
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    record Datos(Integer sucursal, Integer titular, Integer familiar, Integer hotel, Integer vuelo) {
    }

    private Datos preparar(int capacidadHotel, int capacidadVuelo) {
        int n = SECUENCIA.incrementAndGet();
        return new TransactionTemplate(transacciones).execute(tx -> {
            Sucursal sucursal = sucursalRepo.save(new Sucursal("Sucursal " + n, "123"));
            Turista titular = turistaRepo
                    .save(new Turista("Ana", "Perez", "Calle", n + "a@ejemplo.com", "1", "2", sucursal));
            Turista familiar = turistaRepo
                    .save(new Turista("Luis", "Perez", "Calle", n + "b@ejemplo.com", "1", "2", sucursal, titular));
            Hotel hotel = hotelRepo.save(new Hotel("Hotel " + n, "Calle", "Cordoba", "1", capacidadHotel));
            Vuelo vuelo = vueloRepo.save(
                    new Vuelo(n, DIA.atTime(10, 0), "Buenos Aires", "Cordoba", capacidadVuelo, capacidadVuelo, 0));
            return new Datos(sucursal.getCodigo(), titular.getCodigo(), familiar.getCodigo(), hotel.getCodigo(),
                    vuelo.getNumero());
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
        assertEquals(DIA.atTime(10, 0), vueloRepo.findById(d.vuelo()).orElseThrow().getFechaYHora());
    }

    @Test
    void cuentaOcupacionSimultaneaYPermiteEditarSinContarLaPropiaReserva() {
        Datos d = preparar(2, 10);
        new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
            Hotel hotel = hotelRepo.findById(d.hotel()).orElseThrow();
            Vuelo vuelo = vueloRepo.findById(d.vuelo()).orElseThrow();
            reservaRepo.save(new Reserva(turistaRepo.findById(d.titular()).orElseThrow(), vuelo, hotel,
                    ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA, DIA.plusDays(2)));
            Vuelo otro = vueloRepo.save(new Vuelo(SECUENCIA.incrementAndGet(), DIA.plusDays(2).atTime(10, 0),
                    "Buenos Aires", "Cordoba", 10, 10, 0));
            reservaRepo.save(new Reserva(turistaRepo.findById(d.familiar()).orElseThrow(), otro, hotel,
                    ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA.plusDays(2), DIA.plusDays(4)));
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
        assertThrows(OperacionNoPermitidaException.class,
                () -> hoteles.modificar(admin, d.hotel(), "Otro", "Calle", "Mendoza", "1", 5));
        assertThrows(OperacionNoPermitidaException.class, () -> vuelos.modificar(admin, d.vuelo(),
                DIA.plusDays(1).atTime(10, 0), "Buenos Aires", "Cordoba", 10, 10, 0));
        assertThrows(OperacionNoPermitidaException.class,
                () -> vuelos.modificar(admin, d.vuelo(), DIA.atTime(10, 0), "Buenos Aires", "Mendoza", 10, 10, 0));
        hoteles.modificar(admin, d.hotel(), "Renombrado " + d.hotel(), "Nueva direccion", "Cordoba", "2", 5);
        vuelos.modificar(admin, d.vuelo(), DIA.atTime(12, 0), "Buenos Aires", "Cordoba", 10, 10, 0);
        assertEquals("Cordoba", hotelRepo.findById(d.hotel()).orElseThrow().getCiudad());
        assertEquals("Renombrado " + d.hotel(), hotelRepo.findById(d.hotel()).orElseThrow().getNombre());
        assertEquals(DIA.atTime(12, 0), vueloRepo.findById(d.vuelo()).orElseThrow().getFechaYHora());
    }

    @Test
    void unErrorAlModificarHotelRevierteInclusoLosCamposYaAsignados() {
        Datos d = preparar(5, 10);
        Hotel original = hotelRepo.findById(d.hotel()).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> hoteles.modificar(
                admin, d.hotel(), "Nombre cambiado", "Otra direccion", "Cordoba", " ", 8));
        Hotel guardado = hotelRepo.findById(d.hotel()).orElseThrow();
        assertEquals(original.getNombre(), guardado.getNombre());
        assertEquals(original.getDireccion(), guardado.getDireccion());
        assertEquals(5, guardado.getCapacidadTotal());
    }

    @SuppressWarnings("null") // listarPara devuelve turistas no nulos.
    @Test
    void listaElTitularYFamiliaresSinIncluirOtroGrupo() {
        Datos d = preparar(5, 10);
        preparar(5, 10);
        Cliente cliente = new Cliente("cliente", "hash", turistaRepo.findById(d.titular()).orElseThrow());
        var codigos = turistas.listarPara(cliente).stream().map(Turista::getCodigo)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(java.util.Set.of(d.titular(), d.familiar()), codigos);
    }

    @Test
    void elCorreoNormalizadoNoPuedeDuplicarseEnAltasNiModificaciones() {
        Datos d = preparar(5, 10);
        String email = turistaRepo.findById(d.titular()).orElseThrow().getEmail();
        assertThrows(EntidadDuplicadaException.class, () -> turistas.crearTitular(
                admin, "Otra", "Persona", "Calle", " " + email.toUpperCase(java.util.Locale.ROOT) + " ",
                "1", "2", d.sucursal()));
        String emailFamiliar = turistaRepo.findById(d.familiar()).orElseThrow().getEmail();
        assertThrows(EntidadDuplicadaException.class, () -> turistas.modificar(
                admin, d.familiar(), "Luis", "Perez", "Calle", " " + email + " ", "1", "2", d.sucursal()));
        assertEquals(emailFamiliar, turistaRepo.findById(d.familiar()).orElseThrow().getEmail());
    }

    @Test
    void trasladaFamiliaYConservaSucursalHistoricaInclusoAlEditarReserva() {
        Datos d = preparar(5, 10);
        Reserva anterior = reservar(d, d.familiar());
        Integer nueva = new TransactionTemplate(transacciones)
                .execute(tx -> sucursalRepo.save(new Sucursal("Nueva " + d.hotel(), "1")).getCodigo());
        assertThrows(IllegalArgumentException.class, () -> turistas.modificar(admin, d.familiar(), "Luis", "Perez",
                "Calle", d.hotel() + "f@ejemplo.com", "1", "2", nueva));
        turistas.modificar(admin, d.titular(), "Ana", "Perez", "Calle", d.hotel() + "t@ejemplo.com", "1", "2", nueva);
        assertEquals(nueva, turistaRepo.findById(d.familiar()).orElseThrow().getSucursalContratacion().getCodigo());
        reservas.modificar(admin, anterior.getCodigo(), d.familiar(), d.vuelo(), d.hotel(),
                ClaseVuelo.TURISTA, TipoHospedaje.PENSION_COMPLETA, DIA, DIA.plusDays(3));
        assertEquals(d.sucursal(),
                reservaRepo.findById(anterior.getCodigo()).orElseThrow().getSucursalContratacion().getCodigo());
        assertEquals(nueva, reservar(d, d.titular()).getSucursalContratacion().getCodigo());
        assertThrows(IllegalArgumentException.class,
                () -> usuarios.crear(admin, "invalido", "clave", RolUsuario.VENDEDOR, d.titular()));
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
        Datos d = preparar(1, 10);
        CountDownLatch guardada = new CountDownLatch(1);
        CountDownLatch confirmar = new CountDownLatch(1);
        CountDownLatch modificacionIniciada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<?> primera = hilos.submit(() -> new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                reservar(d, d.titular());
                guardada.countDown();
                esperar(confirmar);
            }));
            assertTrue(guardada.await(10, TimeUnit.SECONDS));
            Future<?> modificacion = hilos.submit(() -> {
                modificacionIniciada.countDown();
                hoteles.modificar(admin, d.hotel(), "Hotel reducido " + d.hotel(), "Calle", "Cordoba", "1", 0);
            });
            assertTrue(modificacionIniciada.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> modificacion.get(200, TimeUnit.MILLISECONDS));
            confirmar.countDown();
            primera.get(10, TimeUnit.SECONDS);
            ExecutionException fallo = assertThrows(ExecutionException.class,
                    () -> modificacion.get(10, TimeUnit.SECONDS));
            assertInstanceOf(CapacidadExcedidaException.class, fallo.getCause());
            assertEquals(1, hotelRepo.findById(d.hotel()).orElseThrow().getCapacidadTotal());
        } finally {
            confirmar.countDown();
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void comprobarUltimaPlaza(Datos d) throws Exception {
        CountDownLatch guardada = new CountDownLatch(1);
        CountDownLatch confirmar = new CountDownLatch(1);
        CountDownLatch segundaIniciada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<?> primera = hilos.submit(() -> new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                reservar(d, d.titular());
                guardada.countDown();
                esperar(confirmar);
            }));
            assertTrue(guardada.await(10, TimeUnit.SECONDS));
            Future<?> segunda = hilos.submit(() -> {
                segundaIniciada.countDown();
                reservar(d, d.familiar());
            });
            assertTrue(segundaIniciada.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> segunda.get(200, TimeUnit.MILLISECONDS));
            confirmar.countDown();
            primera.get(10, TimeUnit.SECONDS);
            ExecutionException fallo = assertThrows(ExecutionException.class, () -> segunda.get(10, TimeUnit.SECONDS));
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
}
