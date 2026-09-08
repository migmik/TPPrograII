package com.tijetravel.tijeback.servicios.usuarios;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class UsuarioFactory {
    private final Map<RolUsuario, CreadorUsuario> creadores = new EnumMap<>(RolUsuario.class);

    public UsuarioFactory(List<CreadorUsuario> implementaciones) {
        for (CreadorUsuario creador : implementaciones) {
            if (creador.rol() == null || creadores.putIfAbsent(creador.rol(), creador) != null) {
                throw new IllegalStateException("Cada creador debe declarar un rol unico");
            }
        }
        for (RolUsuario rol : RolUsuario.values()) {
            if (!creadores.containsKey(rol)) {
                throw new IllegalStateException("Falta un creador para " + rol);
            }
        }
    }

    public Usuario crear(String nombre, String hash, RolUsuario rol, Turista turista) {
        if (rol == null) throw new IllegalArgumentException("El rol es obligatorio");
        return creadores.get(rol).crear(nombre, hash, turista);
    }
}
