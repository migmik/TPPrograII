package com.tijetravel.tijeback;

import java.util.concurrent.atomic.AtomicInteger;

/** Documentos ficticios y distintos para los datos de las pruebas. */
public final class DnisPrueba {
    private static final AtomicInteger SECUENCIA = new AtomicInteger(80000000);
    private DnisPrueba() { }
    public static String siguiente() { return String.valueOf(SECUENCIA.incrementAndGet()); }
}
