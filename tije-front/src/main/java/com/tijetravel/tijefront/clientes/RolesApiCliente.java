package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.PermisoRespuesta;
import com.tijetravel.tijefront.dto.RolRespuesta;
import com.tijetravel.tijefront.formularios.GuardarRolFormulario;

@Service
public class RolesApiCliente {
    private final ConexionApiSesion conexion;

    public RolesApiCliente(ConexionApiSesion conexion) {
        this.conexion = conexion;
    }

    public List<RolRespuesta> listar() {
        RolRespuesta[] roles = conexion.getCliente().get().uri("/api/v1/roles")
                .retrieve().body(RolRespuesta[].class);
        if (roles == null) {
            throw new RestClientException("La API no devolvió los roles.");
        }
        return Arrays.asList(roles);
    }

    public List<PermisoRespuesta> listarPermisos() {
        PermisoRespuesta[] permisos = conexion.getCliente().get().uri("/api/v1/roles/permisos")
                .retrieve().body(PermisoRespuesta[].class);
        if (permisos == null) {
            throw new RestClientException("La API no devolvió los permisos.");
        }
        return Arrays.asList(permisos);
    }

    public RolRespuesta buscar(Integer codigo) {
        RolRespuesta rol = conexion.getCliente().get().uri("/api/v1/roles/{codigo}", codigo)
                .retrieve().body(RolRespuesta.class);
        if (rol == null) {
            throw new RestClientException("La API no devolvió el rol.");
        }
        return rol;
    }

    public void crear(GuardarRolFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().post().uri("/api/v1/roles")
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void modificar(Integer codigo, GuardarRolFormulario formulario) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().put().uri("/api/v1/roles/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .body(formulario).retrieve().toBodilessEntity();
    }

    public void eliminar(Integer codigo) {
        CsrfRespuesta csrf = conexion.obtenerCsrf();
        conexion.getCliente().delete().uri("/api/v1/roles/{codigo}", codigo)
                .header(csrf.getNombreEncabezado(), csrf.getToken())
                .retrieve().toBodilessEntity();
    }
}
