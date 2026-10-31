package com.tijetravel.tijefront.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

/**
 * En el frontend Spring Security solo protege los formularios con CSRF. Quien
 * puede hacer cada cosa lo decide la API.
 */
@Configuration
public class ConfiguracionSeguridad {

    @Bean
    public CsrfTokenRepository tokensFormularios() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain seguridadFormularios(HttpSecurity http, final CsrfTokenRepository tokens)
            throws Exception {
        http.authorizeHttpRequests(
                new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
                    @Override
                    public void customize(
                            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry reglas) {
                        reglas.anyRequest().permitAll();
                    }
                });

        http.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> csrf) {
                csrf.csrfTokenRepository(tokens);
            }
        });

        // El cierre de sesion lo maneja SesionControlador, no Spring.
        http.logout(new Customizer<LogoutConfigurer<HttpSecurity>>() {
            @Override
            public void customize(LogoutConfigurer<HttpSecurity> salida) {
                salida.disable();
            }
        });

        http.requestCache(new Customizer<RequestCacheConfigurer<HttpSecurity>>() {
            @Override
            public void customize(RequestCacheConfigurer<HttpSecurity> cache) {
                cache.disable();
            }
        });

        http.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
            @Override
            public void customize(ExceptionHandlingConfigurer<HttpSecurity> errores) {
                errores.accessDeniedHandler(new ManejadorFormularioVencido());
            }
        });

        return http.build();
    }
}
