-- No se inventan documentos para las personas que ya estaban registradas.
-- La API exige completar el DNI al crear o editar un turista.
ALTER TABLE turistas ADD COLUMN dni VARCHAR(8) NULL;
ALTER TABLE turistas ADD CONSTRAINT uk_turista_dni UNIQUE (dni);
ALTER TABLE turistas ADD CONSTRAINT chk_turista_dni
    CHECK (dni IS NULL OR (CHAR_LENGTH(dni) BETWEEN 7 AND 8 AND dni NOT REGEXP '[^0-9]'));
