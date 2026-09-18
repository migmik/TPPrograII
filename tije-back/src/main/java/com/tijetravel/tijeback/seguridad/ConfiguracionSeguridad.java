package com.tijetravel.tijeback.seguridad;

import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

        @Bean
        public PasswordEncoder codificadorContrasenias() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public AuthenticationManager administradorAutenticacion(
                        UsuarioDetallesServicio usuarioDetallesServicio,
                        PasswordEncoder codificadorContrasenias) {
                DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarioDetallesServicio);
                proveedor.setPasswordEncoder(codificadorContrasenias);
                return new ProviderManager(proveedor);
        }

        @Bean
        public SecurityContextRepository repositorioContextoSeguridad() {
                return new HttpSessionSecurityContextRepository();
        }

        @Bean
        public CsrfTokenRepository repositorioTokenCsrf() {
                return new HttpSessionCsrfTokenRepository();
        }

        @Bean
        public SessionAuthenticationStrategy estrategiaAutenticacionSesion(
                        CsrfTokenRepository repositorioTokenCsrf) {
                return new CompositeSessionAuthenticationStrategy(List.of(
                                new ChangeSessionIdAuthenticationStrategy(),
                                new CsrfAuthenticationStrategy(repositorioTokenCsrf)));
        }

        @Bean
        public CorsConfigurationSource configuracionCors(
                        @Value("${app.seguridad.origen-frontend}") String origenFrontend) {
                CorsConfiguration configuracion = new CorsConfiguration();
                configuracion.setAllowedOrigins(List.of(origenFrontend));
                configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuracion.setAllowedHeaders(List.of("Accept", "Content-Type", "X-CSRF-TOKEN"));
                configuracion.setAllowCredentials(true);
                configuracion.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
                fuente.registerCorsConfiguration("/api/**", configuracion);
                return fuente;
        }

        @Bean
        public SecurityFilterChain cadenaFiltrosSeguridad(
                        HttpSecurity http,
                        SecurityContextRepository repositorioContextoSeguridad,
                        CsrfTokenRepository repositorioTokenCsrf,
                        CorsConfigurationSource configuracionCors,
                        EscritorErrorSeguridad escritorErrorSeguridad) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(configuracionCors))
                                .csrf(csrf -> csrf.csrfTokenRepository(repositorioTokenCsrf))
                                .securityContext(contexto -> contexto
                                                .securityContextRepository(repositorioContextoSeguridad)
                                                .requireExplicitSave(true))
                                .sessionManagement(sesion -> sesion
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .requestCache(cache -> cache.disable())
                                .formLogin(formulario -> formulario.disable())
                                .httpBasic(basica -> basica.disable())
                                .logout(salida -> salida
                                                .logoutUrl("/api/v1/autenticacion/logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("TIJESESSION")
                                                .logoutSuccessHandler((solicitud, respuesta, autenticacion) -> respuesta
                                                                .setStatus(HttpStatus.NO_CONTENT.value())))
                                .exceptionHandling(errores -> errores
                                                .authenticationEntryPoint((solicitud, respuesta,
                                                                excepcion) -> escritorErrorSeguridad.escribir(
                                                                                solicitud,
                                                                                respuesta,
                                                                                HttpStatus.UNAUTHORIZED,
                                                                                "AUTENTICACION_REQUERIDA",
                                                                                "Es necesario iniciar sesion"))
                                                .accessDeniedHandler((solicitud, respuesta,
                                                                excepcion) -> escritorErrorSeguridad.escribir(
                                                                                solicitud,
                                                                                respuesta,
                                                                                HttpStatus.FORBIDDEN,
                                                                                "ACCESO_DENEGADO",
                                                                                "No tiene permiso para realizar esta operacion")))
                                .authorizeHttpRequests(autorizacion -> autorizacion
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(
                                                                "/api/v1/autenticacion/login",
                                                                "/api/v1/autenticacion/csrf",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/sucursales/**",
                                                                "/api/v1/hoteles/**",
                                                                "/api/v1/vuelos/**")
                                                .permitAll()
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/turistas/**",
                                                                "/api/v1/reservas/**")
                                                .hasAuthority(Permiso.CONSULTAR.name())
                                                .requestMatchers("/api/v1/usuarios/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_USUARIOS.name())
                                                .requestMatchers("/api/v1/sucursales/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_SUCURSALES.name())
                                                .requestMatchers("/api/v1/hoteles/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_HOTELES.name())
                                                .requestMatchers("/api/v1/vuelos/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_VUELOS.name())
                                                .requestMatchers("/api/v1/turistas/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_TURISTAS.name())
                                                .requestMatchers("/api/v1/reservas/**")
                                                .hasAuthority(Permiso.ADMINISTRAR_RESERVAS.name())
                                                .anyRequest().authenticated());
                return http.build();
        }
}
