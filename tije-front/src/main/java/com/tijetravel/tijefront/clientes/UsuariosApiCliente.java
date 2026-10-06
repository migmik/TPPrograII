package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.UsuarioRespuesta;
import com.tijetravel.tijefront.formularios.CrearUsuarioFormulario;
import com.tijetravel.tijefront.formularios.ModificarUsuarioFormulario;

@Service
public class UsuariosApiCliente {
    private final ConexionApiSesion conexion;

    public UsuariosApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<UsuarioRespuesta> listar() {
        return listar(null);
    }

    public List<UsuarioRespuesta> listar(String rol) {
        UsuarioRespuesta[] usuarios = conexion.getCliente().get().uri(uriBuilder -> {
            var ruta = uriBuilder.path("/api/v1/usuarios");
            if (rol != null && !rol.isBlank()) {
                ruta.queryParam("rol", rol.trim());
            }
            return ruta.build();
        })
                .retrieve().body(UsuarioRespuesta[].class);
        if (usuarios == null) {
            throw new RestClientException("La API no devolvió el listado de usuarios.");
        }
        return Arrays.asList(usuarios);
    }

    public void crear(CrearUsuarioFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/usuarios")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public UsuarioRespuesta buscar(Integer codigo) {
        UsuarioRespuesta usuario = conexion.getCliente().get().uri("/api/v1/usuarios/{codigo}", codigo)
                .retrieve().body(UsuarioRespuesta.class);
        if (usuario == null) {
            throw new RestClientException("La API no devolvió el usuario.");
        }
        return usuario;
    }

    public void modificar(Integer codigo, ModificarUsuarioFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/usuarios/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/usuarios/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }
}
