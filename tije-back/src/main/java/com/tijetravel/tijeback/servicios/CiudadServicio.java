package com.tijetravel.tijeback.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class CiudadServicio {
    private final HotelRepositorio hotelRepositorio;
    private final VueloRepositorio vueloRepositorio;

    public CiudadServicio(HotelRepositorio hotelRepositorio, VueloRepositorio vueloRepositorio) {
        this.hotelRepositorio = hotelRepositorio;
        this.vueloRepositorio = vueloRepositorio;
    }

    /** Ciudades de hoteles y vuelos sin repetir, ordenadas alfabeticamente. */
    public List<String> listarDisponibles() {
        // La clave normalizada evita repetir "Cordoba" y "Córdoba".
        Map<String, String> ciudades = new TreeMap<>();
        agregar(ciudades, hotelRepositorio.listarCiudades());
        agregar(ciudades, vueloRepositorio.listarOrigenes());
        agregar(ciudades, vueloRepositorio.listarDestinos());
        return new ArrayList<>(ciudades.values());
    }

    private void agregar(Map<String, String> ciudades, List<String> nombres) {
        for (String ciudad : nombres) {
            if (ciudad == null || ciudad.isBlank()) {
                continue;
            }
            String clave = CiudadConsulta.normalizar(ciudad);
            if (!ciudades.containsKey(clave)) {
                ciudades.put(clave, ciudad.trim());
            }
        }
    }
}
