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
- Inicio y cierre de sesión con los usuarios existentes del backend.
- Pantalla «Mi cuenta» con el nombre de usuario y el rol informado por la API.
- Mensajes para fechas inválidas, recursos inexistentes y problemas de conexión.

Los catálogos son públicos. Las altas, modificaciones, eliminaciones y las
pantallas de los otros recursos quedan para las siguientes etapas. El CSS actual
solo facilita la lectura.

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
| `clientes/HotelesApiCliente.java` | Reúne las tres llamadas HTTP de hoteles. |
| `clientes/VuelosApiCliente.java` | Consulta listado, detalle y disponibilidad por clase de vuelos. |
| `controladores/InicioControlador.java` | Muestra la página de inicio. |
| `controladores/HotelesControlador.java` | Recibe las solicitudes y prepara los datos de las páginas. |
| `controladores/VuelosControlador.java` | Prepara el listado de vuelos y el detalle con ambas clases. |
| `controladores/ManejadorErrores.java` | Convierte errores de conexión o consulta en una página comprensible. |
| `dto/` | Datos recibidos de la API. No son entidades JPA ni acceden a la base. |
| `dto/VueloRespuesta.java` y `dto/DisponibilidadVueloRespuesta.java` | Datos del vuelo y de las plazas libres recibidas. |
| `formularios/ConsultaDisponibilidad.java` | Datos y validaciones del formulario. |
| `resources/application.properties` | Puerto, URL del backend y ubicación de las JSP. |
| `resources/messages.properties` | Mensajes en español para fechas mal escritas. |
| `webapp/WEB-INF/vistas/` | Páginas JSP y navegación compartida. |
| `webapp/WEB-INF/vistas/vuelos/` | `lista.jsp` recorre el catálogo; `detalle.jsp` muestra el vuelo y sus plazas. |
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
`@SessionScope` crea un `AutenticacionApiCliente` por sesión del navegador, cada
uno con su propio `CookieManager`. Por eso las cookies de dos usuarios no se mezclan.
Los clientes de hoteles y vuelos conservan su conexión pública compartida.

`DatosNavegacion` agrega el usuario guardado al modelo de las páginas para mostrar
«Mi cuenta» y «Cerrar sesión», e indica al navegador que no guarde esas páginas
en caché. Ese dato del usuario sirve para la presentación; no reemplaza
las comprobaciones de sesión y permisos que hace la API. Esta etapa muestra el rol;
las pantallas para realizar operaciones según ese rol se incorporarán después.

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
| `clientes/AutenticacionApiCliente.java` | Cookies por sesión y llamadas de autenticación a la API. |
| `controladores/SesionControlador.java` | Ingreso, consulta de cuenta y salida. |
| `controladores/DatosNavegacion.java` | Datos del usuario para la navegación compartida. |
| `formularios/IniciarSesionFormulario.java` | Campos y validaciones del ingreso. |
| `dto/SesionRespuesta.java` y `dto/CsrfRespuesta.java` | Respuestas recibidas de la API. |
| `webapp/WEB-INF/vistas/sesion/` | Formulario de ingreso y página de cuenta. |

Referencias: [CSRF y formularios con Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
y [cliente HTTP de Spring para Java](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/client/JdkClientHttpRequestFactory.html).

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

La suite contiene 37 pruebas automatizadas para hoteles, vuelos y sesiones. Comprueba,
entre otras cosas, que se lean correctamente las fechas del JSON y que se muestren
las plazas libres recibidas aunque sean menores a la capacidad del vuelo.
También verifica el aislamiento de cookies, los tokens CSRF, el cambio de
identificador al ingresar, las credenciales incorrectas, las sesiones vencidas y
la salida cuando la API no responde.

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
