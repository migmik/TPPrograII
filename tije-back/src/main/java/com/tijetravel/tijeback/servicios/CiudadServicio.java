package com.tijetravel.tijeback.servicios;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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

    public List<String> listarDisponibles() {
        Map<String, String> ciudades = new TreeMap<>();
        Stream.of(
                hotelRepositorio.listarCiudades(),
                vueloRepositorio.listarOrigenes(),
                vueloRepositorio.listarDestinos())
                .flatMap(List::stream)
                .filter(ciudad -> ciudad != null && !ciudad.isBlank())
                .forEach(ciudad -> ciudades.putIfAbsent(normalizar(ciudad), ciudad.trim()));
        return List.copyOf(ciudades.values());
    }

    private String normalizar(String ciudad) {
        return Normalizer.normalize(ciudad.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}