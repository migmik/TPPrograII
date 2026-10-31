package com.tijetravel.tijefront.controladores;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    /** Noches que se usan para el hotel cuando no se elige la fecha de partida. */
    private static final int NOCHES_SIN_PARTIDA = 3;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        // Aunque haya errores el buscador queda compacto, como en los resultados.
        modelo.addAttribute("modoBusqueda", true);
        if (mismaCiudad(busqueda.getOrigen(), busqueda.getDestino())) {
            errores.rejectValue("destino", "ciudades.iguales", "El destino debe ser distinto del origen.");
        }
        LocalDate llegada = busqueda.getFechaLlegada();
        LocalDate partida = busqueda.getFechaPartida();
        if (llegada == null && partida != null) {
            errores.rejectValue("fechaLlegada", "fechas.llegada", "Para elegir la partida, elegí también la llegada.");
        }
        if (llegada != null && partida != null && !partida.isAfter(llegada)) {
            errores.rejectValue("fechaPartida", "fechas.orden", "La partida debe ser posterior a la llegada.");
        }
        if (errores.hasErrors()) {
            return "inicio";
        }

        long noches = NOCHES_SIN_PARTIDA;
        if (llegada != null && partida != null) {
            noches = ChronoUnit.DAYS.between(llegada, partida);
        }

        // Si no hay vuelos en la fecha pedida (o no se eligio fecha) se muestran los mas cercanos.
        List<VueloRespuesta> vuelos;
        String avisoVuelos = null;
        if (llegada == null) {
            vuelos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(), null,
                    busqueda.getPersonas(), false);
            avisoVuelos = "Como no elegiste fecha, te mostramos los próximos vuelos.";
        } else {
            vuelos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(), llegada,
                    busqueda.getPersonas(), false);
            if (vuelos.isEmpty()) {
                vuelos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(), llegada,
                        busqueda.getPersonas(), true);
                if (!vuelos.isEmpty()) {
                    avisoVuelos = "No hay vuelos el " + llegada.format(FORMATO_FECHA)
                            + ". Te mostramos los más cercanos.";
                }
            }
        }

        // Los hoteles se buscan para el dia de cada vuelo; se guardan por dia para no repetir consultas.
        Map<LocalDate, List<HotelRespuesta>> hotelesPorDia = new HashMap<>();
        List<CombinacionViaje> combinaciones = new ArrayList<>();
        for (VueloRespuesta vuelo : vuelos) {
            LocalDate dia = vuelo.getFechaYHora().toLocalDate();
            List<HotelRespuesta> hotelesDelDia = buscarHoteles(hotelesPorDia, busqueda, dia, noches);
            for (HotelRespuesta hotel : hotelesDelDia) {
                combinaciones.add(new CombinacionViaje(vuelo, hotel, dia, dia.plusDays(noches)));
            }
        }

        // "Solo hoteles" usa la fecha pedida, o la del primer vuelo si no se eligio fecha.
        LocalDate diaHotel = llegada;
        if (diaHotel == null && !vuelos.isEmpty()) {
            diaHotel = vuelos.get(0).getFechaYHora().toLocalDate();
        }
        if (diaHotel == null) {
            diaHotel = LocalDate.now();
        }
        List<HotelRespuesta> hoteles = buscarHoteles(hotelesPorDia, busqueda, diaHotel, noches);

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

        cargarCombinaciones(modelo, combinaciones);
        modelo.addAttribute("vuelosEncontrados", vuelos);
        modelo.addAttribute("avisoVuelos", avisoVuelos);
        modelo.addAttribute("hotelesEncontrados", hoteles);
        modelo.addAttribute("hotelLlegada", diaHotel.format(FORMATO_FECHA));
        modelo.addAttribute("hotelPartida", diaHotel.plusDays(noches).format(FORMATO_FECHA));
        modelo.addAttribute("cantidadNoches", noches);
        modelo.addAttribute("busquedaRealizada", true);
        return "inicio";
    }

    private List<HotelRespuesta> buscarHoteles(Map<LocalDate, List<HotelRespuesta>> hotelesPorDia,
            BuscarViajeFormulario busqueda, LocalDate dia, long noches) {
        List<HotelRespuesta> hoteles = hotelesPorDia.get(dia);
        if (hoteles == null) {
            hoteles = hotelesApi.buscar(busqueda.getDestino(), dia, dia.plusDays(noches), busqueda.getPersonas());
            hotelesPorDia.put(dia, hoteles);
        }
        return hoteles;
    }

    /**
     * Ordena las combinaciones de vuelo y hotel por precio. Las mas baratas se
     * muestran primero como recomendadas y el resto queda aparte.
     */
    private void cargarCombinaciones(Model modelo, List<CombinacionViaje> combinaciones) {
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
