package com.tijetravel.tijeback.servicios.impl;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Compara nombres de ciudades sin tener en cuenta mayusculas ni acentos. */
final class CiudadConsulta {
    private CiudadConsulta() {
    }

    /** Nombres posibles de la ciudad tal como pueden estar guardados en la base. */
    static List<String> nombres(String ciudad) {
        String normalizada = normalizar(ciudad);
        List<String> nombres = new ArrayList<>();
        nombres.add(normalizada);
        if (normalizada.equals("cordoba")) {
            nombres.add("córdoba");
        }
        return nombres;
    }

    static boolean misma(String primera, String segunda) {
        return normalizar(primera).equals(normalizar(segunda));
    }

    static String normalizar(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad es obligatoria");
        }
        return Normalizer.normalize(ciudad.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
