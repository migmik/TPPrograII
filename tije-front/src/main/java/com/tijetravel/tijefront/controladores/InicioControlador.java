package com.tijetravel.tijefront.controladores;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.formularios.BuscarViajeFormulario;

import jakarta.validation.Valid;

@Controller
public class InicioControlador {
    private final VuelosApiCliente vuelosApi;
    private final HotelesApiCliente hotelesApi;

    public InicioControlador(VuelosApiCliente vuelosApi, HotelesApiCliente hotelesApi) {
        this.vuelosApi = vuelosApi;
        this.hotelesApi = hotelesApi;
    }

    @GetMapping("/")
    public String inicio(Model modelo) {
        modelo.addAttribute("busqueda", new BuscarViajeFormulario());
        return "inicio";
    }

    @GetMapping("/buscar")
    public String buscar(@Valid @ModelAttribute("busqueda") BuscarViajeFormulario busqueda,
            BindingResult errores, Model modelo) {
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

        List<VueloRespuesta> vuelos = new ArrayList<>();
        for (VueloRespuesta vuelo : vuelosApi.listar()) {
            if (mismaCiudad(vuelo.getOrigen(), busqueda.getOrigen())
                    && mismaCiudad(vuelo.getDestino(), busqueda.getDestino())
                    && vuelo.getFechaYHora().toLocalDate().equals(busqueda.getFechaLlegada())
                    && vuelosApi.consultarDisponibilidad(vuelo.getNumero(), "TURISTA")
                            .getPlazasDisponibles() >= busqueda.getPersonas()) {
                vuelos.add(vuelo);
            }
        }

        List<HotelRespuesta> hoteles = new ArrayList<>();
        for (HotelRespuesta hotel : hotelesApi.listar()) {
            if (mismaCiudad(hotel.getCiudad(), busqueda.getDestino())
                    && hotelesApi.consultarDisponibilidad(hotel.getCodigo(),
                            busqueda.getFechaLlegada(), busqueda.getFechaPartida())
                            .getPlazasDisponibles() >= busqueda.getPersonas()) {
                hoteles.add(hotel);
            }
        }

        modelo.addAttribute("vuelosEncontrados", vuelos);
        modelo.addAttribute("hotelesEncontrados", hoteles);
        modelo.addAttribute("busquedaRealizada", true);
        return "inicio";
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
