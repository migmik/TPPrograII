package com.tijetravel.tijeback.modelos;

import java.util.regex.Pattern;

public final class ValidacionModelo {
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ValidacionModelo() {
    }

    public static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    public static String email(String valor) {
        String email = textoObligatorio(valor, "email");
        if (!EMAIL_VALIDO.matcher(email).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
        return email;
    }

    static int enteroPositivo(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser mayor que cero");
        }
        return valor;
    }

    static int enteroNoNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser negativo");
        }
        return valor;
    }

    static <T> T obligatorio(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor;
    }
}
