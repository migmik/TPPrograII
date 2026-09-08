package com.tijetravel.tijeback.api.controladores;

import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tijetravel.tijeback.api.dto.CsrfRespuesta;
import com.tijetravel.tijeback.api.dto.IniciarSesionSolicitud;
import com.tijetravel.tijeback.api.dto.SesionRespuesta;
import com.tijetravel.tijeback.api.mapeadores.SesionMapeador;
import com.tijetravel.tijeback.seguridad.UsuarioAutenticado;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/autenticacion")
public class AutenticacionRestControlador {
    private final AuthenticationManager administradorAutenticacion;
    private final SessionAuthenticationStrategy estrategiaAutenticacionSesion;
    private final SecurityContextRepository repositorioContextoSeguridad;
    private final SesionMapeador sesionMapeador;
    private final SecurityContextHolderStrategy contextoEstrategia =
            SecurityContextHolder.getContextHolderStrategy();

    public AutenticacionRestControlador(
            AuthenticationManager administradorAutenticacion,
            SessionAuthenticationStrategy estrategiaAutenticacionSesion,
            SecurityContextRepository repositorioContextoSeguridad,
            SesionMapeador sesionMapeador) {
        this.administradorAutenticacion = administradorAutenticacion;
        this.estrategiaAutenticacionSesion = estrategiaAutenticacionSesion;
        this.repositorioContextoSeguridad = repositorioContextoSeguridad;
        this.sesionMapeador = sesionMapeador;
    }

    @GetMapping("/csrf")
    public CsrfRespuesta obtenerCsrf(CsrfToken token) {
        return new CsrfRespuesta(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/login")
    public SesionRespuesta iniciarSesion(
            @Valid @RequestBody IniciarSesionSolicitud credenciales,
            HttpServletRequest solicitud,
            HttpServletResponse respuesta) {
        if (credenciales.contrasenia().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Contrasenia demasiado extensa");
        }
        Authentication autenticacion = administradorAutenticacion.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        credenciales.nombreUsuario().trim(),
                        credenciales.contrasenia()));

        estrategiaAutenticacionSesion.onAuthentication(autenticacion, solicitud, respuesta);
        SecurityContext contexto = contextoEstrategia.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        contextoEstrategia.setContext(contexto);
        repositorioContextoSeguridad.saveContext(contexto, solicitud, respuesta);

        return sesionMapeador.aRespuesta((UsuarioAutenticado) autenticacion.getPrincipal());
    }

    @GetMapping("/sesion")
    public SesionRespuesta obtenerSesion(Authentication autenticacion) {
        return sesionMapeador.aRespuesta((UsuarioAutenticado) autenticacion.getPrincipal());
    }
}
