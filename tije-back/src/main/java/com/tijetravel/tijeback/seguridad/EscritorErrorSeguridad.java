package com.tijetravel.tijeback.seguridad;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.errores.ErrorRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EscritorErrorSeguridad {
    private final JsonMapper jsonMapper;

    public EscritorErrorSeguridad(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void escribir(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            HttpStatus estado,
            String error,
            String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(
                respuesta.getOutputStream(),
                new ErrorRespuesta(
                        Instant.now(),
                        estado.value(),
                        error,
                        mensaje,
                        solicitud.getRequestURI(),
                        Map.of()));
    }
}
