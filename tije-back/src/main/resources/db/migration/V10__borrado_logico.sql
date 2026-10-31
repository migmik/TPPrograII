-- Borrado logico: cada tabla guarda cuando se creo cada fila y, si se dio de
-- baja, cuando. Las filas dadas de baja no se borran para conservar el historial.
ALTER TABLE sucursales ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE sucursales ADD COLUMN fecha_baja DATETIME(6) NULL;
ALTER TABLE hoteles ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE hoteles ADD COLUMN fecha_baja DATETIME(6) NULL;
ALTER TABLE vuelos ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE vuelos ADD COLUMN fecha_baja DATETIME(6) NULL;
ALTER TABLE turistas ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE turistas ADD COLUMN fecha_baja DATETIME(6) NULL;
ALTER TABLE usuarios ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE usuarios ADD COLUMN fecha_baja DATETIME(6) NULL;
ALTER TABLE reservas ADD COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE reservas ADD COLUMN fecha_baja DATETIME(6) NULL;

-- Los datos unicos (DNI, email, nombre de usuario, etc.) solo tienen que ser
-- unicos entre los registros activos. Cada columna "_activo" se calcula sola:
-- copia el dato mientras la fila esta activa y queda en NULL cuando se da de
-- baja. Como el indice unico admite varios NULL, una persona dada de baja no
-- impide registrar a otra con el mismo DNI.

-- Las claves foraneas necesitan un indice propio antes de quitar los unicos.
CREATE INDEX idx_reserva_turista ON reservas (turista_codigo);
CREATE INDEX idx_usuario_turista ON usuarios (turista_codigo);

ALTER TABLE sucursales DROP CONSTRAINT uk_sucursal_direccion;
ALTER TABLE sucursales ADD COLUMN direccion_activa VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN direccion END);
CREATE UNIQUE INDEX uk_sucursal_direccion ON sucursales (direccion_activa);

ALTER TABLE hoteles DROP CONSTRAINT uk_hotel_nombre_ciudad;
ALTER TABLE hoteles ADD COLUMN nombre_activo VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN nombre END);
CREATE UNIQUE INDEX uk_hotel_nombre_ciudad ON hoteles (nombre_activo, ciudad);

ALTER TABLE turistas DROP CONSTRAINT uk_turista_dni;
ALTER TABLE turistas DROP CONSTRAINT uk_turista_email;
ALTER TABLE turistas ADD COLUMN dni_activo VARCHAR(8)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN dni END);
ALTER TABLE turistas ADD COLUMN email_activo VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN email END);
CREATE UNIQUE INDEX uk_turista_dni ON turistas (dni_activo);
CREATE UNIQUE INDEX uk_turista_email ON turistas (email_activo);

ALTER TABLE usuarios DROP CONSTRAINT uk_usuario_nombre;
ALTER TABLE usuarios DROP CONSTRAINT uk_usuario_dni;
ALTER TABLE usuarios DROP CONSTRAINT uk_usuario_turista;
ALTER TABLE usuarios ADD COLUMN nombre_usuario_activo VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN nombre_usuario END);
ALTER TABLE usuarios ADD COLUMN dni_activo VARCHAR(8)
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN dni END);
ALTER TABLE usuarios ADD COLUMN turista_activo INT
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN turista_codigo END);
CREATE UNIQUE INDEX uk_usuario_nombre ON usuarios (nombre_usuario_activo);
CREATE UNIQUE INDEX uk_usuario_dni ON usuarios (dni_activo);
CREATE UNIQUE INDEX uk_usuario_turista ON usuarios (turista_activo);

ALTER TABLE reservas DROP CONSTRAINT uk_reserva_turista_vuelo;
ALTER TABLE reservas ADD COLUMN turista_activo INT
    GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN turista_codigo END);
CREATE UNIQUE INDEX uk_reserva_turista_vuelo ON reservas (turista_activo, vuelo_numero);
