package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import com.tijetravel.tijefront.dto.SesionRespuesta;

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
    public String listar(@RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "codigo") String orden,
            @RequestParam(defaultValue = "0") int pagina,
            HttpServletRequest solicitud, Model modelo) {
        ciudad = limpiar(ciudad);
        nombre = limpiar(nombre);
        if (!List.of("codigo", "precioAsc", "precioDesc").contains(orden)) orden = "codigo";
        if (pagina < 0) pagina = 0;
        SesionRespuesta usuario = solicitud.getSession(false) == null ? null
                : (SesionRespuesta) solicitud.getSession(false).getAttribute("usuarioActual");
        boolean personal = usuario != null && ("VENDEDOR".equals(usuario.getRol())
                || "ADMINISTRADOR".equals(usuario.getRol()));
        boolean busqueda = ciudad != null || nombre != null;
        modelo.addAttribute("puedeVerCatalogo", personal);
        modelo.addAttribute("busquedaRealizada", busqueda || personal);
        modelo.addAttribute("ciudadBusqueda", ciudad);
        modelo.addAttribute("nombreBusqueda", nombre);
        modelo.addAttribute("orden", orden);
        if (busqueda || personal) {
            var resultado = hotelesApi.listarPagina(ciudad, nombre, orden, pagina, personal);
            modelo.addAttribute("hoteles", resultado.elementos());
            modelo.addAttribute("pagina", resultado.pagina());
            modelo.addAttribute("totalPaginas", resultado.totalPaginas());
            modelo.addAttribute("hayAnterior", resultado.hayAnterior());
            modelo.addAttribute("haySiguiente", resultado.haySiguiente());
        }
        return "hoteles/lista";
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
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
