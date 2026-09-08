package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SucursalRespuesta;
import com.tijetravel.tijeback.modelos.Sucursal;

@Component
public class SucursalMapeador {

    public SucursalRespuesta aRespuesta(Sucursal sucursal) {
        return new SucursalRespuesta(
                sucursal.getCodigo(),
                sucursal.getDireccion(),
                sucursal.getTelefono());
    }
}
