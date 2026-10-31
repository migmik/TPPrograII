# Modelo entidad-relación de Tije Travel

El esquema se crea con las migraciones de Flyway en
`tije-back/src/main/resources/db/migration` (V1 a V9). El diagrama muestra cómo
quedan las tablas después de aplicar todas las migraciones.

```mermaid
erDiagram
    SUCURSALES ||--o{ TURISTAS : "contrata"
    SUCURSALES ||--o{ RESERVAS : "registra"
    TURISTAS |o--o{ TURISTAS : "es titular de"
    TURISTAS ||--o{ RESERVAS : "tiene"
    TURISTAS |o--o| USUARIOS : "tiene cuenta de cliente"
    VUELOS ||--o{ RESERVAS : "incluye"
    HOTELES ||--o{ RESERVAS : "aloja"

    SUCURSALES {
        int codigo PK
        varchar direccion UK
        varchar ciudad
        varchar telefono
    }

    TURISTAS {
        int codigo PK
        varchar dni UK "7 u 8 digitos"
        varchar nombre
        varchar apellido
        varchar direccion
        varchar email UK
        varchar telefono_fijo
        varchar telefono_celular
        int sucursal_codigo FK
        int titular_codigo FK "null si es titular"
    }

    USUARIOS {
        int codigo PK
        varchar tipo_usuario "ADMINISTRADOR, VENDEDOR o CLIENTE"
        varchar nombre_usuario UK
        varchar contrasenia "hash bcrypt"
        enum rol
        varchar dni UK "solo empleados"
        int turista_codigo FK, UK "solo clientes"
    }

    HOTELES {
        int codigo PK
        varchar nombre
        varchar direccion
        varchar ciudad
        varchar telefono
        int capacidad_total
        int precio_noche_persona
    }

    VUELOS {
        int numero PK
        datetime fecha_hora
        varchar origen
        varchar destino
        int total_plazas
        int plazas_turista
        int plazas_primera
        int precio
    }

    RESERVAS {
        int codigo PK
        int turista_codigo FK
        int sucursal_codigo FK
        int vuelo_numero FK
        int hotel_codigo FK
        enum clase_vuelo "TURISTA o PRIMERA"
        enum tipo_hospedaje "MEDIA_PENSION o PENSION_COMPLETA"
        date fecha_llegada
        date fecha_partida
        int precio_vuelo
        int precio_hotel
        int total
        boolean pagada
        datetime fecha_pago
    }
```

La tabla `control_escrituras` tiene una sola fila y no se relaciona con las
demás: los servicios la bloquean con `SELECT ... FOR UPDATE` para que dos
escrituras no se pisen (`BloqueoEscrituras`).

## Relaciones

| Relación | Cardinalidad | Cómo se implementa |
|---|---|---|
| Sucursal contrata turistas | 1 a N | `turistas.sucursal_codigo` → `sucursales.codigo` |
| Titular y familiares | 1 a N (autorrelación) | `turistas.titular_codigo` → `turistas.codigo`; es null en el titular |
| Turista tiene reservas | 1 a N | `reservas.turista_codigo` → `turistas.codigo` |
| Sucursal registra reservas | 1 a N | `reservas.sucursal_codigo`: guarda la sucursal del momento de la reserva aunque el turista se mude |
| Vuelo y hotel de la reserva | 1 a N cada uno | `reservas.vuelo_numero` y `reservas.hotel_codigo` |
| Cliente y turista titular | 1 a 1 (opcional) | `usuarios.turista_codigo`, único y solo para clientes |

## Herencia de usuarios

En Java, `Usuario` es una clase abstracta con tres subclases: `Administrador`,
`Vendedor` y `Cliente`. En la base se guardan todas en la tabla `usuarios`
(estrategia `SINGLE_TABLE`) y la columna `tipo_usuario` indica qué clase es.
Los checks de la tabla aseguran que:

- un cliente siempre tiene `turista_codigo` y nunca un DNI propio (lo toma del turista);
- un administrador o vendedor nunca tiene `turista_codigo`.

## Restricciones principales

- Un turista no puede tener dos reservas en el mismo vuelo (`uk_reserva_turista_vuelo`).
- Un hotel no puede repetirse con el mismo nombre en la misma ciudad (`uk_hotel_nombre_ciudad`).
- La fecha de llegada tiene que ser anterior a la de partida (`chk_reserva_fechas`).
- Las plazas por clase de un vuelo no pueden superar el total (`chk_vuelo_distribucion_plazas`).
- Precios de vuelos entre 150.000 y 300.000 (múltiplos de 10.000) y de hoteles
  entre 25.000 y 55.000 por noche (múltiplos de 1.000).
- `total` de la reserva siempre es `precio_vuelo + precio_hotel` (`chk_reserva_importes`).
