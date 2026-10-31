package com.tijetravel.tijefront.controladores;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tijetravel.tijefront.dto.RegistroDadoDeBaja;

/**
 * Modulos que tienen registros dados de baja. Cada uno sabe su ruta en la API,
 * el permiso que pide y como describir un registro para el listado.
 */
public enum ModuloBajas {
    SUCURSALES("sucursales", "Sucursales", "ADMINISTRAR_SUCURSALES") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return registro.getDireccion() + " (" + registro.getCiudad() + ")";
        }
    },
    HOTELES("hoteles", "Hoteles", "ADMINISTRAR_HOTELES") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return registro.getNombre() + " (" + registro.getCiudad() + ")";
        }
    },
    VUELOS("vuelos", "Vuelos", "ADMINISTRAR_VUELOS") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return "Vuelo " + registro.getNumero() + ": " + registro.getOrigen() + " a " + registro.getDestino()
                    + ", " + registro.getFechaYHora().format(FORMATO_FECHA_HORA);
        }
    },
    TURISTAS("turistas", "Turistas", "ADMINISTRAR_TURISTAS") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return registro.getNombre() + " " + registro.getApellido() + " - DNI " + registro.getDni();
        }
    },
    RESERVAS("reservas", "Reservas", "ADMINISTRAR_RESERVAS") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return "Turista " + registro.getCodigoTurista() + ", vuelo " + registro.getNumeroVuelo()
                    + ", hotel " + registro.getCodigoHotel() + ", llegada "
                    + registro.getFechaLlegada().format(FORMATO_FECHA);
        }
    },
    USUARIOS("usuarios", "Usuarios", "ADMINISTRAR_USUARIOS") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return registro.getNombreUsuario() + " (" + registro.getRol() + ")";
        }
    },
    ROLES("roles", "Roles", "ADMINISTRAR_USUARIOS") {
        @Override
        public String describir(RegistroDadoDeBaja registro) {
            return registro.getNombre();
        }
    };

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String ruta;
    private final String titulo;
    private final String permiso;

    ModuloBajas(String ruta, String titulo, String permiso) {
        this.ruta = ruta;
        this.titulo = titulo;
        this.permiso = permiso;
    }

    /** Texto corto que identifica el registro en el listado de bajas. */
    public abstract String describir(RegistroDadoDeBaja registro);

    /** Busca el modulo por su ruta (por ejemplo "hoteles"), o null si no existe. */
    public static ModuloBajas desdeRuta(String ruta) {
        for (ModuloBajas modulo : values()) {
            if (modulo.ruta.equals(ruta)) {
                return modulo;
            }
        }
        return null;
    }

    public static String formatear(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA_HORA);
    }

    public String getRuta() {
        return ruta;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPermiso() {
        return permiso;
    }
}
