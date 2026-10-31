package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.UsuarioRespuesta;
import com.tijetravel.tijefront.dto.PaginaUsuariosRespuesta;
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

    public PaginaUsuariosRespuesta listarPagina(String rol, Integer codigo, String nombre, String dni, int pagina) {
        PaginaUsuariosRespuesta resultado = conexion.getCliente().get().uri(uri -> {
            var ruta = uri.path("/api/v1/usuarios/pagina")
                    .queryParam("pagina", pagina).queryParam("tamanio", 20);
            if (rol != null && !rol.isBlank()) ruta.queryParam("rol", rol.trim());
            if (codigo != null) ruta.queryParam("codigo", codigo);
            if (nombre != null && !nombre.isBlank()) ruta.queryParam("nombre", nombre.trim());
            if (dni != null && !dni.isBlank()) ruta.queryParam("dni", dni.trim());
            return ruta.build();
        }).retrieve().body(PaginaUsuariosRespuesta.class);
        if (resultado == null) throw new RestClientException("La API no devolvió la página de usuarios.");
        return resultado;
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
