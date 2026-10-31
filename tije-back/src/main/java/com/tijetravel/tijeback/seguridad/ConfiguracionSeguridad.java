package com.tijetravel.tijeback.seguridad;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
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

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

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
    public SessionAuthenticationStrategy estrategiaAutenticacionSesion(CsrfTokenRepository repositorioTokenCsrf) {
        List<SessionAuthenticationStrategy> estrategias = new ArrayList<>();
        estrategias.add(new ChangeSessionIdAuthenticationStrategy());
        estrategias.add(new CsrfAuthenticationStrategy(repositorioTokenCsrf));
        return new CompositeSessionAuthenticationStrategy(estrategias);
    }

    @Bean
    public CorsConfigurationSource configuracionCors(@Value("${app.seguridad.origen-frontend}") String origenFrontend) {
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

    /**
     * Cadena de filtros de la API. Cada parte se configura con una clase anonima
     * que implementa Customizer (la forma que pide Spring Security 7).
     */
    @Bean
    public SecurityFilterChain cadenaFiltrosSeguridad(
            HttpSecurity http,
            final SecurityContextRepository repositorioContextoSeguridad,
            final CsrfTokenRepository repositorioTokenCsrf,
            final CorsConfigurationSource configuracionCors,
            final EscritorErrorSeguridad escritorErrorSeguridad,
            UsuarioRepositorio usuarioRepositorio) throws Exception {

        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> cors) {
                cors.configurationSource(configuracionCors);
            }
        });

        http.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> csrf) {
                csrf.csrfTokenRepository(repositorioTokenCsrf);
            }
        });

        // La sesion solo se guarda cuando el login lo pide explicitamente.
        http.securityContext(new Customizer<SecurityContextConfigurer<HttpSecurity>>() {
            @Override
            public void customize(SecurityContextConfigurer<HttpSecurity> contexto) {
                contexto.securityContextRepository(repositorioContextoSeguridad);
                contexto.requireExplicitSave(true);
            }
        });

        // Es una API: no hace falta recordar la pagina pedida antes del login.
        http.requestCache(new Customizer<RequestCacheConfigurer<HttpSecurity>>() {
            @Override
            public void customize(RequestCacheConfigurer<HttpSecurity> cache) {
                cache.disable();
            }
        });

        http.logout(new Customizer<LogoutConfigurer<HttpSecurity>>() {
            @Override
            public void customize(LogoutConfigurer<HttpSecurity> salida) {
                salida.logoutUrl("/api/v1/autenticacion/logout");
                salida.invalidateHttpSession(true);
                salida.clearAuthentication(true);
                salida.deleteCookies("TIJESESSION");
                salida.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT));
            }
        });

        http.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
            @Override
            public void customize(ExceptionHandlingConfigurer<HttpSecurity> errores) {
                errores.authenticationEntryPoint(escritorErrorSeguridad);
                errores.accessDeniedHandler(escritorErrorSeguridad);
            }
        });

        http.authorizeHttpRequests(
                new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
                    @Override
                    public void customize(
                            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry reglas) {
                        configurarPermisos(reglas);
                    }
                });

        // Antes de autorizar se recargan los permisos por si cambio el rol del usuario.
        http.addFilterBefore(new ActualizacionPermisosFiltro(usuarioRepositorio), AuthorizationFilter.class);

        return http.build();
    }

    /** Reglas de acceso por URL. Se evaluan en orden: gana la primera que coincide. */
    private void configurarPermisos(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry reglas) {
        reglas.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        reglas.requestMatchers(
                "/api/v1/autenticacion/login",
                "/api/v1/autenticacion/registro",
                "/api/v1/autenticacion/csrf",
                "/error").permitAll();

        // Los dados de baja piden sesion; el servicio controla el permiso de cada modulo.
        reglas.requestMatchers(HttpMethod.GET, "/api/v1/*/bajas").authenticated();

        // Catalogo publico (el listado completo pide sesion y lo controla el servicio).
        reglas.requestMatchers(HttpMethod.GET, "/api/v1/hoteles", "/api/v1/vuelos").authenticated();
        reglas.requestMatchers(HttpMethod.GET,
                "/api/v1/ciudades",
                "/api/v1/sucursales/**",
                "/api/v1/hoteles/**",
                "/api/v1/vuelos/**").permitAll();

        reglas.requestMatchers(HttpMethod.GET, "/api/v1/turistas/**", "/api/v1/reservas/**")
                .hasAuthority(Permiso.CONSULTAR.name());
        reglas.requestMatchers("/api/v1/usuarios/**", "/api/v1/roles/**")
                .hasAuthority(Permiso.ADMINISTRAR_USUARIOS.name());
        reglas.requestMatchers("/api/v1/sucursales/**").hasAuthority(Permiso.ADMINISTRAR_SUCURSALES.name());
        reglas.requestMatchers("/api/v1/hoteles/**").hasAuthority(Permiso.ADMINISTRAR_HOTELES.name());
        reglas.requestMatchers("/api/v1/vuelos/**").hasAuthority(Permiso.ADMINISTRAR_VUELOS.name());
        reglas.requestMatchers(HttpMethod.POST, "/api/v1/reservas", "/api/v1/reservas/*/pagar")
                .hasAuthority(Permiso.CREAR_RESERVAS.name());
        reglas.requestMatchers("/api/v1/turistas/**").hasAuthority(Permiso.ADMINISTRAR_TURISTAS.name());
        reglas.requestMatchers("/api/v1/reservas/**").hasAuthority(Permiso.ADMINISTRAR_RESERVAS.name());
        reglas.anyRequest().authenticated();
    }
}
