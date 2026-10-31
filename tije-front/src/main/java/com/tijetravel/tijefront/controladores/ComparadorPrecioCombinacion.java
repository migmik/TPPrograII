package com.tijetravel.tijefront.controladores;

import java.util.Comparator;

import com.tijetravel.tijefront.dto.CombinacionViaje;

/** Ordena las combinaciones de vuelo y hotel de la mas barata a la mas cara. */
public class ComparadorPrecioCombinacion implements Comparator<CombinacionViaje> {

    @Override
    public int compare(CombinacionViaje primera, CombinacionViaje segunda) {
        return Long.compare(primera.getPrecioPorPersona(), segunda.getPrecioPorPersona());
    }
}
