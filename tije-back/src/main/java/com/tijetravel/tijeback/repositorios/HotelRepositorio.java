package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tijetravel.tijeback.modelos.Hotel;

public interface HotelRepositorio extends JpaRepository<Hotel, Integer> {

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCase(String nombre, String ciudad);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndCodigoNot(
            String nombre,
            String ciudad,
            Integer codigo);
}
