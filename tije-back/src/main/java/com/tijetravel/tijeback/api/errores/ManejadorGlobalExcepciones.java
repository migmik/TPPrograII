package com.tijetravel.tijeback.api.errores;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.CredencialesInvalidasException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> manejarEntidadNoEncontrada(
            EntidadNoEncontradaException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.NOT_FOUND,
                "ENTIDAD_NO_ENCONTRADA",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(EntidadDuplicadaException.class)
    public ResponseEntity<ErrorRespuesta> manejarEntidadDuplicada(
            EntidadDuplicadaException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.CONFLICT,
                "ENTIDAD_DUPLICADA",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(CapacidadExcedidaException.class)
    public ResponseEntity<ErrorRespuesta> manejarCapacidadExcedida(
            CapacidadExcedidaException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.CONFLICT,
                "CAPACIDAD_EXCEDIDA",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorRespuesta> manejarOperacionNoPermitida(
            OperacionNoPermitidaException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.FORBIDDEN,
                "OPERACION_NO_PERMITIDA",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorRespuesta> manejarCredencialesInvalidas(
            CredencialesInvalidasException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.UNAUTHORIZED,
                "CREDENCIALES_INVALIDAS",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> manejarFalloAutenticacion(
            AuthenticationException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.UNAUTHORIZED,
                "CREDENCIALES_INVALIDAS",
                "Usuario o contrasenia incorrectos",
                solicitud,
                Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoInvalido(
            MethodArgumentNotValidException excepcion,
            HttpServletRequest solicitud) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            detalles.putIfAbsent(error.getField(), mensaje(error));
        }
        return responder(
                HttpStatus.BAD_REQUEST,
                "DATOS_INVALIDOS",
                "La solicitud contiene datos invalidos",
                solicitud,
                detalles);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametrosInvalidos(
            HandlerMethodValidationException excepcion,
            HttpServletRequest solicitud) {
        Map<String, String> detalles = new LinkedHashMap<>();
        excepcion.getParameterValidationResults().forEach(resultado -> {
            String parametro = resultado.getMethodParameter().getParameterName();
            for (MessageSourceResolvable error : resultado.getResolvableErrors()) {
                detalles.putIfAbsent(
                        parametro == null ? "parametro" : parametro,
                        mensaje(error));
            }
        });
        return responder(
                HttpStatus.BAD_REQUEST,
                "PARAMETROS_INVALIDOS",
                "La solicitud contiene parametros invalidos",
                solicitud,
                detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarRestriccionesInvalidas(
            ConstraintViolationException excepcion,
            HttpServletRequest solicitud) {
        Map<String, String> detalles = new LinkedHashMap<>();
        excepcion.getConstraintViolations().forEach(violacion -> detalles.putIfAbsent(
                violacion.getPropertyPath().toString(),
                violacion.getMessage()));
        return responder(
                HttpStatus.BAD_REQUEST,
                "PARAMETROS_INVALIDOS",
                "La solicitud contiene parametros invalidos",
                solicitud,
                detalles);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorRespuesta> manejarSolicitudInvalida(
            Exception excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "No se pudo interpretar la solicitud",
                solicitud,
                Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> manejarArgumentoInvalido(
            IllegalArgumentException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "ARGUMENTO_INVALIDO",
                excepcion.getMessage(),
                solicitud,
                Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridadDeDatos(
            DataIntegrityViolationException excepcion,
            HttpServletRequest solicitud) {
        LOGGER.warn("Conflicto de integridad al procesar {}", solicitud.getRequestURI(), excepcion);
        return responder(
                HttpStatus.CONFLICT,
                "CONFLICTO_DE_DATOS",
                "La operacion entra en conflicto con los datos existentes",
                solicitud,
                Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaInexistente(
            NoResourceFoundException excepcion,
            HttpServletRequest solicitud) {
        return responder(
                HttpStatus.NOT_FOUND,
                "RUTA_NO_ENCONTRADA",
                "No existe el recurso solicitado",
                solicitud,
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorInesperado(
            Exception excepcion,
            HttpServletRequest solicitud) {
        if (excepcion instanceof ErrorResponse errorHttp) {
            return responder(
                    errorHttp.getStatusCode(),
                    "ERROR_HTTP",
                    "La solicitud HTTP no pudo completarse",
                    solicitud,
                    Map.of());
        }

        LOGGER.error("Error inesperado al procesar {}", solicitud.getRequestURI(), excepcion);
        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrio un error interno",
                solicitud,
                Map.of());
    }

    private ResponseEntity<ErrorRespuesta> responder(
            HttpStatusCode estado,
            String error,
            String mensaje,
            HttpServletRequest solicitud,
            Map<String, String> detalles) {
        ErrorRespuesta respuesta = new ErrorRespuesta(
                Instant.now(),
                estado.value(),
                error,
                mensaje,
                solicitud.getRequestURI(),
                detalles);
        return ResponseEntity.status(estado).body(respuesta);
    }

    private String mensaje(MessageSourceResolvable error) {
        return error.getDefaultMessage() == null ? "Valor invalido" : error.getDefaultMessage();
    }
}
