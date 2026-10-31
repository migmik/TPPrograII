package com.tijetravel.tijeback.servicios.usuarios;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Fabrica de usuarios: segun el tipo de usuario del rol elige el creador que
 * corresponde (patron Factory con Strategy).
 */
@Component
public class UsuarioFactory {
    private final Map<TipoUsuario, CreadorUsuario> creadores = new EnumMap<>(TipoUsuario.class);

    public UsuarioFactory(List<CreadorUsuario> implementaciones) {
        for (CreadorUsuario creador : implementaciones) {
            if (creador.tipo() == null || creadores.putIfAbsent(creador.tipo(), creador) != null) {
                throw new IllegalStateException("Cada creador debe declarar un tipo de usuario unico");
            }
        }
        for (TipoUsuario tipo : TipoUsuario.values()) {
            if (!creadores.containsKey(tipo)) {
                throw new IllegalStateException("Falta un creador para " + tipo);
            }
        }
    }

    public Usuario crear(String nombre, String hash, Rol rol, Turista turista, String dni) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        return creadores.get(rol.getTipoUsuario()).crear(nombre, hash, rol, turista, dni);
    }
}
