# API REST

La primera version publica catalogos sin datos personales:

```text
GET /api/v1/sucursales
GET /api/v1/sucursales/{codigo}
GET /api/v1/hoteles
GET /api/v1/hoteles/{codigo}
GET /api/v1/hoteles/{codigo}/disponibilidad?fechaLlegada=AAAA-MM-DD&fechaPartida=AAAA-MM-DD
GET /api/v1/vuelos
GET /api/v1/vuelos/{numero}
GET /api/v1/vuelos/{numero}/disponibilidad?clase=TURISTA
```

La consulta de estos catalogos es publica, pero su escritura requiere un
administrador:

```text
POST   /api/v1/sucursales
PUT    /api/v1/sucursales/{codigo}
DELETE /api/v1/sucursales/{codigo}
POST   /api/v1/hoteles
PUT    /api/v1/hoteles/{codigo}
DELETE /api/v1/hoteles/{codigo}
POST   /api/v1/vuelos
PUT    /api/v1/vuelos/{numero}
DELETE /api/v1/vuelos/{numero}
```

Las consultas protegidas son:

```text
GET /api/v1/turistas
GET /api/v1/turistas/{codigo}
GET /api/v1/reservas
GET /api/v1/reservas/{codigo}
GET /api/v1/usuarios
GET /api/v1/usuarios/{codigo}
```

Las operaciones protegidas de escritura son:

```text
POST   /api/v1/turistas
PUT    /api/v1/turistas/{codigo}
DELETE /api/v1/turistas/{codigo}
POST   /api/v1/reservas
PUT    /api/v1/reservas/{codigo}
DELETE /api/v1/reservas/{codigo}
POST   /api/v1/usuarios
PUT    /api/v1/usuarios/{codigo}
DELETE /api/v1/usuarios/{codigo}
```

Al crear un turista, `codigoTitular` vacio indica que es titular y exige
`codigoSucursal`. Si `codigoTitular` tiene valor, se crea un familiar que
hereda la sucursal del titular y `codigoSucursal` debe omitirse. La
modificacion de usuarios cambia nombre y contrasenia, pero no su rol ni el
turista asociado.

Al crear una reserva no se envia `codigoSucursal`: el backend toma
la sucursal de contratacion del turista. Al modificarla conserva la sucursal
original, incluso si cambia el turista asociado. El DTO de respuesta la informa como
`codigoSucursalContratacion`. La disponibilidad de vuelo se consulta por
`TURISTA` o `PRIMERA`; la de hotel calcula la ocupacion maxima simultanea dentro del
rango solicitado.

Los identificadores deben ser positivos. Los errores se devuelven como JSON
con `fechaHora`, `estado`, `error`, `mensaje`, `ruta` y `detalles`.

La autenticacion usa estas rutas:

```text
GET  /api/v1/autenticacion/csrf
POST /api/v1/autenticacion/login
GET  /api/v1/autenticacion/sesion
POST /api/v1/autenticacion/logout
```

El navegador primero pide `/csrf`, conserva la cookie `TIJESESSION` y envia el
token recibido en el encabezado `X-CSRF-TOKEN` al hacer `POST`, `PUT` o
`DELETE`. Despues del login debe pedir un token nuevo. Si frontend y backend
usan origenes diferentes, las peticiones del frontend deben incluir
credenciales para que el navegador envie la cookie.

Los catalogos `GET` siguen siendo publicos. Sus escrituras y todas las rutas de
usuarios requieren rol `ADMINISTRADOR`. Administradores y vendedores consultan
todos los turistas y reservas. Un cliente solo recibe su turista titular, sus
familiares y las reservas de ese grupo. Las escrituras de turistas y reservas
aceptan `VENDEDOR` o `ADMINISTRADOR`. Una solicitud sin sesion recibe `401`;
una sesion sin el rol o alcance necesario recibe `403`.

Las sesiones viven en memoria del backend y vencen despues de 30 minutos de
inactividad. Esto sirve para una sola instancia del backend aunque la base este
en otro equipo y los usuarios ingresen desde otras maquinas. Reiniciar el
backend cierra las sesiones; si en el futuro se ejecutan varias instancias, se
debera agregar Spring Session con un almacen compartido como Redis o JDBC.

## Nombres del contrato de hoteles

`capacidadTotal` representa la capacidad base del hotel en las solicitudes y
respuestas de catalogo. Es el unico nombre aceptado para ingresar la capacidad.
Los endpoints de disponibilidad usan `plazasDisponibles` para las plazas libres
calculadas en las fechas consultadas. La columna SQL se llama `capacidad_total`;
la migracion V5 renombra la columna anterior conservando los datos existentes.
El permiso para gestionar turistas se llama `ADMINISTRAR_TURISTAS`; los
consumidores del listado de permisos de sesion deben usar este nombre.

