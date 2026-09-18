package com.tijetravel.tijeback.seguridad;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Component
@ConditionalOnProperty(prefix = "app.seguridad.administrador-inicial", name = "habilitado", havingValue = "true")
public class AdministradorInicializador implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministradorInicializador.class);

    private final BloqueoEscrituras bloqueoEscrituras;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorContrasenias;
    private final String nombreUsuario;
    private final String contrasenia;

    public AdministradorInicializador(
            BloqueoEscrituras bloqueoEscrituras,
            UsuarioRepositorio usuarioRepositorio,
            PasswordEncoder codificadorContrasenias,
            @Value("${app.seguridad.administrador-inicial.nombre-usuario:}") String nombreUsuario,
            @Value("${app.seguridad.administrador-inicial.contrasenia:}") String contrasenia) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.usuarioRepositorio = usuarioRepositorio;
        this.codificadorContrasenias = codificadorContrasenias;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(ApplicationArguments argumentos) {
        bloqueoEscrituras.adquirir();
        if (usuarioRepositorio.countByRol(RolUsuario.ADMINISTRADOR) > 0) {
            return;
        }

        String nombreNormalizado = validarConfiguracion();
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCase(nombreNormalizado)) {
            throw new IllegalStateException(
                    "El nombre configurado para el administrador inicial ya esta en uso");
        }

        usuarioRepositorio.save(new Administrador(
                nombreNormalizado,
                codificadorContrasenias.encode(contrasenia)));
        LOGGER.info("Se creo el administrador inicial {}", nombreNormalizado);
    }

    private String validarConfiguracion() {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_USERNAME es obligatorio al habilitar el administrador inicial");
        }
        if (contrasenia == null || contrasenia.length() < 12) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD debe contener al menos 12 caracteres");
        }
        if (contrasenia.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD no puede superar 72 bytes");
        }
        return nombreUsuario.trim();
    }
}
