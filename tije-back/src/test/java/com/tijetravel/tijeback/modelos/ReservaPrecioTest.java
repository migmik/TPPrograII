package com.tijetravel.tijeback.modelos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;

class ReservaPrecioTest {
    @Test
    void aplicaRecargosPorClaseYPensionYNoCambiaUnPagoConfirmado() {
        Turista turista = mock(Turista.class);
        when(turista.getSucursalContratacion()).thenReturn(mock(Sucursal.class));
        Vuelo vuelo = mock(Vuelo.class);
        when(vuelo.getPrecio()).thenReturn(200000);
        Hotel hotel = mock(Hotel.class);
        when(hotel.getPrecioNochePersona()).thenReturn(40000);
        LocalDate llegada = LocalDate.of(2027, 1, 10);
        LocalDate partida = llegada.plusDays(3);

        Reserva reserva = new Reserva(turista, vuelo, hotel, ClaseVuelo.PRIMERA,
                TipoHospedaje.PENSION_COMPLETA, llegada, partida);
        assertEquals(220000, reserva.getPrecioVuelo());
        assertEquals(126000, reserva.getPrecioHotel());
        assertEquals(346000, reserva.getTotal());

        reserva.confirmarPago();
        var primeraFecha = reserva.getFechaPago();
        reserva.confirmarPago();
        assertTrue(reserva.isPagada());
        assertEquals(primeraFecha, reserva.getFechaPago());
        assertThrows(IllegalStateException.class, () -> reserva.actualizarDatos(turista, vuelo, hotel,
                ClaseVuelo.PRIMERA, TipoHospedaje.PENSION_COMPLETA, llegada, partida));
    }
}
