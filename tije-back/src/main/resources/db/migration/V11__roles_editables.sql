-- Los permisos dejan de estar fijos en el codigo: cada usuario tiene un rol y
-- cada rol guarda los permisos que el administrador le tilda.
CREATE TABLE roles (
    codigo INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    tipo_usuario VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_baja DATETIME(6) NULL,
    nombre_activo VARCHAR(60) GENERATED ALWAYS AS (CASE WHEN fecha_baja IS NULL THEN nombre END),
    CONSTRAINT pk_roles PRIMARY KEY (codigo),
    CONSTRAINT chk_rol_tipo_usuario CHECK (tipo_usuario IN ('EMPLEADO', 'CLIENTE'))
);
CREATE UNIQUE INDEX uk_rol_nombre ON roles (nombre_activo);

CREATE TABLE rol_permisos (
    rol_codigo INT NOT NULL,
    permiso VARCHAR(40) NOT NULL,
    CONSTRAINT pk_rol_permisos PRIMARY KEY (rol_codigo, permiso),
    CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_codigo) REFERENCES roles (codigo)
);

-- Roles iniciales con los mismos permisos que tenian los tres tipos de usuario.
INSERT INTO roles (nombre, tipo_usuario) VALUES ('Administrador', 'EMPLEADO');
INSERT INTO roles (nombre, tipo_usuario) VALUES ('Vendedor', 'EMPLEADO');
INSERT INTO roles (nombre, tipo_usuario) VALUES ('Cliente', 'CLIENTE');

INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CONSULTAR' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CREAR_RESERVAS' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'VER_CATALOGO_COMPLETO' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'VER_BALANCE' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_TURISTAS' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_RESERVAS' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_SUCURSALES' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_HOTELES' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_VUELOS' FROM roles WHERE nombre = 'Administrador';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_USUARIOS' FROM roles WHERE nombre = 'Administrador';

INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CONSULTAR' FROM roles WHERE nombre = 'Vendedor';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CREAR_RESERVAS' FROM roles WHERE nombre = 'Vendedor';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'VER_CATALOGO_COMPLETO' FROM roles WHERE nombre = 'Vendedor';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_TURISTAS' FROM roles WHERE nombre = 'Vendedor';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'ADMINISTRAR_RESERVAS' FROM roles WHERE nombre = 'Vendedor';

INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CONSULTAR' FROM roles WHERE nombre = 'Cliente';
INSERT INTO rol_permisos (rol_codigo, permiso) SELECT codigo, 'CREAR_RESERVAS' FROM roles WHERE nombre = 'Cliente';

-- Cada usuario pasa a tener el rol equivalente al que tenia.
ALTER TABLE usuarios ADD COLUMN rol_codigo INT NULL;
UPDATE usuarios SET rol_codigo = (SELECT codigo FROM roles WHERE nombre = 'Administrador') WHERE rol = 'ADMINISTRADOR';
UPDATE usuarios SET rol_codigo = (SELECT codigo FROM roles WHERE nombre = 'Vendedor') WHERE rol = 'VENDEDOR';
UPDATE usuarios SET rol_codigo = (SELECT codigo FROM roles WHERE nombre = 'Cliente') WHERE rol = 'CLIENTE';

-- Administrador y Vendedor quedan como un solo tipo de usuario: Empleado.
ALTER TABLE usuarios DROP CONSTRAINT chk_usuario_tipo_rol;
ALTER TABLE usuarios DROP CONSTRAINT chk_usuario_tipo;
ALTER TABLE usuarios DROP CONSTRAINT chk_usuario_asociacion_turista;
UPDATE usuarios SET tipo_usuario = 'EMPLEADO' WHERE tipo_usuario IN ('ADMINISTRADOR', 'VENDEDOR');
ALTER TABLE usuarios DROP COLUMN rol;
ALTER TABLE usuarios MODIFY COLUMN rol_codigo INT NOT NULL;
ALTER TABLE usuarios ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_codigo) REFERENCES roles (codigo);
ALTER TABLE usuarios ADD CONSTRAINT chk_usuario_tipo CHECK (tipo_usuario IN ('EMPLEADO', 'CLIENTE'));
ALTER TABLE usuarios ADD CONSTRAINT chk_usuario_asociacion_turista CHECK (
    (tipo_usuario = 'CLIENTE' AND turista_codigo IS NOT NULL)
    OR (tipo_usuario = 'EMPLEADO' AND turista_codigo IS NULL)
);
