# Base de datos

Flyway administra la estructura de tablas desde:

```text
tije-back/src/main/resources/db/migration
```

El script `crear_base.sql` solo crea la base vacia. No crea tablas ni elimina
informacion existente.

## Desarrollo

1. Ejecutar `crear_base.sql` en MySQL.
2. Definir `DB_URL`, `DB_USER` y `DB_PASSWORD`.
3. Iniciar el backend con el perfil `dev`.

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
cd tije-back
.\mvnw.cmd spring-boot:run
```

El perfil `dev` aplica el esquema comun y los datos de demostracion ubicados
en `db/dev`. Esos datos no se cargan con el perfil `prod`.

## Primer administrador

Los datos de demostracion no incluyen cuentas de acceso. Para crear el primer
administrador, iniciar una vez el backend con estas variables definidas:

```powershell
$env:APP_ADMIN_ENABLED = "true"
$env:APP_ADMIN_USERNAME = "administrador"
$env:APP_ADMIN_DNI = "12345678" # Reemplazar por el DNI real del administrador
$env:APP_ADMIN_PASSWORD = "una-clave-segura-de-12-o-mas"
```

La clave se guarda como hash BCrypt. El inicializador solo actua cuando no hay
ningun administrador; despues del primer arranque conviene volver a definir
`APP_ADMIN_ENABLED=false` y retirar la clave del entorno.

## Produccion

El perfil `prod` exige las tres variables de conexion y ejecuta solamente las
migraciones comunes. Hibernate usa `ddl-auto=validate`: valida las entidades,
pero no crea ni modifica tablas.

Tambien requiere `FRONTEND_ORIGIN` con el origen exacto del frontend. La cookie
de sesion usa la marca `Secure` en este perfil, de modo que frontend y backend
deben publicarse mediante HTTPS.

Las migraciones se deben ejecutar sobre una base vacia o sobre una base que ya
tenga historial de Flyway. No se habilita `baseline-on-migrate` para evitar
aceptar por error un esquema heredado incompatible.

## Incorporación del DNI

La migración `V6__agregar_dni_turistas.sql` agrega `turistas.dni` como
`VARCHAR(8)`, una restricción UNIQUE y un CHECK de 7 u 8 dígitos. Flyway la
aplica al iniciar el backend; no hay que borrar la base ni volver a ejecutar V1.

La columna permite NULL exclusivamente para conservar registros anteriores sin
inventar documentos (incluidos los datos demo antiguos). La API y el modelo
exigen DNI para todas las altas y modificaciones. No hay valores de relleno.
Los registros pendientes se pueden consultar con:

```sql
SELECT codigo, nombre, apellido FROM turistas WHERE dni IS NULL;
```

Completalos con el DNI real desde Turistas > Editar. La migración no modifica
códigos, titulares, cuentas ni reservas. Una vez completados todos, se podrá
agregar otra migración que establezca NOT NULL; no debe hacerse mientras falten datos.

El 05/10/2026 se verificó V6 con H2 y MySQL, incluidos formatos inválidos,
duplicados y conservación de los registros existentes. Antes de actualizar la
base local se generó un respaldo en
`tije-back/config/mysql-pruebas/respaldo-antes-dni.sql`, ignorado por Git.

## DNI de administradores y vendedores

V7 agrega `usuarios.dni`, con formato de 7 u 8 dígitos y unicidad entre
administradores y vendedores. Los clientes tienen NULL en esa columna: el
sistema consulta el DNI de su turista asociado, sin otra copia. Un CHECK impide
guardar una segunda copia del documento en la fila de un cliente.

Los empleados anteriores quedan pendientes y se completan desde Usuarios > Editar.
El primer administrador de una base nueva requiere `APP_ADMIN_DNI`; esa variable
no reemplaza el documento ni las credenciales de una cuenta ya existente.

V7 se verificó el 05/10/2026 con MySQL y las JSP, incluyendo altas y edición de
empleados, documentos duplicados y consulta del DNI actualizado del cliente.
Antes de aplicarla a la base local se guardó
`tije-back/config/mysql-pruebas/respaldo-antes-dni-empleados.sql` (ignorado por Git).
Se compararon las seis tablas del negocio antes y después: sus datos y
credenciales se conservaron.
