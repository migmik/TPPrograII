package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.RolesPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.impl.TuristaServicioImpl;

@ExtendWith(MockitoExtension.class)
class TuristaServicioTest {
    @Mock
    private TuristaRepositorio turistaRepositorio;

    @Mock
    private SucursalRepositorio sucursalRepositorio;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private DisponibilidadServicio disponibilidadServicio;

    private TuristaServicio servicio;
    private Empleado administrador;
    private Turista titular;
    private Turista familiar;

    @BeforeEach
    void prepararServicio() {
        servicio = new TuristaServicioImpl(
                turistaRepositorio,
                sucursalRepositorio,
                reservaRepositorio,
                usuarioRepositorio,
                new AutorizacionServicio(),
                org.mockito.Mockito.mock(BloqueoEscrituras.class),
                disponibilidadServicio);
        administrador = new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
        Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
        titular = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Direccion", "ana@example.com",
                "100", "200", sucursal);
        ReflectionTestUtils.setField(titular, "codigo", 1);
        familiar = new Turista(DnisPrueba.siguiente(), "Luis", "Perez", "Direccion", "luis@example.com",
                "100", "300", sucursal, titular);
        ReflectionTestUtils.setField(familiar, "codigo", 2);
    }

    private Reserva reservaDe(Turista turista) {
        Vuelo vuelo = new Vuelo(100, LocalDateTime.of(2026, 12, 1, 10, 0), "Buenos Aires", "Cordoba", 20, 15, 5);
        Hotel hotel = new Hotel("Hotel Centro", "Calle 2", "Cordoba", "351-5000", 10);
        return new Reserva(turista, vuelo, hotel, ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
    }

    @Test
    void laBajaDelTitularArrastraAlGrupoSusReservasPendientesYSuCuenta() {
        Cliente cuenta = new Cliente("ana", "clave", titular, RolesPrueba.cliente());
        Reserva pendienteTitular = reservaDe(titular);
        Reserva pendienteFamiliar = reservaDe(familiar);
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));
        when(turistaRepositorio.findByTitularCodigoAndFechaBajaIsNull(1)).thenReturn(List.of(familiar));
        when(usuarioRepositorio.buscarClientesDeTurista(1)).thenReturn(List.of(cuenta));
        when(reservaRepositorio.findByTuristaCodigoAndPagadaFalseAndFechaBajaIsNull(1))
                .thenReturn(List.of(pendienteTitular));
        when(reservaRepositorio.findByTuristaCodigoAndPagadaFalseAndFechaBajaIsNull(2))
                .thenReturn(List.of(pendienteFamiliar));

        servicio.darDeBaja(administrador, 1);

        assertFalse(titular.estaActivo());
        assertFalse(familiar.estaActivo());
        assertFalse(cuenta.estaActivo());
        assertFalse(pendienteTitular.estaActivo());
        assertFalse(pendienteFamiliar.estaActivo());
        // Todo el grupo comparte la fecha de baja para poder reactivarlo junto.
        assertTrue(familiar.seDioDeBajaJuntoCon(titular));
        assertTrue(cuenta.seDioDeBajaJuntoCon(titular));
        verify(turistaRepositorio, never()).delete(titular);
    }

    @Test
    void laBajaDeUnFamiliarNoTocaAlTitularNiSuCuenta() {
        when(turistaRepositorio.findById(2)).thenReturn(Optional.of(familiar));

        servicio.darDeBaja(administrador, 2);

        assertFalse(familiar.estaActivo());
        assertTrue(titular.estaActivo());
        verify(usuarioRepositorio, never()).buscarClientesDeTurista(1);
    }

    @Test
    void noDaDeBajaUnTuristaQueYaEstaDadoDeBaja() {
        titular.darDeBaja(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));

        try {
            servicio.darDeBaja(administrador, 1);
            fail("Se esperaba EntidadNoEncontradaException");
        } catch (com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException esperada) {
            // Para la aplicacion un turista dado de baja no existe.
        }
    }

    @Test
    void reactivarAlTitularDevuelveSoloLoQueSeDioDeBajaConEl() {
        LocalDateTime fechaGrupo = LocalDateTime.of(2026, 5, 10, 12, 0);
        Turista familiarAnterior = new Turista(DnisPrueba.siguiente(), "Eva", "Perez", "Direccion",
                "eva@example.com", "100", "400", titular.getSucursalContratacion(), titular);
        ReflectionTestUtils.setField(familiarAnterior, "codigo", 3);
        familiarAnterior.darDeBaja(LocalDateTime.of(2026, 1, 1, 9, 0));
        titular.darDeBaja(fechaGrupo);
        familiar.darDeBaja(fechaGrupo);
        Reserva reserva = reservaDe(familiar);
        reserva.darDeBaja(fechaGrupo);

        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));
        when(turistaRepositorio.findByTitularCodigo(1)).thenReturn(List.of(familiar, familiarAnterior));
        when(usuarioRepositorio.buscarClientesDeTurista(1)).thenReturn(new ArrayList<Cliente>());
        when(reservaRepositorio.findByTuristaCodigo(1)).thenReturn(new ArrayList<Reserva>());
        when(reservaRepositorio.findByTuristaCodigo(2)).thenReturn(List.of(reserva));
        when(disponibilidadServicio.plazasDisponiblesVuelo(100, ClaseVuelo.TURISTA)).thenReturn(5);
        when(disponibilidadServicio.plazasDisponiblesHotel(reserva.getHotel(), reserva.getFechaLlegada(),
                reserva.getFechaPartida())).thenReturn(5);

        Turista reactivado = servicio.reactivar(administrador, 1);

        assertNotNull(reactivado);
        assertTrue(titular.estaActivo());
        assertTrue(familiar.estaActivo());
        assertTrue(reserva.estaActivo());
        assertFalse(familiarAnterior.estaActivo());
    }

    @Test
    void unaReservaSinLugarNoVuelveAlReactivarElGrupo() {
        LocalDateTime fechaGrupo = LocalDateTime.of(2026, 5, 10, 12, 0);
        titular.darDeBaja(fechaGrupo);
        Reserva reserva = reservaDe(titular);
        reserva.darDeBaja(fechaGrupo);
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));
        when(turistaRepositorio.findByTitularCodigo(1)).thenReturn(new ArrayList<Turista>());
        when(usuarioRepositorio.buscarClientesDeTurista(1)).thenReturn(new ArrayList<Cliente>());
        when(reservaRepositorio.findByTuristaCodigo(1)).thenReturn(List.of(reserva));
        when(disponibilidadServicio.plazasDisponiblesVuelo(100, ClaseVuelo.TURISTA)).thenReturn(0);
        when(disponibilidadServicio.plazasDisponiblesHotel(reserva.getHotel(), reserva.getFechaLlegada(),
                reserva.getFechaPartida())).thenReturn(5);

        servicio.reactivar(administrador, 1);

        assertTrue(titular.estaActivo());
        assertFalse(reserva.estaActivo());
    }

    @Test
    void noReactivaUnFamiliarSiSuTitularSigueDadoDeBaja() {
        titular.darDeBaja(LocalDateTime.of(2026, 5, 10, 12, 0));
        familiar.darDeBaja(LocalDateTime.of(2026, 5, 10, 12, 0));
        when(turistaRepositorio.findById(2)).thenReturn(Optional.of(familiar));

        try {
            servicio.reactivar(administrador, 2);
            fail("Se esperaba OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException esperada) {
            // Es la excepcion que se esperaba.
        }
        assertFalse(familiar.estaActivo());
    }

    @Test
    void buscaTuristaPorDniSinListarTodaLaTabla() {
        when(turistaRepositorio.findByDniAndFechaBajaIsNull(titular.getDni())).thenReturn(List.of(titular));

        assertEquals(List.of(titular), servicio.listarPara(administrador, titular.getDni(), null));

        verify(turistaRepositorio).findByDniAndFechaBajaIsNull(titular.getDni());
        verify(turistaRepositorio, never()).findAll();
    }

    @Test
    void filtraTitularesSinListarTodaLaTabla() {
        when(turistaRepositorio.findByTitularIsNullAndFechaBajaIsNullOrderByCodigo()).thenReturn(List.of(titular));

        assertEquals(List.of(titular), servicio.listarPara(administrador, null, true));

        verify(turistaRepositorio).findByTitularIsNullAndFechaBajaIsNullOrderByCodigo();
        verify(turistaRepositorio, never()).findAll();
    }
}
