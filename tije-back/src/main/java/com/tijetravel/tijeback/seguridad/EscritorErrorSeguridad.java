package com.tijetravel.tijeback.seguridad;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.errores.ErrorRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

/**
 * Responde en JSON los errores que corta Spring Security antes de llegar a
 * los controladores: falta de sesion (401) y falta de permisos (403).
 */
@Component
public class EscritorErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final JsonMapper jsonMapper;

    public EscritorErrorSeguridad(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest solicitud, HttpServletResponse respuesta,
            AuthenticationException excepcion) throws IOException {
        escribir(solicitud, respuesta, HttpStatus.UNAUTHORIZED, "AUTENTICACION_REQUERIDA",
                "Es necesario iniciar sesion");
    }

    @Override
    public void handle(HttpServletRequest solicitud, HttpServletResponse respuesta,
            AccessDeniedException excepcion) throws IOException {
        escribir(solicitud, respuesta, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permiso para realizar esta operacion");
    }

    private void escribir(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            HttpStatus estado,
            String error,
            String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                Instant.now(),
                estado.value(),
                error,
                mensaje,
                solicitud.getRequestURI(),
                new HashMap<String, String>());
        jsonMapper.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}
