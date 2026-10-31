package com.tijetravel.tijeback.seguridad;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;

class AdministradorInicializadorTest {
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private AdministradorInicializador inicializador(String dni) {
        return new AdministradorInicializador(mock(BloqueoEscrituras.class), usuarios, encoder,
                "admin", "ClaveSoloDePrueba123!", dni);
    }

    @Test
    void noCreaElPrimerAdministradorSinDni() {
        try {
            inicializador(null).run(new DefaultApplicationArguments());
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException esperada) {
            // Es la excepcion que se esperaba.
        }
        verify(usuarios, never()).save(any());
    }

    @Test
    void guardaElDniConfigurado() {
        when(encoder.encode(any())).thenReturn("hash");
        inicializador("12345678").run(new DefaultApplicationArguments());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertEquals("12345678", captor.getValue().getDni());
    }

    @Test
    void noAlteraElAdministradorExistenteAunqueFalteLaVariableNueva() {
        when(usuarios.countByRol(RolUsuario.ADMINISTRADOR)).thenReturn(1L);
        inicializador(null).run(new DefaultApplicationArguments());
        verify(usuarios, never()).save(any());
    }
}
