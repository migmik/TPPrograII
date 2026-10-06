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
GET /api/v1/reservas/pagina
GET /api/v1/usuarios
GET /api/v1/usuarios/{codigo}
```

El listado de turistas acepta `dni` para buscar una coincidencia exacta y
`titular=true` o `titular=false` para filtrar titulares o familiares. Por ejemplo,
`GET /api/v1/turistas?dni=12345678&titular=true`. Sin filtros se conserva el
listado completo para administradores y vendedores. Los clientes solo consultan
su grupo familiar; cualquier filtro sigue limitado a ese grupo.
El parámetro repetible `codigos` permite solicitar solo turistas específicos,
por ejemplo `GET /api/v1/turistas?codigos=2&codigos=3`; en clientes también se
respeta el grupo familiar.

El listado de usuarios acepta `rol=ADMINISTRADOR`, `rol=VENDEDOR` o `rol=CLIENTE`,
por ejemplo `GET /api/v1/usuarios?rol=VENDEDOR`. Sin `rol`, conserva el listado
completo. Solo un administrador puede consultar este recurso.

La interfaz usa `GET /api/v1/reservas/pagina` con `pagina` (desde 0) y
`tamanio` (1 a 100). Se puede aplicar un criterio por consulta: `codigoTurista`,
`numeroVuelo`, `codigoHotel` o el rango completo `fechaDesde`/`fechaHasta` sobre
la fecha de llegada. La respuesta incluye `elementos`, `pagina`, `totalPaginas`,
`hayAnterior` y `haySiguiente`. Por ejemplo:
`GET /api/v1/reservas/pagina?pagina=0&tamanio=20&numeroVuelo=100`.

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
POST /api/v1/autenticacion/registro
GET  /api/v1/autenticacion/sesion
POST /api/v1/autenticacion/logout
```

El navegador primero pide `/csrf`, conserva la cookie `TIJESESSION` y envia el
token recibido en el encabezado `X-CSRF-TOKEN` al hacer `POST`, `PUT` o
`DELETE`. Despues del login debe pedir un token nuevo. Si frontend y backend
usan origenes diferentes, las peticiones del frontend deben incluir
credenciales para que el navegador envie la cookie.

El registro público requiere CSRF y crea exclusivamente un usuario `CLIENTE`
asociado a un turista titular nuevo. El formulario solicita sus datos personales,
credenciales y una sucursal; no permite elegir roles administrativos. Repetir
nombre de usuario, DNI o email devuelve `409`.

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

## DNI del turista

POST y PUT de `/api/v1/turistas` requieren `dni` como texto: por ejemplo
`"dni": "12345678"`. La regla de la aplicación admite 7 u 8 dígitos, sin puntos
ni espacios. Es único entre titulares y familiares. Omitirlo o enviar un formato
inválido devuelve 400; repetir el de otra persona devuelve 409. Al editar se
permite conservar el propio DNI o corregirlo por otro disponible. Las respuestas
de listado, detalle, alta y modificación incluyen `dni`.

Las cuentas Cliente y las reservas acceden al DNI por su relación con Turista;
no se duplica el documento en esas tablas. El código interno sigue siendo la clave
de las relaciones. Los turistas anteriores pueden devolver `dni: null` hasta
completar su documento real desde la edición.

## DNI de usuarios

POST y PUT de `/api/v1/usuarios` requieren `dni` para administradores y vendedores.
Un documento repetido entre empleados devuelve 409. Para un cliente se omite
`dni` (o se envía null): se utiliza el documento del turista asociado. El alta de
un cliente requiere que ese turista ya tenga DNI. Su documento se corrige en
`/api/v1/turistas`, no en la cuenta de acceso.

Las respuestas de usuarios y de autenticación incluyen el DNI correspondiente;
la consulta de sesión lo lee actualizado, sin guardarlo en el principal de seguridad.
El DNI identifica a la persona; el nombre de usuario sigue siendo el dato de login.
