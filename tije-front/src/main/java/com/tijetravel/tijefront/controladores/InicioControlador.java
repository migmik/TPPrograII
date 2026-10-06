package com.tijetravel.tijefront.controladores;

import java.text.Normalizer;
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
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.formularios.BuscarViajeFormulario;

import jakarta.validation.Valid;

@Controller
public class InicioControlador {
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
        List<VueloRespuesta> vuelosSugeridos = List.of();
        if (vuelos.isEmpty()) {
            vuelosSugeridos = vuelosApi.buscar(busqueda.getOrigen(), busqueda.getDestino(),
                    busqueda.getFechaLlegada(), busqueda.getPersonas(), true);
        }

        List<HotelRespuesta> hoteles = hotelesApi.buscar(busqueda.getDestino(),
                busqueda.getFechaLlegada(), busqueda.getFechaPartida(), busqueda.getPersonas());

        modelo.addAttribute("vuelosEncontrados", vuelos);
        modelo.addAttribute("vuelosSugeridos", vuelosSugeridos);
        modelo.addAttribute("hotelesEncontrados", hoteles);
        modelo.addAttribute("busquedaRealizada", true);
        return "inicio";
    }

    private void cargarCiudades(Model modelo) {
        try {
            modelo.addAttribute("ciudades", ciudadesApi.listar());
        } catch (RestClientException error) {
            modelo.addAttribute("ciudades", List.of());
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
