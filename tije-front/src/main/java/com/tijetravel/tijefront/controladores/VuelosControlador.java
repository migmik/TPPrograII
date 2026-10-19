package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import com.tijetravel.tijefront.dto.SesionRespuesta;

import com.tijetravel.tijefront.clientes.VuelosApiCliente;

@Controller
@RequestMapping("/vuelos")
public class VuelosControlador {
    private final VuelosApiCliente vuelosApi;

    public VuelosControlador(VuelosApiCliente vuelosApi) {
        this.vuelosApi = vuelosApi;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String origen,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "numero") String orden,
            @RequestParam(defaultValue = "0") int pagina,
            HttpServletRequest solicitud, Model modelo) {
        origen = limpiar(origen);
        destino = limpiar(destino);
        if (!List.of("numero", "precioAsc", "precioDesc").contains(orden)) orden = "numero";
        if (pagina < 0) pagina = 0;
        SesionRespuesta usuario = solicitud.getSession(false) == null ? null
                : (SesionRespuesta) solicitud.getSession(false).getAttribute("usuarioActual");
        boolean personal = usuario != null && ("VENDEDOR".equals(usuario.getRol())
                || "ADMINISTRADOR".equals(usuario.getRol()));
        boolean busqueda = origen != null || destino != null || fechaDesde != null || fechaHasta != null;
        modelo.addAttribute("puedeVerCatalogo", personal);
        modelo.addAttribute("busquedaRealizada", busqueda || personal);
        modelo.addAttribute("origen", origen);
        modelo.addAttribute("destino", destino);
        modelo.addAttribute("fechaDesde", fechaDesde);
        modelo.addAttribute("fechaHasta", fechaHasta);
        modelo.addAttribute("orden", orden);
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            modelo.addAttribute("errorBusqueda", "La fecha final no puede ser anterior a la inicial.");
            return "vuelos/lista";
        }
        if (busqueda || personal) {
            var resultado = vuelosApi.listarPagina(origen, destino, fechaDesde, fechaHasta,
                    orden, pagina, personal);
            modelo.addAttribute("vuelos", resultado.elementos());
            modelo.addAttribute("pagina", resultado.pagina());
            modelo.addAttribute("totalPaginas", resultado.totalPaginas());
            modelo.addAttribute("hayAnterior", resultado.hayAnterior());
            modelo.addAttribute("haySiguiente", resultado.haySiguiente());
        }
        return "vuelos/lista";
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @GetMapping("/{numero}")
    public String detalle(@PathVariable Integer numero, Model modelo) {
        modelo.addAttribute("vuelo", vuelosApi.buscar(numero));
        modelo.addAttribute("disponibilidadTurista", vuelosApi.consultarDisponibilidad(numero, "TURISTA"));
        modelo.addAttribute("disponibilidadPrimera", vuelosApi.consultarDisponibilidad(numero, "PRIMERA"));
        return "vuelos/detalle";
    }
}
