ALTER TABLE sucursales ADD COLUMN ciudad VARCHAR(100) NULL;

UPDATE sucursales SET ciudad = CASE
    WHEN LOWER(direccion) LIKE '%buenos aires%' THEN 'Buenos Aires'
    WHEN LOWER(direccion) LIKE '%córdoba%' OR LOWER(direccion) LIKE '%cordoba%' THEN 'Córdoba'
    WHEN LOWER(direccion) LIKE '%bariloche%' THEN 'Bariloche'
    WHEN LOWER(direccion) LIKE '%salta%' THEN 'Salta'
    ELSE 'Sin especificar'
END;

ALTER TABLE sucursales MODIFY COLUMN ciudad VARCHAR(100) NOT NULL;
CREATE INDEX idx_sucursales_ciudad ON sucursales (ciudad);
