package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.formularios.ConsultaDisponibilidad;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/hoteles")
public class HotelesControlador {
    private final HotelesApiCliente hotelesApi;

    public HotelesControlador(HotelesApiCliente hotelesApi) {
        this.hotelesApi = hotelesApi;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("hoteles", hotelesApi.listar());
        return "hoteles/lista";
    }

    @GetMapping("/{codigo}")
    public String detalle(@PathVariable Integer codigo, Model modelo) {
        modelo.addAttribute("hotel", hotelesApi.buscar(codigo));
        modelo.addAttribute("consulta", new ConsultaDisponibilidad());
        return "hoteles/detalle";
    }

    @GetMapping("/{codigo}/disponibilidad")
    public String disponibilidad(
            @PathVariable Integer codigo,
            @Valid @ModelAttribute("consulta") ConsultaDisponibilidad consulta,
            BindingResult errores,
            Model modelo) {
        modelo.addAttribute("hotel", hotelesApi.buscar(codigo));
        if (!errores.hasErrors() && !consulta.getFechaLlegada().isBefore(consulta.getFechaPartida())) {
            errores.rejectValue("fechaPartida", "fechas.orden", "La partida debe ser posterior a la llegada.");
        }
        if (!errores.hasErrors()) {
            modelo.addAttribute("disponibilidad", hotelesApi.consultarDisponibilidad(
                    codigo, consulta.getFechaLlegada(), consulta.getFechaPartida()));
        }
        return "hoteles/detalle";
    }
}
