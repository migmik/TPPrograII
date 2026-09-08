# Tije Travel - Backend Web

Proyecto de Programacion II para gestionar una cadena de agencias de viajes.

La version original de consola se conserva en la rama `main` y en el tag
`v1.0-tp-entregado`. Esta rama contiene el nucleo reorganizado para una
aplicacion web con Spring Boot, JPA y MySQL.

Los archivos `docs/uml.svg` y `docs/uml.pdf` son exportaciones historicas de
la primera entrega. Los diagramas vigentes son `docs/uml-tijetravel.md` y
`docs/uml-tijetravel.puml`.

## Estado actual

- Modelos convertidos en entidades JPA.
- Repositorios Spring Data definidos para todas las entidades.
- Reglas de negocio separadas en controladores por recurso.
- Validaciones de dominio y excepciones explicitas.
- Transacciones declaradas en las operaciones de escritura.
- Pruebas unitarias, integracion JPA y arranque de contexto con H2.
- Menus de consola y persistencia en archivos retirados del codigo productivo.
- Esquema MySQL versionado con Flyway y validado por Hibernate.
- Datos heredados no sensibles disponibles como seed del perfil `dev`.
- API REST de consulta y disponibilidad para sucursales, hoteles y vuelos.
- Respuestas HTTP desacopladas de las entidades mediante DTOs y mapeadores.
- Autenticacion web con Spring Security, contrasenias BCrypt y sesiones HTTP.
- Autorizacion por rol, proteccion CSRF y CORS limitado al frontend configurado.
- Consultas REST protegidas de turistas, reservas y usuarios.
- Operaciones REST de escritura para turistas, reservas y usuarios.
- Operaciones REST administrativas para sucursales, hoteles y vuelos.
- Frontend web aun pendiente.

Los archivos de `tije-back/datos` se mantienen como referencia historica. Su
contenido de dominio fue convertido en una migracion de desarrollo, excepto
los usuarios porque sus contrasenias originales estan en texto plano.

## Arquitectura

```text
Frontend web                         (pendiente)
        |
        v
Spring Security                     sesion, CSRF, CORS y roles
        |
        v
Controllers REST + DTOs             api/ (catalogos y datos protegidos)
        |
        v
Controladores de negocio            controladores/
        |
        v
Repositorios Spring Data JPA        repositorios/
        |
        v
MySQL + migraciones Flyway          configurado
```

Los controladores de negocio usan `@Service`. Se conserva el nombre
`controladores` para mantener el estilo del proyecto Gestion Militar. Los
controllers HTTP viven en `api/controladores` y delegan las operaciones sin
duplicar reglas de negocio.

## Estructura

```text
TPPrograII/
  database/                         creacion de la base y guia de uso
  docs/                             UML y documentacion
  tije-front/                       frontend futuro
  tije-back/
    config/                         configuracion local externa e ignorada
    datos/                          datos heredados, solo referencia
    pom.xml
    src/
      main/
        java/com/tijetravel/tijeback/
          api/
            controladores/          entrada HTTP y rutas versionadas
            dto/                    contratos JSON de respuesta
            errores/                traduccion de excepciones a HTTP
            mapeadores/              conversion de modelos a DTOs
          controladores/            casos de uso y reglas de negocio
          enums/                    roles, permisos y tipos del dominio
          excepciones/              errores esperables del negocio
          modelos/                  entidades y validaciones
          repositorios/             contratos de acceso a datos
          seguridad/                sesion HTTP, usuarios y reglas de acceso
          TijeBackApplication.java
        resources/
          application.yml
          application-dev.yml
          application-prod.yml
          application.example.properties
          db/migration/             esquema comun versionado
          db/dev/                   datos exclusivos de desarrollo
      test/
```

## Criterios de diseno

- Los paquetes y variables siguen la convencion Java: minusculas para paquetes,
  `PascalCase` para clases y `lowerCamelCase` para miembros.
- Se eliminaron abreviaturas como `u`, `c`, `d` y `t` de las firmas
  publicas.
- Los modelos validan su estado al construirse o modificarse.
- Los identificadores de sucursales, hoteles, turistas, reservas y usuarios son
  generados por la base de datos. El numero de vuelo sigue siendo un dato del
  dominio.
- Las relaciones usan objetos JPA en vez de codigos sueltos.
- `Usuario` mantiene herencia y polimorfismo mediante `Administrador`,
  `Vendedor` y `Cliente`.
- Los controladores reciben repositorios por constructor y no conocen detalles
  de consola, archivos ni SQL.
- La API nunca devuelve directamente entidades JPA; los mapeadores construyen
  DTOs que definen el contrato entregado al frontend.
- Las contrasenias se almacenan con BCrypt y nunca se devuelven en los DTOs.
- Spring Security autentica contra `UsuarioRepositorio` y guarda solamente el
  usuario autenticado y sus permisos en la sesion HTTP.
- La autorizacion HTTP complementa las reglas de negocio existentes: un filtro
  rechaza primero las rutas que el rol no puede utilizar.
- Los errores esperables se expresan con excepciones como
  `EntidadNoEncontradaException`, `EntidadDuplicadaException`,
  `CapacidadExcedidaException` y `OperacionNoPermitidaException`.

## Reglas preservadas

- Un vendedor administra turistas y reservas; un administrador administra todo.
- Un cliente solo consulta y debe estar asociado a un turista titular.
- No se puede eliminar el ultimo administrador.
- No se eliminan entidades que siguen referenciadas por reservas, usuarios o
  familiares.
- Un turista no puede reservar dos veces el mismo vuelo.
- La sucursal de una reserva se deriva de la sucursal de contratacion del
  turista y no puede elegirse desde la solicitud.
- La llegada debe coincidir con la fecha del vuelo y la ciudad del hotel con su
  destino.
- Se controla la capacidad por clase del vuelo y la ocupacion superpuesta del
  hotel.
- Una reduccion de capacidad no puede dejar reservas existentes fuera de cupo.

## Configuracion

La configuracion comun usa Flyway y `ddl-auto=validate`. Los perfiles `dev`,
`prod` y `test` separan los datos de demostracion, las credenciales obligatorias
de produccion y la base H2 de pruebas. `application.yml` usa variables de
entorno y el ejemplo sin secretos se encuentra en
`tije-back/src/main/resources/application.example.properties`.

Para desarrollo se puede crear el archivo ignorado
`tije-back/config/application.properties`. Spring Boot lo carga desde fuera del
classpath al ejecutar Maven o el JAR desde `tije-back`. Maven excluye por
configuracion cualquier `application.properties` situado accidentalmente en
`src/main/resources`, por lo que una clave local no se empaqueta en el JAR.

La conexion admite estas variables de entorno:

```text
DB_URL
DB_USER
DB_PASSWORD
FRONTEND_ORIGIN
APP_ADMIN_ENABLED
APP_ADMIN_USERNAME
APP_ADMIN_PASSWORD
```

La base vacia se puede crear con `database/crear_base.sql`. Flyway crea las
tablas desde `db/migration`; el perfil `dev` agrega el seed de `db/dev`, incluida
una reserva futura en la migracion `V3`, y el perfil `prod` ejecuta unicamente
las migraciones comunes.

`FRONTEND_ORIGIN` debe contener el origen exacto desde el que el navegador
consume la API, por ejemplo `http://192.168.1.20:5173`. En produccion es
obligatorio y la cookie de sesion se marca como `Secure`, por lo que se debe
publicar el sistema mediante HTTPS.

Si la base no tiene administradores, se puede habilitar una sola vez la
creacion segura del primero con `APP_ADMIN_ENABLED=true`, un nombre en
`APP_ADMIN_USERNAME` y una clave de al menos 12 caracteres en
`APP_ADMIN_PASSWORD`. Si ya existe un administrador, el inicializador no crea
otro usuario.

## API REST

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

Al crear o modificar una reserva no se envia `codigoSucursal`: el backend toma
la sucursal de contratacion del turista. El DTO de respuesta la informa como
`codigoSucursalContratacion`. La disponibilidad de vuelo se consulta por
`TURISTA` o `PRIMERA`; la de hotel cuenta las reservas que se superponen con el
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

## Verificacion

Desde `tije-back`:

```powershell
.\mvnw.cmd test
```

Las pruebas usan el perfil `test` con H2 en memoria, no dependen de la
configuracion local y no requieren una instancia de MySQL. La suite ejecuta
Flyway desde una base vacia y verifica esquema, seed, relaciones JPA y reglas
heredadas del sistema de consola. Las pruebas MockMvc recorren tambien la capa
HTTP y verifican DTOs, validaciones, estados y respuestas de error.

## Proximas etapas

1. Implementar el frontend y consumir exclusivamente la API del backend.
2. Preparar despliegue HTTPS y, solo si hay multiples instancias del backend,
   externalizar las sesiones con Spring Session.
