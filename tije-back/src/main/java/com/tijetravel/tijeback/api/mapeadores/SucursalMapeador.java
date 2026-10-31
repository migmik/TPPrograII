package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SucursalRespuesta;
import com.tijetravel.tijeback.modelos.Sucursal;

@Component
public class SucursalMapeador extends MapeadorBase<Sucursal, SucursalRespuesta> {

    @Override
    public SucursalRespuesta aRespuesta(Sucursal sucursal) {
        return new SucursalRespuesta(
                sucursal.getCodigo(),
                sucursal.getDireccion(),
                sucursal.getTelefono(),
                sucursal.getCiudad(),
                sucursal.getFechaCreacion(),
                sucursal.getFechaBaja());
    }
}
