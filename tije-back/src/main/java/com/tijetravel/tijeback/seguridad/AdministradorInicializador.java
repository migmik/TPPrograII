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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.repositorios.RolRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;

@Component
@ConditionalOnProperty(prefix = "app.seguridad.administrador-inicial", name = "habilitado", havingValue = "true")
public class AdministradorInicializador implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministradorInicializador.class);

    private final BloqueoEscrituras bloqueoEscrituras;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder codificadorContrasenias;
    private final String nombreUsuario;
    private final String contrasenia;
    private final String dni;

    public AdministradorInicializador(
            BloqueoEscrituras bloqueoEscrituras,
            UsuarioRepositorio usuarioRepositorio,
            RolRepositorio rolRepositorio,
            PasswordEncoder codificadorContrasenias,
            @Value("${app.seguridad.administrador-inicial.nombre-usuario:}") String nombreUsuario,
            @Value("${app.seguridad.administrador-inicial.contrasenia:}") String contrasenia,
            @Value("${app.seguridad.administrador-inicial.dni:}") String dni) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.codificadorContrasenias = codificadorContrasenias;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.dni = dni;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(ApplicationArguments argumentos) {
        bloqueoEscrituras.adquirir();
        // Si ya hay alguien que puede administrar usuarios no hace falta crear otro.
        if (usuarioRepositorio.contarActivosConPermiso(Permiso.ADMINISTRAR_USUARIOS) > 0) {
            return;
        }

        String nombreNormalizado = validarConfiguracion();
        if (dni == null || !dni.matches("[0-9]{7,8}")) {
            throw new IllegalStateException("APP_ADMIN_DNI es obligatorio y debe tener 7 u 8 digitos");
        }
        if (usuarioRepositorio.existsByDniAndFechaBajaIsNull(dni)) {
            throw new IllegalStateException("El DNI del administrador inicial ya esta en uso");
        }
        if (usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNull(nombreNormalizado)) {
            throw new IllegalStateException(
                    "El nombre configurado para el administrador inicial ya esta en uso");
        }

        Rol rolAdministrador = rolRepositorio.findByNombreIgnoreCaseAndFechaBajaIsNull("Administrador");
        if (rolAdministrador == null || !rolAdministrador.tienePermiso(Permiso.ADMINISTRAR_USUARIOS)) {
            throw new IllegalStateException(
                    "Falta el rol Administrador con el permiso de administrar usuarios");
        }
        usuarioRepositorio.save(new Empleado(
                nombreNormalizado,
                codificadorContrasenias.encode(contrasenia), dni, rolAdministrador));
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
