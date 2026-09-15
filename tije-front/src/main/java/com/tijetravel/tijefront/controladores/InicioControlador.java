package com.tijetravel.tijefront.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }
}
