package com.tijetravel.tijefront.clientes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma la ruta de una consulta a la API junto con sus parametros (patron
 * Builder). Los valores se guardan aparte para que RestClient los codifique
 * (espacios, acentos, etc.). Los parametros nulos o vacios no se agregan.
 *
 * Ejemplo: new RutaConsulta("/api/v1/hoteles/pagina").parametro("pagina", 0)
 * genera la plantilla "/api/v1/hoteles/pagina?pagina={pagina}".
 */
public class RutaConsulta {
    private final StringBuilder plantilla;
    private final Map<String, Object> valores = new HashMap<>();
    private boolean tieneParametros = false;

    public RutaConsulta(String ruta) {
        this.plantilla = new StringBuilder(ruta);
    }

    /** Valor de una variable que ya esta en la ruta, por ejemplo {codigo}. */
    public RutaConsulta variable(String nombre, Object valor) {
        valores.put(nombre, valor);
        return this;
    }

    public RutaConsulta parametro(String nombre, Object valor) {
        if (valor == null) {
            return this;
        }
        if (valor instanceof String) {
            String texto = ((String) valor).trim();
            if (texto.isEmpty()) {
                return this;
            }
            valor = texto;
        }
        agregar(nombre, nombre, valor);
        return this;
    }

    /** Repite el parametro una vez por cada valor: codigos=1&codigos=2. */
    public RutaConsulta parametros(String nombre, List<?> lista) {
        for (int i = 0; i < lista.size(); i++) {
            agregar(nombre, nombre + i, lista.get(i));
        }
        return this;
    }

    private void agregar(String nombre, String variable, Object valor) {
        if (tieneParametros) {
            plantilla.append('&');
        } else {
            plantilla.append('?');
            tieneParametros = true;
        }
        plantilla.append(nombre).append("={").append(variable).append('}');
        valores.put(variable, valor);
    }

    public String getPlantilla() {
        return plantilla.toString();
    }

    public Map<String, Object> getValores() {
        return valores;
    }
}
