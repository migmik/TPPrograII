-- Los documentos anteriores quedan pendientes hasta conocer el dato real.
-- El DNI del cliente se obtiene del turista, no se guarda otra copia.
ALTER TABLE usuarios ADD COLUMN dni VARCHAR(8) NULL;
ALTER TABLE usuarios ADD CONSTRAINT uk_usuario_dni UNIQUE (dni);
ALTER TABLE usuarios ADD CONSTRAINT chk_usuario_dni
    CHECK (dni IS NULL OR (CHAR_LENGTH(dni) BETWEEN 7 AND 8 AND dni NOT REGEXP '[^0-9]'));
ALTER TABLE usuarios ADD CONSTRAINT chk_cliente_sin_dni_duplicado
    CHECK (tipo_usuario <> 'CLIENTE' OR dni IS NULL);
