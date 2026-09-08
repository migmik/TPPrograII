INSERT INTO sucursales (codigo, direccion, telefono) VALUES
    (1, 'Av Corrientes 1234', '011-4321-1000'),
    (2, 'Av Santa Fe 2500', '011-4321-2000');

INSERT INTO hoteles (codigo, nombre, direccion, ciudad, telefono, plazas_disponibles) VALUES
    (1, 'Hotel Centro', 'Av Hotel 100', 'Buenos Aires', '011-4444-5555', 20),
    (2, 'Hotel Sierras', 'Ruta Provincial 5 Km 70', 'Cordoba', '0351-555-1111', 35),
    (3, 'Hotel Playa Sur', 'Costanera 900', 'Mar del Plata', '0223-555-2222', 18),
    (4, 'Hotel Taragui', 'Colon 456', 'Goya', '3777656565', 10);

INSERT INTO vuelos (
    numero,
    fecha_hora,
    origen,
    destino,
    total_plazas,
    plazas_turista,
    plazas_primera
) VALUES
    (100, '2026-08-15 10:30:00', 'Buenos Aires', 'Cordoba', 100, 80, 20),
    (101, '2026-09-05 08:15:00', 'Buenos Aires', 'Mar del Plata', 90, 70, 20),
    (102, '2026-10-12 19:45:00', 'Cordoba', 'Buenos Aires', 120, 95, 25),
    (103, '2026-11-01 20:00:00', 'Buenos Aires', 'Goya', 20, 15, 5);

INSERT INTO turistas (
    codigo,
    nombre,
    apellido,
    direccion,
    email,
    telefono_fijo,
    telefono_celular,
    sucursal_codigo,
    titular_codigo
) VALUES
    (1, 'Juan', 'Perez', 'Calle Falsa 123', 'juan.perez@mail.com', '4444-1111', '1122334455', 1, NULL),
    (3, 'Carlos', 'Gomez', 'Av Siempre Viva 742', 'carlos.gomez@mail.com', '4555-3333', '1199887766', 2, NULL);

INSERT INTO turistas (
    codigo,
    nombre,
    apellido,
    direccion,
    email,
    telefono_fijo,
    telefono_celular,
    sucursal_codigo,
    titular_codigo
) VALUES
    (2, 'Maria', 'Perez', 'Calle Falsa 123', 'maria.perez@mail.com', '4444-2222', '1166778899', 1, 1);

INSERT INTO reservas (
    codigo,
    turista_codigo,
    sucursal_codigo,
    vuelo_numero,
    hotel_codigo,
    clase_vuelo,
    tipo_hospedaje,
    fecha_llegada,
    fecha_partida
) VALUES
    (1, 1, 1, 100, 2, 'TURISTA', 'MEDIA_PENSION', '2026-08-15', '2026-08-20'),
    (2, 2, 1, 101, 3, 'TURISTA', 'PENSION_COMPLETA', '2026-09-05', '2026-09-10');

-- usuarios.txt is intentionally excluded because it stores plain-text passwords.
