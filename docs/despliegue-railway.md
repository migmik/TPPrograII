# Despliegue en Railway

El proyecto usa tres servicios en un mismo proyecto de Railway: MySQL, `tije-back`
y `tije-front`. El frontend y el backend se construyen con los `Dockerfile` de sus
carpetas. La base queda accesible solo dentro del proyecto.

## 1. Conectar el repositorio

En cada servicio Java, seleccioná el repositorio de GitHub y revisá en **Settings →
Source** que el directorio raíz sea `/tije-back` o `/tije-front`, según corresponda.
Los archivos nuevos tienen que estar subidos a GitHub para que Railway los use.
Railway detecta el `Dockerfile` de cada directorio. No hace falta configurar un
comando de compilación ni uno de inicio.

## 2. Configurar variables

En **Variables** de `tije-back`, agregá:

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `FRONTEND_ORIGIN` | `https://<dominio-del-front>` |

`MySQL` en las referencias debe coincidir con el nombre del servicio de base de
datos en tu proyecto. Elegí las variables desde el selector de Railway para evitar
errores al escribirlas. `FRONTEND_ORIGIN` es la URL pública exacta del frontend,
sin `/` final.

Para crear el primer administrador en una base vacía, agregá temporalmente al
backend `APP_ADMIN_ENABLED=true`, `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD` y
`APP_ADMIN_DNI`. La contraseña debe tener al menos 12 caracteres y el DNI, 7 u 8
dígitos. Tras comprobar el ingreso, cambiá `APP_ADMIN_ENABLED` a `false` y quitá
`APP_ADMIN_PASSWORD` de Railway. No guardes la contraseña en Git.

En **Variables** de `tije-front`, agregá:

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `BACKEND_URL` | `https://<dominio-del-back>` |

`BACKEND_URL` es la URL base pública del backend, sin `/api/v1` ni `/` final. Se
usa HTTPS porque el backend marca como segura la cookie de inicio de sesión en
producción. Si se empleara el enlace HTTP interno entre estos dos servicios,
habría que adaptar el manejo de esa cookie. No habilites acceso público a MySQL.

Railway proporciona `PORT` al iniciar cada servicio. La configuración de Spring
lo usa automáticamente; en la computadora local siguen vigentes 8080 para el
backend y 8081 para el frontend. `FRONT_PORT` conserva el cambio de puerto local.

## 3. Publicar y comprobar

En **Settings → Networking → Public Networking**, generá un dominio para cada
servicio Java. Copiá esas URL en `FRONTEND_ORIGIN` y `BACKEND_URL` y aplicá los
cambios. Si Railway pide elegir puerto, usá el mismo que muestra `PORT` en las
variables del servicio. La base de datos no necesita dominio público.

Una vez desplegados, comprobá en este orden:

1. Los logs de `tije-back` muestran que Flyway creó o verificó las tablas y que
   Tomcat arrancó. No hace falta ejecutar `database/crear_base.sql` en Railway.
2. `https://<dominio-del-back>/api/v1/hoteles` responde con JSON.
3. `https://<dominio-del-front>/` abre la página y el catálogo de hoteles carga.
4. Iniciá sesión con el administrador inicial y probá una operación desde el
   frontend. Después desactivá el alta inicial como se indicó arriba.

Si un servicio no arranca, mirá sus **Deploy Logs**. Un error de conexión a
MySQL suele indicar una referencia o variable incorrecta; un error de puerto o
dominio indica que el puerto de la aplicación y el del dominio no coinciden.
El despliegue usa el perfil `prod`: no inserta los datos de demostración del
perfil `dev`.
