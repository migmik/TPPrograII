package com.tijetravel.tijeback.repositorios;

import com.tijetravel.tijeback.modelos.Hotel;

public interface HotelRepositorio extends GenericoRepositorio<Hotel, Integer> {

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCase(String nombre, String ciudad);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndCodigoNot(
            String nombre,
            String ciudad,
            Integer codigo);
}
