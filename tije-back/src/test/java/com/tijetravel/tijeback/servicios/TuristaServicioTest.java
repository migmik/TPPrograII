package com.tijetravel.tijeback.servicios;

import com.tijetravel.tijeback.DnisPrueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

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

    private TuristaServicio servicio;
    private Administrador administrador;
    private Turista turista;

    @BeforeEach
    void prepararServicio() {
        servicio = new TuristaServicio(
                turistaRepositorio,
                sucursalRepositorio,
                reservaRepositorio,
                usuarioRepositorio,
                new AutorizacionServicio(),
                org.mockito.Mockito.mock(BloqueoEscrituras.class));
        administrador = new Administrador("admin", "clave", com.tijetravel.tijeback.DnisPrueba.siguiente());
        turista = new Turista(DnisPrueba.siguiente(), "Ana",
                "Perez",
                "Direccion",
                "ana@example.com",
                "100",
                "200",
                new Sucursal("Av. Colon 100", "351-1000"));
    }

    @Test
    void noEliminaTuristaConReservas() {
            when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
        when(reservaRepositorio.existsByTuristaCodigo(1)).thenReturn(true);

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.eliminar(administrador, 1));

        verify(turistaRepositorio, never()).delete(any());
    }

    @Test
    void noEliminaTitularConFamiliares() {
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
        when(turistaRepositorio.existsByTitularCodigo(1)).thenReturn(true);

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.eliminar(administrador, 1));

        verify(turistaRepositorio, never()).delete(any());
    }

    @Test
    void noEliminaTuristaVinculadoAUnUsuario() {
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
        when(usuarioRepositorio.contarClientesPorTurista(1)).thenReturn(1L);

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.eliminar(administrador, 1));

        verify(turistaRepositorio, never()).delete(any());
    }

    @Test
    void eliminaTuristaSinRelaciones() {
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
        servicio.eliminar(administrador, 1);

        verify(turistaRepositorio).delete(turista);
    }

    @Test
    void buscaTuristaPorDniSinListarTodaLaTabla() {
        when(turistaRepositorio.findByDni(turista.getDni())).thenReturn(List.of(turista));

        assertEquals(List.of(turista), servicio.listarPara(administrador, turista.getDni()));

        verify(turistaRepositorio).findByDni(turista.getDni());
        verify(turistaRepositorio, never()).findAll();
    }

    @Test
    void filtraTitularesSinListarTodaLaTabla() {
        when(turistaRepositorio.findByTitularIsNull()).thenReturn(List.of(turista));

        assertEquals(List.of(turista), servicio.listarPara(administrador, null, true));

        verify(turistaRepositorio).findByTitularIsNull();
        verify(turistaRepositorio, never()).findAll();
    }
}
