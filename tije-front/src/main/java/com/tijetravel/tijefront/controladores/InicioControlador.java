package com.tijetravel.tijefront.controladores;

import java.text.Normalizer;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.clientes.CiudadesApiCliente;
import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.CombinacionViaje;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.formularios.BuscarViajeFormulario;

import jakarta.validation.Valid;

@Controller
public class InicioControlador {
    /** Cuantas combinaciones de vuelo y hotel se muestran como recomendadas. */
    private static final int MAXIMO_RECOMENDADOS = 6;

    private final VuelosApiCliente vuelosApi;
    private final HotelesApiCliente hotelesApi;
    private final CiudadesApiCliente ciudadesApi;

    public InicioControlador(VuelosApiCliente vuelosApi, HotelesApiCliente hotelesApi,
            CiudadesApiCliente ciudadesApi) {
        this.vuelosApi = vuelosApi;
        this.hotelesApi = hotelesApi;
        this.ciudadesApi = ciudadesApi;
    }

    @GetMapping("/")
    public String inicio(Model modelo) {
        modelo.addAttribute("busqueda", new BuscarViajeFormulario());
        cargarCiudades(modelo);
        return "inicio";
    }

    @GetMapping("/buscar")
    public String buscar(@Valid @ModelAttribute("busqueda") BuscarViajeFormulario busqueda,
            BindingResult errores, Model modelo) {
        cargarCiudades(modelo);
        if (mismaCiudad(busqueda.getOrigen(), busqueda.getDestino())) {
            errores.rejectValue("destino", "ciudades.iguales", "El destino debe ser distinto del origen.");
        }
        if (busqueda.getFechaLlegada() != null && busqueda.getFechaPartida() != null
                && !busqueda.getFechaPartida().isAfter(busqueda.getFechaLlegada())) {
            errores.rejectValue("fechaPartida", "fechas.orden", "La partida debe ser posterior a la llegada.");
        }
        if (errores.hasErrors()) {
            return "inicio";
        }

        List<VueloRespuesta> vuelos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(),
                busqueda.getFechaLlegada(), busqueda.getPersonas(), false);
        List<VueloRespuesta> vuelosSugeridos = new ArrayList<>();
        if (vuelos.isEmpty()) {
            vuelosSugeridos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(),
                    busqueda.getFechaLlegada(), busqueda.getPersonas(), true);
        }

        List<HotelRespuesta> hoteles = hotelesApi.buscar(busqueda.getDestino(),
                busqueda.getFechaLlegada(), busqueda.getFechaPartida(), busqueda.getPersonas());

        // Se copian las listas porque las que devuelve el cliente no se pueden ordenar.
        vuelos = new ArrayList<>(vuelos);
        hoteles = new ArrayList<>(hoteles);
        if ("precioAsc".equals(busqueda.getOrdenPrecio())) {
            Collections.sort(vuelos, new ComparadorPrecioVuelo(false));
            Collections.sort(hoteles, new ComparadorPrecioHotel(false));
        } else if ("precioDesc".equals(busqueda.getOrdenPrecio())) {
            Collections.sort(vuelos, new ComparadorPrecioVuelo(true));
            Collections.sort(hoteles, new ComparadorPrecioHotel(true));
        }

        long noches = ChronoUnit.DAYS.between(busqueda.getFechaLlegada(), busqueda.getFechaPartida());
        cargarCombinaciones(modelo, vuelos, hoteles, noches);
        modelo.addAttribute("vuelosEncontrados", vuelos);
        modelo.addAttribute("vuelosSugeridos", vuelosSugeridos);
        modelo.addAttribute("hotelesEncontrados", hoteles);
        modelo.addAttribute("cantidadNoches", noches);
        modelo.addAttribute("busquedaRealizada", true);
        return "inicio";
    }

    /**
     * Arma todas las combinaciones de vuelo y hotel ordenadas por precio. Las mas
     * baratas se muestran primero como recomendadas y el resto queda aparte.
     */
    private void cargarCombinaciones(Model modelo, List<VueloRespuesta> vuelos, List<HotelRespuesta> hoteles,
            long noches) {
        List<CombinacionViaje> combinaciones = new ArrayList<>();
        for (VueloRespuesta vuelo : vuelos) {
            for (HotelRespuesta hotel : hoteles) {
                combinaciones.add(new CombinacionViaje(vuelo, hotel, noches));
            }
        }
        Collections.sort(combinaciones, new ComparadorPrecioCombinacion());

        List<CombinacionViaje> recomendados = new ArrayList<>();
        List<CombinacionViaje> otras = new ArrayList<>();
        for (int i = 0; i < combinaciones.size(); i++) {
            if (i < MAXIMO_RECOMENDADOS) {
                recomendados.add(combinaciones.get(i));
            } else {
                otras.add(combinaciones.get(i));
            }
        }
        modelo.addAttribute("recomendados", recomendados);
        modelo.addAttribute("otrasCombinaciones", otras);
    }

    private void cargarCiudades(Model modelo) {
        try {
            modelo.addAttribute("ciudades", ciudadesApi.listar());
        } catch (RestClientException error) {
            modelo.addAttribute("ciudades", new ArrayList<String>());
        }
    }

    private boolean mismaCiudad(String primera, String segunda) {
        if (primera == null || segunda == null) {
            return false;
        }
        return normalizar(primera).equals(normalizar(segunda));
    }

    private String normalizar(String ciudad) {
        return Normalizer.normalize(ciudad.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
