package com.tijetravel.tijeback.servicios;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

final class CiudadConsulta {
    private CiudadConsulta() {
    }

    static List<String> nombres(String ciudad) {
        String normalizada = normalizar(ciudad);
        return normalizada.equals("cordoba")
                ? List.of("cordoba", "córdoba") : List.of(normalizada);
    }

    static boolean misma(String primera, String segunda) {
        return normalizar(primera).equals(normalizar(segunda));
    }

    private static String normalizar(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad es obligatoria");
        }
        return Normalizer.normalize(ciudad.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
