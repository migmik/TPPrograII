CREATE TABLE sucursales (
    codigo INT NOT NULL AUTO_INCREMENT,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(255) NOT NULL,
    CONSTRAINT pk_sucursales PRIMARY KEY (codigo),
    CONSTRAINT uk_sucursal_direccion UNIQUE (direccion)
);

CREATE TABLE hoteles (
    codigo INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(255) NOT NULL,
    telefono VARCHAR(255) NOT NULL,
    plazas_disponibles INT NOT NULL,
    CONSTRAINT pk_hoteles PRIMARY KEY (codigo),
    CONSTRAINT uk_hotel_nombre_ciudad UNIQUE (nombre, ciudad),
    CONSTRAINT chk_hotel_plazas_no_negativas CHECK (plazas_disponibles >= 0)
);

CREATE TABLE vuelos (
    numero INT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    origen VARCHAR(255) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    total_plazas INT NOT NULL,
    plazas_turista INT NOT NULL,
    plazas_primera INT NOT NULL,
    CONSTRAINT pk_vuelos PRIMARY KEY (numero),
    CONSTRAINT chk_vuelo_numero_positivo CHECK (numero > 0),
    CONSTRAINT chk_vuelo_total_positivo CHECK (total_plazas > 0),
    CONSTRAINT chk_vuelo_plazas_turista CHECK (plazas_turista >= 0),
    CONSTRAINT chk_vuelo_plazas_primera CHECK (plazas_primera >= 0),
    CONSTRAINT chk_vuelo_distribucion_plazas
        CHECK (plazas_turista + plazas_primera <= total_plazas)
);

CREATE TABLE turistas (
    codigo INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono_fijo VARCHAR(255) NOT NULL,
    telefono_celular VARCHAR(255) NOT NULL,
    sucursal_codigo INT NOT NULL,
    titular_codigo INT NULL,
    CONSTRAINT pk_turistas PRIMARY KEY (codigo),
    CONSTRAINT uk_turista_email UNIQUE (email),
    CONSTRAINT fk_turista_sucursal
        FOREIGN KEY (sucursal_codigo) REFERENCES sucursales (codigo),
    CONSTRAINT fk_turista_titular
        FOREIGN KEY (titular_codigo) REFERENCES turistas (codigo)
);

CREATE INDEX idx_turista_sucursal ON turistas (sucursal_codigo);
CREATE INDEX idx_turista_titular ON turistas (titular_codigo);

CREATE TABLE usuarios (
    codigo INT NOT NULL AUTO_INCREMENT,
    tipo_usuario VARCHAR(31) NOT NULL,
    nombre_usuario VARCHAR(255) NOT NULL,
    contrasenia VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'CLIENTE', 'VENDEDOR') NOT NULL,
    turista_codigo INT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (codigo),
    CONSTRAINT uk_usuario_nombre UNIQUE (nombre_usuario),
    CONSTRAINT uk_usuario_turista UNIQUE (turista_codigo),
    CONSTRAINT fk_usuario_turista
        FOREIGN KEY (turista_codigo) REFERENCES turistas (codigo),
    CONSTRAINT chk_usuario_tipo
        CHECK (tipo_usuario IN ('ADMINISTRADOR', 'CLIENTE', 'VENDEDOR')),
    CONSTRAINT chk_usuario_tipo_rol CHECK (tipo_usuario = rol),
    CONSTRAINT chk_usuario_asociacion_turista CHECK (
        (tipo_usuario = 'CLIENTE' AND turista_codigo IS NOT NULL)
        OR
        (tipo_usuario IN ('ADMINISTRADOR', 'VENDEDOR') AND turista_codigo IS NULL)
    )
);

CREATE TABLE reservas (
    codigo INT NOT NULL AUTO_INCREMENT,
    turista_codigo INT NOT NULL,
    sucursal_codigo INT NOT NULL,
    vuelo_numero INT NOT NULL,
    hotel_codigo INT NOT NULL,
    clase_vuelo ENUM('PRIMERA', 'TURISTA') NOT NULL,
    tipo_hospedaje ENUM('MEDIA_PENSION', 'PENSION_COMPLETA') NOT NULL,
    fecha_llegada DATE NOT NULL,
    fecha_partida DATE NOT NULL,
    CONSTRAINT pk_reservas PRIMARY KEY (codigo),
    CONSTRAINT uk_reserva_turista_vuelo UNIQUE (turista_codigo, vuelo_numero),
    CONSTRAINT fk_reserva_turista
        FOREIGN KEY (turista_codigo) REFERENCES turistas (codigo),
    CONSTRAINT fk_reserva_sucursal
        FOREIGN KEY (sucursal_codigo) REFERENCES sucursales (codigo),
    CONSTRAINT fk_reserva_vuelo
        FOREIGN KEY (vuelo_numero) REFERENCES vuelos (numero),
    CONSTRAINT fk_reserva_hotel
        FOREIGN KEY (hotel_codigo) REFERENCES hoteles (codigo),
    CONSTRAINT chk_reserva_fechas CHECK (fecha_llegada < fecha_partida)
);

CREATE INDEX idx_reserva_sucursal ON reservas (sucursal_codigo);
CREATE INDEX idx_reserva_vuelo_clase ON reservas (vuelo_numero, clase_vuelo);
CREATE INDEX idx_reserva_hotel_fechas
    ON reservas (hotel_codigo, fecha_llegada, fecha_partida);
