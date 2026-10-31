package com.tijetravel.tijeback.api.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.GuardarSucursalSolicitud;
import com.tijetravel.tijeback.api.dto.SucursalRespuesta;
import com.tijetravel.tijeback.api.mapeadores.SucursalMapeador;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.seguridad.UsuarioActualServicio;
import com.tijetravel.tijeback.servicios.SucursalServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/sucursales")
public class SucursalesRestControlador {
    private final SucursalServicio sucursalServicio;
    private final SucursalMapeador sucursalMapeador;
    private final UsuarioActualServicio usuarioActualServicio;

    public SucursalesRestControlador(
            SucursalServicio sucursalServicio,
            SucursalMapeador sucursalMapeador,
            UsuarioActualServicio usuarioActualServicio) {
        this.sucursalServicio = sucursalServicio;
        this.sucursalMapeador = sucursalMapeador;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping
    public List<SucursalRespuesta> listar(@RequestParam(required = false) String ciudad) {
        return sucursalMapeador.aRespuestas(sucursalServicio.listar(ciudad));
    }

    @GetMapping("/ciudades")
    public List<String> ciudades() {
        return sucursalServicio.listarCiudades();
    }

    @GetMapping("/{codigo}")
    public SucursalRespuesta encontrarPorId(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo) {
        return sucursalMapeador.aRespuesta(sucursalServicio.encontrarPorId(codigo));
    }

    @PostMapping
    public ResponseEntity<SucursalRespuesta> crear(
            @Valid @RequestBody GuardarSucursalSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        Sucursal sucursal = sucursalServicio.crear(
                actor,
                solicitud.direccion(),
                solicitud.telefono(),
                solicitud.ciudad());
        SucursalRespuesta respuesta = sucursalMapeador.aRespuesta(sucursal);
        return ResponseEntity
                .created(URI.create("/api/v1/sucursales/" + respuesta.codigo()))
                .body(respuesta);
    }

    @PutMapping("/{codigo}")
    public SucursalRespuesta modificar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            @Valid @RequestBody GuardarSucursalSolicitud solicitud,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return sucursalMapeador.aRespuesta(sucursalServicio.modificar(
                actor,
                codigo,
                solicitud.direccion(),
                solicitud.telefono(),
                solicitud.ciudad()));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        sucursalServicio.darDeBaja(actor, codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bajas")
    public List<SucursalRespuesta> listarBajas(Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return sucursalMapeador.aRespuestas(sucursalServicio.listarBajasPara(actor));
    }

    @PostMapping("/{codigo}/reactivar")
    public SucursalRespuesta reactivar(
            @PathVariable @Positive(message = "El codigo debe ser positivo") Integer codigo,
            Authentication autenticacion) {
        Usuario actor = usuarioActualServicio.obtener(autenticacion);
        return sucursalMapeador.aRespuesta(sucursalServicio.reactivar(actor, codigo));
    }
}
