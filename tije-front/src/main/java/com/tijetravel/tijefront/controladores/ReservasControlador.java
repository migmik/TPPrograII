package com.tijetravel.tijefront.controladores;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.ReservasApiCliente;
import com.tijetravel.tijefront.clientes.TuristasApiCliente;
import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.ReservaRespuesta;
import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.formularios.GuardarReservaFormulario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reservas")
public class ReservasControlador {
    private final AutenticacionApiCliente autenticacionApi;
    private final ReservasApiCliente reservasApi;
    private final TuristasApiCliente turistasApi;
    private final HotelesApiCliente hotelesApi;
    private final VuelosApiCliente vuelosApi;

    public ReservasControlador(AutenticacionApiCliente autenticacionApi, ReservasApiCliente reservasApi,
            TuristasApiCliente turistasApi, HotelesApiCliente hotelesApi, VuelosApiCliente vuelosApi) {
        this.autenticacionApi = autenticacionApi;
        this.reservasApi = reservasApi;
        this.turistasApi = turistasApi;
        this.hotelesApi = hotelesApi;
        this.vuelosApi = vuelosApi;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Integer codigoTurista,
            @RequestParam(required = false) Integer numeroVuelo,
            @RequestParam(required = false) Integer codigoHotel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int pagina,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(false, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        var resultado = reservasApi.listarPagina(
                codigoTurista, numeroVuelo, codigoHotel, fechaDesde, fechaHasta, pagina, 20);
        List<ReservaRespuesta> reservas = resultado.elementos();
        modelo.addAttribute("reservas", reservas);
        modelo.addAttribute("pagina", resultado.pagina());
        modelo.addAttribute("totalPaginas", resultado.totalPaginas());
        modelo.addAttribute("hayAnterior", resultado.hayAnterior());
        modelo.addAttribute("haySiguiente", resultado.haySiguiente());
        modelo.addAttribute("codigoTuristaBusqueda", codigoTurista);
        modelo.addAttribute("numeroVueloBusqueda", numeroVuelo);
        modelo.addAttribute("codigoHotelBusqueda", codigoHotel);
        modelo.addAttribute("fechaDesdeBusqueda", fechaDesde);
        modelo.addAttribute("fechaHastaBusqueda", fechaHasta);
        Map<Integer, TuristaResumen> turistas = new HashMap<>();
        List<Integer> codigos = reservas.stream().map(ReservaRespuesta::getCodigoTurista).distinct().toList();
        for (TuristaResumen turista : turistasApi.listarPorCodigos(codigos))
            turistas.put(turista.getCodigo(), turista);
        modelo.addAttribute("turistasPorCodigo", turistas);
        return "reservas/lista";
    }

    @GetMapping("/{codigo}")
    public String detalle(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(false, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        cargarDetalle(codigo, modelo);
        return "reservas/detalle";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Integer numeroVuelo,
            @RequestParam(required = false) Integer codigoHotel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaLlegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPartida,
            HttpServletRequest solicitud, HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(false, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        GuardarReservaFormulario formulario = new GuardarReservaFormulario();
        formulario.setNumeroVuelo(numeroVuelo);
        formulario.setCodigoHotel(codigoHotel);
        formulario.setFechaLlegada(fechaLlegada);
        formulario.setFechaPartida(fechaPartida);
        formulario.setClaseVuelo("TURISTA");
        formulario.setSeleccionBuscador(numeroVuelo != null && codigoHotel != null);
        modelo.addAttribute("reserva", formulario);
        cargarOpciones(modelo, formulario);
        return "reservas/formulario";
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        ReservaRespuesta actual = reservasApi.buscar(codigo);
        GuardarReservaFormulario formulario = new GuardarReservaFormulario();
        formulario.setCodigoTurista(actual.getCodigoTurista());
        formulario.setNumeroVuelo(actual.getNumeroVuelo());
        formulario.setCodigoHotel(actual.getCodigoHotel());
        formulario.setClaseVuelo(actual.getClaseVuelo());
        formulario.setTipoHospedaje(actual.getTipoHospedaje());
        formulario.setFechaLlegada(actual.getFechaLlegada());
        formulario.setFechaPartida(actual.getFechaPartida());
        modelo.addAttribute("reserva", formulario);
        modelo.addAttribute("actual", actual);
        cargarOpciones(modelo, formulario);
        return "reservas/formulario";
    }

    @PostMapping({ "", "/{codigo}/editar" })
    public String guardar(@PathVariable(required = false) Integer codigo,
            @Valid @ModelAttribute("reserva") GuardarReservaFormulario formulario,
            BindingResult errores, HttpServletRequest solicitud, HttpServletResponse respuesta,
            Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAcceso(codigo != null, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        if (!errores.hasFieldErrors("fechaLlegada") && !errores.hasFieldErrors("fechaPartida")
                && !formulario.getFechaLlegada().isBefore(formulario.getFechaPartida())) {
            errores.rejectValue("fechaPartida", "fechas.orden", "La partida debe ser posterior a la llegada.");
        }
        if (errores.hasErrors()) {
            prepararFormulario(codigo, formulario, modelo);
            return "reservas/formulario";
        }
        try {
            if (codigo == null)
                reservasApi.crear(formulario);
            else
                reservasApi.modificar(codigo, formulario);
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado != 400 && estado != 403 && estado != 409)
                throw error;
            respuesta.setStatus(estado);
            String mensaje = "Revisá los datos y las fechas de la reserva.";
            if (estado == 409)
                mensaje = "No se pudo guardar: faltan plazas en el vuelo o el hotel, o el turista ya tiene una reserva para ese vuelo.";
            if (estado == 403)
                mensaje = "No se pudo guardar. Verificá tus permisos y que la llegada coincida con la fecha del vuelo y la ciudad del hotel con su destino.";
            modelo.addAttribute("errorOperacion", mensaje);
            prepararFormulario(codigo, formulario, modelo);
            return "reservas/formulario";
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorOperacion",
                    "No pudimos confirmar el guardado. Consultá el listado antes de volver a enviarlo.");
            prepararFormulario(codigo, formulario, modelo);
            return "reservas/formulario";
        }
        redireccion.addFlashAttribute("mensajeExito", "Reserva guardada correctamente.");
        return "redirect:/reservas";
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        cargarDetalle(codigo, modelo);
        return "reservas/eliminar";
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable Integer codigo, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo, RedirectAttributes redireccion) {
        String salida = comprobarAcceso(true, solicitud, respuesta, modelo);
        if (salida != null)
            return salida;
        cargarDetalle(codigo, modelo);
        try {
            reservasApi.eliminar(codigo);
        } catch (ResourceAccessException error) {
            respuesta.setStatus(503);
            modelo.addAttribute("errorOperacion",
                    "No pudimos confirmar la eliminación. Consultá el listado antes de volver a intentarlo.");
            return "reservas/eliminar";
        }
        redireccion.addFlashAttribute("mensajeExito", "Reserva eliminada correctamente.");
        return "redirect:/reservas";
    }

    private void prepararFormulario(Integer codigo, GuardarReservaFormulario formulario, Model modelo) {
        if (codigo != null)
            modelo.addAttribute("actual", reservasApi.buscar(codigo));
        cargarOpciones(modelo, formulario);
    }

    private void cargarOpciones(Model modelo, GuardarReservaFormulario formulario) {
        modelo.addAttribute("turistas", turistasApi.listar());
        if (formulario.isSeleccionBuscador()
                && formulario.getCodigoHotel() != null && formulario.getNumeroVuelo() != null) {
            modelo.addAttribute("hoteles", List.of(hotelesApi.buscar(formulario.getCodigoHotel())));
            modelo.addAttribute("vuelos", List.of(vuelosApi.buscar(formulario.getNumeroVuelo())));
        } else {
            modelo.addAttribute("hoteles", hotelesApi.listar());
            modelo.addAttribute("vuelos", vuelosApi.listar());
        }
    }

    private void cargarDetalle(Integer codigo, Model modelo) {
        // Primero la reserva: el backend comprueba que pertenezca al grupo del cliente.
        ReservaRespuesta reserva = reservasApi.buscar(codigo);
        modelo.addAttribute("reserva", reserva);
        modelo.addAttribute("turista", turistasApi.buscar(reserva.getCodigoTurista()));
        modelo.addAttribute("hotel", hotelesApi.buscar(reserva.getCodigoHotel()));
        modelo.addAttribute("vuelo", vuelosApi.buscar(reserva.getNumeroVuelo()));
    }

    private String comprobarAcceso(boolean escritura, HttpServletRequest solicitud,
            HttpServletResponse respuesta, Model modelo) {
        var sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioActual") == null)
            return "redirect:/login";
        SesionRespuesta usuario = autenticacionApi.obtenerSesion();
        sesion.setAttribute("usuarioActual", usuario);
        modelo.addAttribute("usuarioActual", usuario);
        boolean puedeGestionar = "ADMINISTRADOR".equals(usuario.getRol()) || "VENDEDOR".equals(usuario.getRol());
        modelo.addAttribute("puedeGestionar", puedeGestionar);
        modelo.addAttribute("puedeCrear", puedeGestionar || "CLIENTE".equals(usuario.getRol()));
        if (escritura && !puedeGestionar) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Solo administradores y vendedores pueden gestionar reservas.");
            return "error";
        }
        return null;
    }
}
