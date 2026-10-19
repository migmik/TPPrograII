ALTER TABLE vuelos ADD COLUMN precio INT NULL;
ALTER TABLE hoteles ADD COLUMN precio_noche_persona INT NULL;

-- Los registros anteriores reciben precios de ejemplo dentro de los rangos acordados.
UPDATE vuelos SET precio = 150000 + FLOOR(RAND() * 16) * 10000;
UPDATE hoteles SET precio_noche_persona = 25000 + FLOOR(RAND() * 31) * 1000;

ALTER TABLE vuelos MODIFY COLUMN precio INT NOT NULL;
ALTER TABLE hoteles MODIFY COLUMN precio_noche_persona INT NOT NULL;
ALTER TABLE vuelos ADD CONSTRAINT chk_vuelo_precio
    CHECK (precio BETWEEN 150000 AND 300000 AND MOD(precio, 10000) = 0);
ALTER TABLE hoteles ADD CONSTRAINT chk_hotel_precio
    CHECK (precio_noche_persona BETWEEN 25000 AND 55000 AND MOD(precio_noche_persona, 1000) = 0);

ALTER TABLE reservas ADD COLUMN precio_vuelo INT NULL;
ALTER TABLE reservas ADD COLUMN precio_hotel INT NULL;
ALTER TABLE reservas ADD COLUMN total INT NULL;
ALTER TABLE reservas ADD COLUMN pagada BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservas ADD COLUMN fecha_pago DATETIME(6) NULL;

UPDATE reservas SET
    precio_vuelo = (SELECT precio FROM vuelos WHERE numero = reservas.vuelo_numero)
        * (CASE WHEN clase_vuelo = 'PRIMERA' THEN 110 ELSE 100 END) / 100,
    precio_hotel = (SELECT precio_noche_persona FROM hoteles WHERE codigo = reservas.hotel_codigo)
        * TIMESTAMPDIFF(DAY, fecha_llegada, fecha_partida)
        * (CASE WHEN tipo_hospedaje = 'PENSION_COMPLETA' THEN 105 ELSE 100 END) / 100;
UPDATE reservas SET total = precio_vuelo + precio_hotel;

ALTER TABLE reservas MODIFY COLUMN precio_vuelo INT NOT NULL;
ALTER TABLE reservas MODIFY COLUMN precio_hotel INT NOT NULL;
ALTER TABLE reservas MODIFY COLUMN total INT NOT NULL;
ALTER TABLE reservas ADD CONSTRAINT chk_reserva_importes
    CHECK (precio_vuelo > 0 AND precio_hotel > 0 AND total = precio_vuelo + precio_hotel);
