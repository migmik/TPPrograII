package com.tijetravel.tijefront.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class ConfiguracionSeguridad {
    @Bean
    public CsrfTokenRepository tokensFormularios() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain seguridadFormularios(HttpSecurity http, CsrfTokenRepository tokens) throws Exception {
        // Spring protege los POST con CSRF. La autenticación se consulta a la API.
        http.authorizeHttpRequests(reglas -> reglas.anyRequest().permitAll())
                .csrf(csrf -> csrf.csrfTokenRepository(tokens))
                .formLogin(formulario -> formulario.disable())
                .httpBasic(basica -> basica.disable())
                .logout(salida -> salida.disable())
                .requestCache(cache -> cache.disable())
                .exceptionHandling(errores -> errores.accessDeniedHandler((solicitud, respuesta, error) -> {
                    respuesta.setStatus(403);
                    solicitud.setAttribute("mensaje",
                            "El formulario venció o no es válido. Volvé a abrir la página e intentá nuevamente.");
                    solicitud.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(solicitud, respuesta);
                }));
        return http.build();
    }
}
