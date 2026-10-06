package com.tijetravel.tijeback.api.controladores;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.servicios.CiudadServicio;

@RestController
@RequestMapping("/api/v1/ciudades")
public class CiudadesRestControlador {
    private final CiudadServicio ciudadServicio;

    public CiudadesRestControlador(CiudadServicio ciudadServicio) {
        this.ciudadServicio = ciudadServicio;
    }

    @GetMapping
    public List<String> listar() {
        return ciudadServicio.listarDisponibles();
    }
}