package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tijetravel.tijefront.clientes.VuelosApiCliente;

@Controller
@RequestMapping("/vuelos")
public class VuelosControlador {
    private final VuelosApiCliente vuelosApi;

    public VuelosControlador(VuelosApiCliente vuelosApi) {
        this.vuelosApi = vuelosApi;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "numero") String orden, Model modelo) {
        if (!java.util.List.of("numero", "precioAsc", "precioDesc").contains(orden)) orden = "numero";
        modelo.addAttribute("vuelos", "numero".equals(orden) ? vuelosApi.listar() : vuelosApi.listar(orden));
        modelo.addAttribute("orden", orden);
        return "vuelos/lista";
    }

    @GetMapping("/{numero}")
    public String detalle(@PathVariable Integer numero, Model modelo) {
        modelo.addAttribute("vuelo", vuelosApi.buscar(numero));
        modelo.addAttribute("disponibilidadTurista", vuelosApi.consultarDisponibilidad(numero, "TURISTA"));
        modelo.addAttribute("disponibilidadPrimera", vuelosApi.consultarDisponibilidad(numero, "PRIMERA"));
        return "vuelos/detalle";
    }
}
