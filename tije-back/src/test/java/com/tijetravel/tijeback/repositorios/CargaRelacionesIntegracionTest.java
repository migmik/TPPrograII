package com.tijetravel.tijeback.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

import jakarta.persistence.EntityManager;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:carga-relaciones;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@ActiveProfiles("test")
@Transactional
class CargaRelacionesIntegracionTest {
    private static final LocalDate DIA = LocalDate.of(2027, 2, 1);

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ReservaRepositorio reservas;
    @Autowired
    private UsuarioRepositorio usuarios;

    private Statistics estadisticas;
    private Integer codigoHotel;
    private Integer numeroVuelo;
    private Integer codigoReserva;
    private Integer codigoFamiliar;
    private Integer codigoTitular;
    private final List<Integer> codigosUsuarios = new ArrayList<>();

    @BeforeEach
    void prepararRelacionesDistintasParaDetectarNMasUno() {
        Sucursal sucursal = guardar(new Sucursal("Sucursal consultas", "123"));
        Hotel hotelComun = guardar(new Hotel("Hotel comun", "Calle", "Cordoba", "123", 20));
        Vuelo vueloComun = guardar(new Vuelo(9000, DIA.atTime(10, 0), "Buenos Aires", "Cordoba", 20, 20, 0));
        codigoHotel = hotelComun.getCodigo();
        numeroVuelo = vueloComun.getNumero();
        for (int i = 1; i <= 3; i++) {
            Turista titular = guardar(new Turista("Titular " + i, "Perez", "Calle",
                    "titular" + i + "@ejemplo.com", "1", "2", sucursal));
            Turista familiar = guardar(new Turista("Familiar " + i, "Perez", "Calle",
                    "familiar" + i + "@ejemplo.com", "1", "2", sucursal, titular));
            codigosUsuarios.add(guardar(new Cliente("cliente" + i, "hash", titular)).getCodigo());
            Vuelo vuelo = guardar(new Vuelo(9000 + i, DIA.atTime(10, 0), "Buenos Aires", "Cordoba", 20, 20, 0));
            Hotel hotel = guardar(new Hotel("Hotel " + i, "Calle", "Cordoba", "123", 20));
            Reserva reserva = guardar(new Reserva(familiar, vuelo, hotelComun,
                    ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA, DIA.plusDays(2)));
            guardar(new Reserva(familiar, vueloComun, hotel,
                    ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION, DIA, DIA.plusDays(2)));
            if (i == 1) {
                codigoReserva = reserva.getCodigo();
                codigoFamiliar = familiar.getCodigo();
                codigoTitular = titular.getCodigo();
            }
        }
        codigosUsuarios.add(guardar(new Administrador("administrador", "hash")).getCodigo());
        codigosUsuarios.add(guardar(new Vendedor("vendedor", "hash")).getCodigo());

        // Excluir INSERTs y evitar que la cache de primer nivel oculte consultas
        // adicionales.
        entityManager.flush();
        entityManager.clear();
        estadisticas = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
    }

    @Test
    void consultaReservasDelHotelConSusVuelosEnUnSoloSelect() {
        List<Reserva> resultado = reservas.findByHotelCodigo(codigoHotel);

        assertEquals(3, resultado.size());
        resultado.forEach(reserva -> assertEquals("Cordoba", reserva.getVuelo().getDestino()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        resultado.forEach(reserva -> assertEquals("Cordoba", reserva.getVuelo().getDestino()));
    }

    @Test
    void consultaReservasDelVueloConSusHotelesEnUnSoloSelect() {
        List<Reserva> resultado = reservas.findByVueloNumero(numeroVuelo);

        assertEquals(3, resultado.size());
        resultado.forEach(reserva -> assertEquals("Cordoba", reserva.getHotel().getCiudad()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        resultado.forEach(reserva -> assertEquals("Cordoba", reserva.getHotel().getCiudad()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "todas", "codigo", "turista", "familia" })
    void cargaElGrafoCompletoSinConsultasAdicionales(String consulta) {
        List<Reserva> resultado = switch (consulta) {
            case "todas" -> reservas.findAll();
            case "codigo" -> List.of(reservas.findById(codigoReserva).orElseThrow());
            case "turista" -> reservas.findByTuristaCodigo(codigoFamiliar);
            case "familia" -> reservas.listarPorTitularYFamiliares(codigoTitular);
            default -> throw new IllegalArgumentException(consulta);
        };

        assertEquals(switch (consulta) {
            case "todas" -> 6;
            case "codigo" -> 1;
            default -> 2;
        }, resultado.size());
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        for (Reserva reserva : resultado) {
            assertTrue(reserva.getTurista().getNombre().startsWith("Familiar"));
            assertTrue(reserva.getTurista().getTitular().getNombre().startsWith("Titular"));
            assertEquals("Sucursal consultas", reserva.getSucursalContratacion().getDireccion());
            assertEquals("Cordoba", reserva.getVuelo().getDestino());
            assertEquals("Cordoba", reserva.getHotel().getCiudad());
        }
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void listaTodosLosRolesYCargaLosTuristasEnUnSoloSelect() {
        List<Usuario> resultado = usuarios.findAll();

        assertEquals(5, resultado.size());
        assertEquals(3, resultado.stream().filter(usuario -> usuario.getRol() == RolUsuario.CLIENTE).count());
        assertEquals(1, resultado.stream().filter(usuario -> usuario.getRol() == RolUsuario.ADMINISTRADOR).count());
        assertEquals(1, resultado.stream().filter(usuario -> usuario.getRol() == RolUsuario.VENDEDOR).count());
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        resultado.forEach(this::verificarTuristaFueraDelContexto);
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 3, 4 })
    void encuentraPorCodigoCualquierRolConSuTuristaCargado(int indice) {
        Usuario resultado = usuarios.findById(codigosUsuarios.get(indice)).orElseThrow();

        assertEquals(codigosUsuarios.get(indice), resultado.getCodigo());
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        verificarTuristaFueraDelContexto(resultado);
    }

    @ParameterizedTest
    @ValueSource(strings = { "CLIENTE1", "ADMINISTRADOR", "VENDEDOR" })
    void encuentraParaLoginSinDistinguirMayusculasYConTuristaCargado(String nombre) {
        Usuario resultado = usuarios.findByNombreUsuarioIgnoreCase(nombre).orElseThrow();

        assertTrue(nombre.equalsIgnoreCase(resultado.getNombreUsuario()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
        entityManager.clear();
        verificarTuristaFueraDelContexto(resultado);
    }

    @Test
    void devuelveVacioCuandoNoExisteElUsuario() {
        assertTrue(usuarios.findById(-1).isEmpty());
        assertTrue(usuarios.findByNombreUsuarioIgnoreCase("inexistente").isEmpty());
        assertEquals(2, estadisticas.getPrepareStatementCount());
    }

    private void verificarTuristaFueraDelContexto(Usuario usuario) {
        if (usuario.getRol() == RolUsuario.CLIENTE) {
            assertNotNull(usuario.getCodigoTurista());
            // Leer un atributo distinto del ID exige que el turista este realmente cargado.
            assertTrue(usuario.getTurista().getNombre().startsWith("Titular"));
        } else {
            assertNull(usuario.getCodigoTurista());
            assertNull(usuario.getTurista());
        }
    }

    private <T> T guardar(T entidad) {
        entityManager.persist(entidad);
        return entidad;
    }
}
