package com.tijetravel.tijeback.servicios;

import java.util.List;

/** Ciudades que aparecen en el catalogo de hoteles y vuelos. */
public interface CiudadServicio {

    /** Ciudades sin repetir (sin importar acentos) y ordenadas alfabeticamente. */
    List<String> listarDisponibles();
}
