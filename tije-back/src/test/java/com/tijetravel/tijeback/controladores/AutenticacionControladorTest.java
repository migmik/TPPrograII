package com.tijetravel.tijeback.controladores;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.excepciones.CredencialesInvalidasException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class AutenticacionControladorTest {
    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder codificadorContrasenias;

    private AutenticacionControlador controlador;

    @BeforeEach
    void prepararControlador() {
        controlador = new AutenticacionControlador(usuarioRepositorio, codificadorContrasenias);
    }

    @Test
    void comparaLaContraseniaContraElHash() {
        Administrador administrador = new Administrador("admin", "{bcrypt}hash");
        when(usuarioRepositorio.findByNombreUsuarioIgnoreCase("admin"))
                .thenReturn(Optional.of(administrador));
        when(codificadorContrasenias.matches("clave", "{bcrypt}hash")).thenReturn(true);

        assertSame(administrador, controlador.iniciarSesion(" admin ", "clave"));
    }

    @Test
    void rechazaUnaContraseniaQueNoCoincide() {
        Administrador administrador = new Administrador("admin", "{bcrypt}hash");
        when(usuarioRepositorio.findByNombreUsuarioIgnoreCase("admin"))
                .thenReturn(Optional.of(administrador));
        when(codificadorContrasenias.matches("incorrecta", "{bcrypt}hash")).thenReturn(false);

        assertThrows(
                CredencialesInvalidasException.class,
                () -> controlador.iniciarSesion("admin", "incorrecta"));
    }
}
