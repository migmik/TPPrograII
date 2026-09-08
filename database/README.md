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

Los datos heredados no incluyen usuarios porque sus claves estaban guardadas
como texto plano. Para crear el primer administrador, iniciar una vez el
backend con estas variables definidas:

```powershell
$env:APP_ADMIN_ENABLED = "true"
$env:APP_ADMIN_USERNAME = "administrador"
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
