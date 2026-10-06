# Tije Travel — frontend

Primera versión de la interfaz web, independiente de `tije-back`. Está hecha con
Java 21, Spring Boot, Spring MVC, JSP y JSTL. Se genera un WAR ejecutable.
No usa JavaScript ni se conecta directamente a MySQL.

## Qué se puede ver

- Inicio con acceso a hoteles y vuelos.
- Listado de hoteles obtenido de la API.
- Detalle de cada hotel.
- Formulario para consultar plazas disponibles entre dos fechas.
- Listado y detalle de vuelos, con fechas en formato día/mes/año y hora.
- Capacidad y plazas libres por clase: turista y primera.
- Alta, edición y eliminación de hoteles y vuelos para administradores.
- Listado, alta, edición y eliminación de sucursales para administradores.
- Inicio y cierre de sesión con los usuarios existentes del backend.
- Registro público de cuentas Cliente asociadas a un nuevo turista titular.
- Pantalla «Mi cuenta» con el nombre de usuario y el rol informado por la API.
- Listado, creación, edición de credenciales y eliminación de usuarios para administradores.
- Listado, detalle, creación, edición y eliminación de turistas para administradores y vendedores.
- Consulta del propio grupo familiar para clientes.
- Listado, detalle, alta, edición y eliminación de reservas para administradores y vendedores.
- Consulta de las reservas del grupo familiar para clientes.
- Mensajes para fechas inválidas, recursos inexistentes y problemas de conexión.

Los catálogos son públicos. La administración de sucursales, hoteles, vuelos y
usuarios requiere ingresar como administrador.
El CSS actual solo facilita la lectura.

## Cómo ejecutarlo

Necesitás Java 21. El Maven Wrapper incluido permite compilar sin instalar Maven
manualmente; la primera ejecución descarga las herramientas y dependencias.

Primero iniciá MySQL y el backend siguiendo la [guía del backend](../README.md).
Desde la raíz, en una terminal:

```powershell
cd tije-back
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

La conexión a MySQL debe estar configurada como explica
[database/README.md](../database/README.md). Si querés ver hoteles de demostración,
usá el perfil `dev` con una base destinada a desarrollo.

En otra terminal, desde la raíz:

```powershell
cd tije-front
.\mvnw.cmd spring-boot:run
```

Abrí **http://localhost:8081**. El frontend consulta por defecto al backend en
`http://localhost:8080`. Para detener cada aplicación, presioná Ctrl+C en su terminal.
Este comando sirve para trabajar en el código; el empaquetado sigue siendo WAR.

Si el backend usa otra dirección, definila antes de iniciar el frontend:

```powershell
$env:BACKEND_URL = "http://localhost:18081"
.\mvnw.cmd spring-boot:run
```

`BACKEND_URL` es la dirección base del servidor, sin `/api/v1` al final.
`FRONT_PORT` permite cambiar el puerto del frontend. También se pueden pasar
`--app.backend.url=...` y `--server.port=...` al comando `java -jar`.

Si ya tenés un Tomcat externo usando el puerto 8080, podés iniciar el proyecto
con el backend en otro puerto. Desde la raíz, con las instancias anteriores de
Tije detenidas:

```powershell
$env:BACK_PORT = "8083"
$env:BACKEND_URL = "http://localhost:8083"
.\arrancar-tije-travel.bat
```

El frontend sigue en `http://localhost:8081`. Las variables se definen en esa
terminal; no hace falta cambiar el código ni detener el Tomcat externo.

La página de inicio funciona aunque el backend esté apagado. Para listar hoteles,
vuelos y consultar disponibilidad tiene que estar funcionando la API.

## Cómo funciona el código

```text
Navegador → controlador del frontend → cliente HTTP Java → API del backend → MySQL
Navegador ← HTML generado por JSP    ← datos recibidos en JSON
```

Por ejemplo, al entrar a `/hoteles`:

1. `HotelesControlador.listar()` recibe la solicitud del navegador.
2. Llama a `HotelesApiCliente.listar()`, que hace un GET a `/api/v1/hoteles`.
3. `RestClient` convierte el JSON en objetos `HotelRespuesta`.
4. El controlador agrega la lista al `Model` con el nombre `hoteles`.
5. Devuelve `"hoteles/lista"`. Spring busca `WEB-INF/vistas/hoteles/lista.jsp`.
6. La JSP recorre la lista con `c:forEach` y genera las filas de la tabla HTML.

El navegador recibe HTML; no conoce la dirección interna de la API ni ejecuta
llamadas JavaScript. La comunicación con el backend la hace el servidor Java del
frontend. Estas consultas no requieren cambiar CORS en el backend.

Al consultar disponibilidad, el formulario hace un GET al frontend. Spring coloca
las fechas en `ConsultaDisponibilidad`; `@NotNull` comprueba que se ingresaron y
`@DateTimeFormat` indica el formato esperado. `BindingResult` contiene los errores
que se muestran junto a los campos. Si las fechas son válidas y están ordenadas,
Java consulta la API y la misma JSP muestra el resultado. La disponibilidad la
calcula el backend: el frontend no cuenta reservas ni modifica la base.

Los vuelos siguen el mismo recorrido. `/vuelos` muestra el listado y
`/vuelos/{numero}` muestra el detalle. `VuelosControlador.detalle()` hace tres
consultas mediante `VuelosApiCliente`: los datos del vuelo, la disponibilidad
de clase `TURISTA` y la de `PRIMERA`. Usa el mismo `RestClient` configurado para
hoteles, sin una segunda configuración de conexión.

Las capacidades y las plazas libres son datos distintos: un vuelo puede tener
80 plazas de clase turista y solo 3 libres. El frontend muestra la disponibilidad
que devuelve la API; no resta reservas ni toma la capacidad como disponibilidad.
Cada consulta informa el estado en ese momento, y no garantiza un cupo para una
reserva posterior.

`VueloRespuesta.getFechaYHoraFormateada()` solo prepara la fecha para mostrarla
como `12/10/2026 19:45`; no cambia la fecha recibida ni aplica reglas de negocio.
Las clases del vuelo se envían con los valores del contrato de la API: `TURISTA`
y `PRIMERA`. El frontend no importa clases Java del backend.

## Archivos principales

| Archivo o carpeta | Para qué sirve |
|---|---|
| `pom.xml` | Dependencias, Java 21 y empaquetado WAR. |
| `TijeFrontApplication.java` | Inicia la aplicación. También permite desplegar el WAR en un contenedor compatible. |
| `configuracion/ConfiguracionApi.java` | Crea el cliente HTTP con la dirección del backend y límites de espera de cinco segundos. |
| `clientes/HotelesApiCliente.java` y `clientes/VuelosApiCliente.java` | Consultan los catálogos y la disponibilidad. |
| `clientes/HotelesGestionApiCliente.java` y `clientes/VuelosGestionApiCliente.java` | Envían los cambios con la sesión del administrador. |
| `clientes/SucursalesApiCliente.java` | Consulta y administra sucursales; también aporta el selector para turistas. |
| `controladores/InicioControlador.java` | Muestra la página de inicio. |
| `controladores/HotelesControlador.java` | Recibe las solicitudes y prepara los datos de las páginas. |
| `controladores/VuelosControlador.java` | Prepara el listado de vuelos y el detalle con ambas clases. |
| `controladores/HotelesGestionControlador.java` y `controladores/VuelosGestionControlador.java` | Reciben los formularios de administración. |
| `controladores/SucursalesControlador.java` | Muestra el listado y recibe los cambios de sucursales. |
| `controladores/ManejadorErrores.java` | Convierte errores de conexión o consulta en una página comprensible. |
| `dto/` | Datos recibidos de la API. No son entidades JPA ni acceden a la base. |
| `dto/VueloRespuesta.java` y `dto/DisponibilidadVueloRespuesta.java` | Datos del vuelo y de las plazas libres recibidas. |
| `formularios/ConsultaDisponibilidad.java` | Datos y validaciones del formulario. |
| `resources/application.properties` | Puerto, URL del backend y ubicación de las JSP. |
| `resources/messages.properties` | Mensajes en español para fechas mal escritas. |
| `webapp/WEB-INF/vistas/` | Páginas JSP y navegación compartida. |
| `webapp/WEB-INF/vistas/vuelos/` | Listado, detalle y formularios de vuelos. |
| `webapp/WEB-INF/vistas/sucursales/` | Listado, formulario y confirmación de baja. |
| `resources/static/css/base.css` | Estilos mínimos de lectura. |
| `src/test/java/` | Pruebas del cliente HTTP y los controladores. |

Las rutas Java están dentro de `src/main/java/com/tijetravel/tijefront/`.

Usamos clases con getters y setters para que el enlace entre JSON, formularios y
expresiones JSP sea fácil de seguir. Por ejemplo, `${hotel.nombre}` lee
`hotel.getNombre()`. `c:out` muestra los textos escapados como HTML y `form:errors`
muestra los errores del formulario. No hay bloques de lógica Java dentro de las JSP.
Las reglas del negocio permanecen en los servicios del backend.

## Inicio y cierre de sesión

Las rutas nuevas son `GET /login` para mostrar el formulario, `POST /login` para
ingresar, `GET /cuenta` para consultar la cuenta y `POST /logout` para salir.
Se usa un usuario existente en la base del backend; el frontend no crea usuarios
ni tiene una tabla de contraseñas.

El ingreso funciona así:

1. `login.jsp` muestra los campos de usuario y contraseña. La etiqueta `form:form`
   agrega el token CSRF al formulario mediante la integración de Spring Security.
2. `SesionControlador` recibe `IniciarSesionFormulario` y comprueba los campos
   obligatorios y sus longitudes.
3. `AutenticacionApiCliente` pide un token a `/api/v1/autenticacion/csrf` y luego
   envía las credenciales a `/api/v1/autenticacion/login`.
4. El backend verifica la contraseña y devuelve los datos de la sesión. Su cookie
   queda guardada en el cliente HTTP Java, dentro del servidor del frontend.
5. El controlador renueva el identificador de la sesión del navegador y su token
   CSRF, guarda solamente los datos del usuario y redirige a `/cuenta`.
6. Antes de mostrar la cuenta, consulta `/api/v1/autenticacion/sesion` para confirmar
   que la sesión del backend sigue vigente. Si venció, vuelve al ingreso.

Hay dos cookies con funciones distintas: el navegador usa `TIJEFRONTSESSION`
para identificarse ante el frontend; el cliente HTTP Java conserva `TIJESESSION`
para identificarse ante el backend. No se envía la cookie del backend al navegador.
`@SessionScope` crea una `ConexionApiSesion` por sesión del navegador, cada
una con su propio `CookieManager`. Por eso las cookies de dos usuarios no se mezclan.
El cliente de autenticación y los clientes de usuarios y turistas usan esa conexión
para conservar la sesión del login en las operaciones privadas.
Los clientes de hoteles y vuelos conservan su conexión pública compartida.

`DatosNavegacion` agrega el usuario guardado al modelo de las páginas para mostrar
«Mi cuenta» y «Cerrar sesión», e indica al navegador que no guarde esas páginas
en caché. Ese dato del usuario sirve para la presentación; no reemplaza
las comprobaciones de sesión y permisos que hace la API. Los administradores ven
además el enlace «Usuarios». Escribir esa dirección manualmente no evita la
comprobación de acceso en el controlador ni los permisos de la API.

El botón de salida envía un POST con CSRF. El cliente pide un token actualizado al
backend, llama a su logout y el controlador invalida la sesión local. Si la API
no responde, igualmente se cierra la sesión local y se informa que no se pudo
confirmar el cierre remoto. La sesión remota quedará sujeta a su vencimiento.

`ConfiguracionSeguridad` usa Spring Security para proteger los formularios y
agregar las cabeceras de seguridad. El ingreso y la salida los atiende nuestro
controlador, que delega en la API. Por eso se desactivan los formularios automáticos
de Spring y la creación del usuario local por defecto. Las rutas pasan por esa
protección; `/cuenta` verifica la sesión en el controlador antes de entregar datos.
CSRF protege el envío del formulario: no es un segundo ingreso ni una contraseña.

| Archivo nuevo | Responsabilidad |
|---|---|
| `configuracion/ConfiguracionSeguridad.java` | Protección de formularios y almacenamiento del token CSRF local. |
| `clientes/ConexionApiSesion.java` | Conexión, cookies por sesión y obtención del token CSRF de la API. |
| `clientes/AutenticacionApiCliente.java` | Llamadas de ingreso, consulta de sesión y salida. |
| `controladores/SesionControlador.java` | Ingreso, consulta de cuenta y salida. |
| `controladores/DatosNavegacion.java` | Datos del usuario para la navegación compartida. |
| `formularios/IniciarSesionFormulario.java` | Campos y validaciones del ingreso. |
| `dto/SesionRespuesta.java` y `dto/CsrfRespuesta.java` | Respuestas recibidas de la API. |
| `webapp/WEB-INF/vistas/sesion/` | Formulario de ingreso y página de cuenta. |

Referencias: [CSRF y formularios con Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
y [cliente HTTP de Spring para Java](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/client/JdkClientHttpRequestFactory.html).

## Crear usuarios desde la página

1. Ingresá con una cuenta administradora. Si todavía no existe, seguí el apartado
   «Primer administrador» de [la guía de base de datos](../database/README.md).
2. Abrí **Usuarios** en el menú y elegí **Crear usuario**.
3. Escribí el nombre, la contraseña y el rol.
4. Para un cliente, elegí un turista titular existente que todavía no tenga cuenta.
   Para un administrador o vendedor dejá el turista sin seleccionar.
5. Al confirmar, se crea la cuenta en el backend y se vuelve al listado.

La pantalla no crea el primer administrador anónimamente: necesita una sesión
administradora previa. Tampoco crea turistas; si no hay turistas disponibles,
se informa en el formulario y todavía se pueden crear vendedores o administradores.

`UsuariosControlador` atiende `GET /usuarios`, `GET /usuarios/nuevo` y
`POST /usuarios`. Primero confirma la sesión y el rol con el backend. Para preparar
el selector, reúne en un `HashSet` los códigos de turistas que ya tienen cuenta y
recorre el listado de turistas con un `for`, agregando solamente los titulares restantes.
La API vuelve a validar la asociación al guardar, incluso si otro administrador
creó una cuenta entre la carga y el envío del formulario.

`CrearUsuarioFormulario` valida los campos. El controlador también comprueba que
el turista corresponda al rol y que la contraseña no supere los 72 bytes que admite
el backend. Si hay un error, conserva nombre, rol y selección disponible, pero la
contraseña se vuelve a escribir. Un nombre o turista duplicado se informa en la
misma página. Si se pierde la conexión durante el alta, no se reintenta el POST
automáticamente: se pide consultar el listado antes de volver a enviarlo.

`UsuariosApiCliente` envía las consultas y el alta usando `ConexionApiSesion`.
Esta clase se extrajo del cliente de autenticación para compartir la conexión
existente con las operaciones privadas. No se abre una segunda sesión de login.
`TuristasApiCliente` obtiene los datos del selector; `TuristaResumen` conserva solo
código, nombre, apellido y condición de titular de la respuesta. Las vistas están en
`webapp/WEB-INF/vistas/usuarios/lista.jsp` y `nuevo.jsp`.

El backend sigue comprobando permisos, nombres duplicados y relaciones entre
entidades, y guarda las contraseñas como hash. El frontend no tiene repositorios,
conexión a MySQL ni contraseñas fijas para las cuentas.

## Pruebas y WAR

```powershell
.\mvnw.cmd test
```

Estas pruebas usan respuestas simuladas de la API y no necesitan MySQL. Comprueban
el contrato JSON, las fechas enviadas, los errores y los datos que reciben las
vistas. La ejecución real del WAR se comprueba aparte, porque MockMvc no compila
ni renderiza las JSP.

Para generar el WAR, desde `tije-front`:

```powershell
.\mvnw.cmd package
```

`package` compila, ejecuta las pruebas y genera
`target/tije-front-0.0.1-SNAPSHOT.war`. Se ejecuta así:

```powershell
java -jar target/tije-front-0.0.1-SNAPSHOT.war
```

**Detalle comprobado en esta PC:** al ejecutar el WAR desde la ruta del repositorio,
Tomcat no pudo cargar las bibliotecas de etiquetas JSP porque la ruta contiene
espacios. El arranque con Maven desde esa misma carpeta sí funcionó. Para ejecutar
el WAR, se comprobó que funciona copiándolo a una carpeta temporal sin espacios:

```powershell
$carpetaFront = Join-Path $env:TEMP "tije-front"
New-Item -ItemType Directory -Force -Path $carpetaFront | Out-Null
Copy-Item target/tije-front-0.0.1-SNAPSHOT.war (Join-Path $carpetaFront "tije-front.war")
java -jar (Join-Path $carpetaFront "tije-front.war")
```

Verificá que el valor de `$carpetaFront` tampoco contenga espacios. Esa copia solo
sirve para ejecutar: el código se sigue editando en el repositorio. Al recompilar,
detené la aplicación antes de reemplazar la copia del WAR.

La suite contiene pruebas automatizadas para hoteles, vuelos, sesiones, usuarios, turistas y reservas. Comprueba,
entre otras cosas, que se lean correctamente las fechas del JSON y que se muestren
las plazas libres recibidas aunque sean menores a la capacidad del vuelo.
También verifica el aislamiento de cookies, los tokens CSRF, el cambio de
identificador al ingresar, las credenciales incorrectas, las sesiones vencidas y
la salida cuando la API no responde.
Las pruebas de usuarios cubren los tres roles, CSRF, asociación del cliente con
un turista, rechazo de permisos, nombres duplicados, sesiones vencidas y reutilización
de las cookies del login. El código nuevo utiliza bucles y pasos explícitos.

El 28/09/2026 se probó además el WAR contra la base MySQL de pruebas: altas desde
las JSP para los tres roles, ingreso con las cuentas creadas, rechazo de accesos
sin permisos, nombres duplicados, selección de titulares sin cuenta, edición de
credenciales y eliminación con confirmación. También se verificó el nuevo ingreso
después de editar la propia cuenta y que borrar una cuenta conserva al turista. Las cuentas
temporales se eliminaron al finalizar; no se crearon usuarios de prueba en la base
principal.

La etapa de sesiones también se verificó ejecutando el WAR contra el backend y
una base MySQL de pruebas existente: formulario JSP con CSRF, ingreso correcto e
incorrecto, dos sesiones independientes, salida normal, salida local con el backend
apagado y vencimiento de sesión después de reiniciar ese backend de prueba.
No se guardaron credenciales de prueba en el código ni en esta documentación.

El 15/09/2026 también se ejecutó el WAR y se verificaron las páginas por HTTP
contra el backend conectado a MySQL: listado, detalle y disponibilidad de los
4 hoteles y los 4 vuelos presentes en esa base. Se compararon los datos mostrados
por las JSP con las respuestas de la API. Fueron consultas de lectura, sin altas,
modificaciones ni eliminaciones de datos.

Con una API simulada temporal se comprobaron además catálogo vacío, vuelos sin
plazas, errores de conexión y textos con caracteres HTML. Esa simulación no forma
parte del frontend. Las verificaciones HTTP comprueban el HTML generado por las
JSP; el diseño visual queda para una etapa posterior.

El WAR se puede ejecutar con `java -jar` porque incluye el lanzador de Spring Boot.
Las dependencias del contenedor marcadas como `provided` se incluyen en
`WEB-INF/lib-provided` para la ejecución independiente. Para un Tomcat externo,
esta versión de Spring Boot requiere un contenedor compatible; el proyecto utiliza
Tomcat 11. Las JSP se guardan bajo `WEB-INF` para acceder a ellas mediante los
controladores.

Referencias: [JSP y JSTL con Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc-view/mvc-jsp.html)
y [empaquetado de JSP en Spring Boot](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.embedded-container.jsp-limitations).

## Editar y eliminar usuarios

Desde el listado, un administrador puede elegir **Editar credenciales**. El
formulario pide nombre y una nueva contraseña: ambos son obligatorios porque
así funciona la API. No cambia el rol ni el turista asociado. Al editar la propia
cuenta se cierra la sesión actual y se pide ingresar nuevamente. Esto no revoca
otras sesiones que esa cuenta pudiera tener abiertas en otros navegadores.

**Eliminar** abre una confirmación con los datos de la cuenta. Solo el botón
**Confirmar eliminación** envía la operación. No se permite borrar la propia
cuenta; el backend también protege al último administrador. Borrar la cuenta no
borra al turista asociado ni sus reservas.

El recorrido es: JSP envía un formulario POST a `UsuariosControlador`, que
comprueba la sesión y valida los datos. `UsuariosApiCliente` llama al backend
con PUT para modificar o DELETE para eliminar, usando su cookie y token CSRF.
Al terminar se vuelve al listado para evitar repetir la operación al recargar.
Las nuevas vistas son `usuarios/editar.jsp` y `usuarios/eliminar.jsp`; los campos
de edición están en `ModificarUsuarioFormulario`. No se agregó JavaScript ni CSS.

Para compilar, probar y generar el WAR se ejecutó desde la raíz:

```powershell
mvn.cmd -f tije-front/pom.xml package
```

## Gestionar turistas

Ingresá y abrí **Turistas**. Administradores y vendedores pueden crear, editar
y eliminar; los clientes solamente consultan su grupo familiar, tal como lo
filtra el backend.

- Para crear un titular, completá los datos y elegí una sucursal. Dejá el titular
  sin seleccionar.
- Para crear un familiar, elegí un titular existente y dejá la sucursal sin
  seleccionar. El backend le asigna la del titular.
- Al editar se conservan el tipo de turista y su titular. La sucursal del familiar
  se obtiene del backend. Cambiar la sucursal de un titular actualiza también a
  sus familiares.
- Eliminar abre primero una confirmación. El backend impide borrar turistas con
  reservas, familiares o una cuenta de usuario asociada.

El turista es la persona registrada; no es una cuenta de acceso. Después de crear
un titular, un administrador puede ir a **Usuarios** y asociarle una cuenta Cliente.

`TuristasControlador` recibe las consultas y formularios de las cuatro JSP de
`vistas/turistas`. `GuardarTuristaFormulario` valida los campos obligatorios y
el email. `TuristasApiCliente` envía GET, POST, PUT o DELETE a la API usando la
sesión existente y CSRF para las escrituras. `SucursalesApiCliente` obtiene las
opciones del selector. Los DTO contienen los datos que muestran las pantallas.
No se modificó el backend ni se agregaron JavaScript o estilos.

Se ejecutó `mvn.cmd -f tije-front/pom.xml package` desde la raíz para compilar,
correr las pruebas y generar el WAR. Las pruebas nuevas comprueban permisos,
validaciones, relación familiar, CSRF y errores de la API.

También se ejecutó el WAR contra MySQL de pruebas: alta de titular y familiar,
edición, rechazo de email duplicado, acceso limitado del cliente a su grupo,
eliminación como vendedor y protección de titulares con familiares o cuentas.
Las cuentas y turistas temporales se eliminaron al finalizar. La base principal
no se modificó en esta verificación.

## DNI

El alta y la edición de turistas piden DNI obligatorio de 7 u 8 dígitos, sin
puntos ni espacios. Se muestra en listado, detalle, confirmación de eliminación
y selectores de titulares y de turistas para crear cuentas. Los duplicados se
informan al guardar. Los registros anteriores muestran «DNI pendiente» y
requieren completar el dato real al editarlos. No se cambia el usuario de login.

Los administradores y vendedores también requieren DNI en Usuarios > Crear/Editar.
En el formulario de alta, dejá el DNI vacío para un cliente y elegí un turista
con documento completo. El DNI del cliente se consulta desde ese turista y se
modifica desde Turistas. El documento se muestra en Usuarios, Mi cuenta y la
confirmación de eliminación. Las cuentas anteriores muestran DNI pendiente.
La edición de usuario sigue requiriendo una nueva contraseña, como antes.

## Gestionar reservas

Ingresá y abrí **Reservas**. Administradores y vendedores pueden crear, editar
y eliminar; los clientes solamente consultan las reservas de su grupo familiar.

1. Elegí **Crear reserva**.
2. Seleccioná el turista (se muestra su DNI), el vuelo y el hotel.
3. Elegí clase de vuelo y tipo de hospedaje.
4. Ingresá llegada y partida. La llegada debe coincidir con la fecha del vuelo,
   el hotel debe estar en la ciudad de destino y la partida debe ser posterior.
5. Guardá. La API confirma la disponibilidad y evita repetir turista y vuelo.

El listado muestra turista, DNI, vuelo, hotel y fechas. El detalle agrega nombres,
destino, clase, hospedaje y sucursal de contratación. Editar conserva los datos
para cambiarlos; la sucursal original se mantiene y no se envía como campo editable.
La API descuenta la propia reserva al verificar las plazas durante la edición.

Eliminar abre una confirmación con todos los datos. La reserva se borra solamente
al enviar el formulario de confirmación: libera las plazas y conserva al turista,
el hotel y el vuelo. No es un cambio de estado ni se guarda un historial de cancelación.

El recorrido del código es JSP -> `ReservasControlador` -> `ReservasApiCliente`
-> API del backend. `GuardarReservaFormulario` valida campos y fechas, y
`ReservaRespuesta` recibe los datos de la API. Las cuatro JSP están en
`WEB-INF/vistas/reservas`; `resumen.jspf` comparte la presentación del detalle
con la confirmación de eliminación. Se usa la conexión de la sesión existente
con CSRF para las escrituras. Las consultas de turistas del cliente y sus reservas
siguen siendo filtradas por el backend, no por el menú de la página.

Ante un error de fechas, falta de plazas, duplicado o incompatibilidad se conserva
el formulario con un mensaje. Si se pierde la conexión al guardar o eliminar,
se pide consultar el listado antes de reintentar. No se repiten escrituras
automáticamente. Los catálogos vacíos y las sesiones vencidas también se contemplan.

Se compiló y generó el WAR con `mvn.cmd -f tije-front/pom.xml package` desde la
raíz. Las 111 pruebas del frontend incluyen ahora permisos de reservas, CSRF,
fechas ISO, validaciones, errores de la API, edición y confirmación de eliminación.
No se modificaron el backend, la base, JavaScript ni CSS para esta etapa.

## Administrar hoteles y vuelos

Ingresá como administrador y abrí **Hoteles** o **Vuelos**. Desde el listado podés
agregar, editar o abrir la confirmación para eliminar. Las consultas de ambos
catálogos siguen disponibles sin iniciar sesión.

El formulario de hotel pide nombre, dirección, ciudad, teléfono y capacidad. La
capacidad puede ser cero. El de vuelo pide número, fecha y hora, origen, destino y
plazas. La suma de plazas turista y primera no puede superar el total. El número
del vuelo no cambia durante una edición. El backend impide modificar datos que
dejarían reservas incompatibles y eliminar hoteles o vuelos con reservas.

Las JSP envían formularios POST a `HotelesGestionControlador` o
`VuelosGestionControlador`. Esos controladores comprueban el rol actual con la API,
validan los datos y llaman a `HotelesGestionApiCliente` o
`VuelosGestionApiCliente`. Los clientes usan la conexión privada de la sesión y
el token CSRF del backend para crear, modificar o eliminar. Al terminar se vuelve
al listado con un mensaje. Los clientes públicos de consulta no cambiaron.

Las páginas nuevas están en `WEB-INF/vistas/hoteles` y
`WEB-INF/vistas/vuelos`. No se agregó JavaScript ni diseño visual nuevo.

Se generó el WAR con `mvn -q package`: las 120 pruebas del frontend pasaron.
Además se ejecutó el WAR contra una base MySQL de demostración y se comprobaron
por HTTP el alta, la edición y la baja de un hotel y un vuelo desde las JSP.
También se verificó que la fecha de un vuelo se precarga correctamente al editarlo.
Los registros temporales se eliminaron al finalizar.

## Administrar sucursales

El listado de **Sucursales** está disponible sin iniciar sesión. El administrador
puede agregar una dirección y teléfono, editar esos datos o abrir la confirmación
de eliminación. La API rechaza una dirección repetida y no permite borrar una
sucursal vinculada a turistas o reservas.

`SucursalesControlador` recibe los formularios JSP y consulta el rol actual en la
API antes de cada operación privada. `GuardarSucursalFormulario` valida los dos
campos. `SucursalesApiCliente` consulta y envía los cambios al backend usando la
sesión y el token CSRF. Las JSP están en `WEB-INF/vistas/sucursales`. No se agregó
JavaScript ni CSS nuevo.

Se ejecutó `mvn -q package` y pasaron las 126 pruebas del frontend. También se
probó el WAR por HTTP contra MySQL de demostración: listado público, alta,
dirección duplicada, edición, rechazo de baja con un turista asociado y
eliminación confirmada. Se retiraron los datos temporales al finalizar.

El 05/10/2026 se verificó además el WAR contra MySQL con datos temporales:
alta como vendedor, duplicados, vuelo y hotel completos, edición sin contar dos
veces las propias plazas, fechas incompatibles, permisos del cliente y
eliminación con liberación de plazas. Se eliminaron al finalizar todas las
reservas, cuentas, turistas, hotel y vuelo creados para esa prueba.
