ALTER TABLE hoteles DROP CONSTRAINT chk_hotel_plazas_no_negativas;
ALTER TABLE hoteles RENAME COLUMN plazas_disponibles TO capacidad_total;
ALTER TABLE hoteles ADD CONSTRAINT chk_hotel_capacidad_no_negativa CHECK (capacidad_total >= 0);
