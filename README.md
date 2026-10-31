# Tije Travel

Trabajo práctico de Programación II para administrar una cadena de agencias de
viajes. Permite gestionar sucursales, hoteles, vuelos, turistas, reservas y usuarios.

El backend está implementado y probado con MySQL. El frontend permite consultar y
administrar sucursales, hoteles, vuelos, turistas, reservas, usuarios y roles según
los permisos de cada usuario, y consultar el grupo familiar y sus reservas como cliente.

## Funcionalidades

- Búsqueda de vuelos y hoteles con filtros, orden por precio y resultados paginados.
- Combinaciones de vuelo y hotel disponibles desde la página de inicio.
- Alta, modificación y baja lógica de datos según los permisos del usuario. Los
  registros dados de baja se pueden ver y reactivar.
- Roles editables: cada rol es un conjunto de permisos (uno por módulo) que se
  tildan desde la pantalla de roles.
- Reservas con control de fechas, destino y plazas disponibles.
- Gestión de turistas titulares y sus familiares.
- Registro público de clientes asociado a un nuevo turista titular.
- Inicio y cierre de sesión.

| Rol inicial | Qué puede hacer |
|---|---|
| Administrador | Administrar todos los recursos, usuarios y roles. |
| Vendedor | Administrar turistas y reservas. |
| Cliente | Consultar su grupo familiar y reservar para él. |

Se pueden crear más roles, por ejemplo "Hotelero" con solo el permiso de
administrar hoteles.

Las búsquedas de hoteles y vuelos pueden consultarse sin iniciar sesión. El listado
completo queda reservado a vendedores y administradores. Las sucursales son públicas.

## Tecnologías

Java 21, Spring Boot, Spring Data JPA con Hibernate, Spring Security y MySQL.
Maven compila el proyecto y ejecuta las pruebas. Flyway crea y actualiza las tablas
a partir de los scripts SQL del proyecto.

El frontend es otra aplicación Java con Spring MVC, JSP/JSTL y CSS básico, sin
JavaScript. Consume la API REST y se empaqueta como WAR ejecutable.

## Organización

```text
tije-back/     Backend Java y pruebas
tije-front/    Frontend web independiente con JSP y pruebas
database/      Script para crear la base y guía de configuración
docs/          Documentación de la API, UML y modelo entidad-relación
```

En el backend, los controladores REST reciben las solicitudes, los servicios
aplican las reglas del negocio y los repositorios acceden a la base de datos.
Los modelos representan las entidades y los DTOs definen los datos de la API.

```text
api/controladores   Reciben la peticion HTTP, llaman al servicio y devuelven el DTO
api/dto             Datos que entran y salen de la API (se serializan a JSON)
api/mapeadores      Convierten entidades en DTOs (MapeadorBase)
api/errores         Traduce las excepciones a respuestas HTTP
servicios           Interfaces con el contrato de cada servicio
servicios/impl      Implementaciones con las reglas de negocio
servicios/usuarios  Fabrica de usuarios segun el tipo (empleado o cliente)
repositorios        Acceso a la base con Spring Data JPA
modelos             Entidades JPA y sus validaciones
excepciones         Excepciones del negocio
seguridad           Spring Security, sesion y usuario actual
```

Los permisos de cada usuario salen de su rol; Spring Security y los servicios usan
esa misma definición y se vuelven a leer en cada pedido, así un cambio de rol se
aplica sin cerrar sesión. Los servicios agrupan cada cambio en una transacción:
JPA guarda las modificaciones al finalizar, o las revierte si ocurre un error.

### Diseño

- **Interfaces:** los controladores dependen de las interfaces de servicio
  (`HotelServicio`, `ReservaServicio`, ...) y Spring inyecta la implementación.
- **Polimorfismo:** `Empleado` y `Cliente` redefinen `getTipoUsuario`,
  `puedeVerTurista` y `getCodigoGrupoFamiliar`, así los servicios no preguntan
  por el tipo de usuario. `ModuloBajas` (frontend) es un enum con un método
  distinto en cada valor.
- **Herencia:** todas las entidades heredan de `EntidadBase` (fecha de creación
  y de baja).
- **Patrones:** Factory + Strategy para crear usuarios (`UsuarioFactory` y
  `CreadorUsuario`), Template Method en `MapeadorBase`, Builder en `RutaConsulta`
  (frontend), Repository y DTO.
- **Excepciones:** `ReglaNegocioException` y sus subclases; el
  `ManejadorGlobalExcepciones` las convierte en el código HTTP que corresponde.
- **Programación orientada a objetos tradicional:** el código usa bucles,
  clases y clases anónimas en lugar de streams y expresiones lambda.

## Ejecutar el backend

Se necesita Java 21 y MySQL en ejecución.

1. Crear la base ejecutando `database/crear_base.sql` en MySQL o Workbench.
2. Configurar `DB_URL`, `DB_USER` y `DB_PASSWORD` para esa base.
3. Desde la raíz del proyecto, ejecutar en PowerShell:

```powershell
cd tije-back
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

El perfil `dev` carga datos de demostración. El backend atiende por defecto en
`http://localhost:8080`; por ejemplo, `GET /api/v1/hoteles/pagina?ciudad=Salta`
devuelve hoteles filtrados. El listado completo requiere una sesión de personal.

La [guía de base de datos](database/README.md) explica la configuración y cómo
crear el primer administrador. Las credenciales locales no deben subirse a Git.

## Ejecutar el frontend

Con el backend funcionando en `http://localhost:8080`, abrir otra terminal desde
la raíz del proyecto:

```powershell
cd tije-front
.\mvnw.cmd spring-boot:run
```

La página está en **http://localhost:8081**. La [guía del frontend](tije-front/README.md)
explica los archivos, el recorrido de las consultas, cómo cambiar la URL de la API
y cómo generar y ejecutar el WAR.

## Pruebas

Desde `tije-back`:

```powershell
.\mvnw.cmd test
```

Por defecto se usa H2 en memoria, sin necesitar MySQL. Las pruebas cubren reglas
de negocio, persistencia, API, permisos y operaciones simultáneas. La suite también
se verificó contra MySQL, junto con una prueba HTTP y de reinicio del backend.

## Reglas importantes

Los vuelos tienen un precio por persona de $150.000 a $300.000 (múltiplos de
$10.000). Los hoteles cobran de $25.000 a $55.000 por persona y noche (múltiplos
de $1.000). Primera clase suma 10% al vuelo y pensión completa suma 5% al hotel.
El importe calculado queda guardado en la reserva, por lo que un cambio posterior
del catálogo no modifica una venta ya hecha. La reserva pasa por una pantalla de
pago simulado de cuatro segundos; no se solicitan datos de tarjeta. El balance
del administrador suma solamente las reservas pagadas.

- Un turista no puede reservar dos veces el mismo vuelo.
- Las fechas, el destino del vuelo y la ciudad del hotel deben ser compatibles.
- La disponibilidad hotelera considera la ocupación simultánea durante la estadía.
- Cambiar capacidades o datos de un viaje no puede dejar reservas incompatibles.
- Los familiares comparten la sucursal del titular. Las reservas conservan la
  sucursal donde se contrataron, aunque el grupo cambie de sucursal después.
- Las escrituras se procesan de a una para evitar conflictos al reservar cupos.

## Documentación y próximos pasos

- [API REST](docs/api.md)
- [UML](docs/uml-tijetravel.md) y [archivo PlantUML](docs/uml-tijetravel.puml)
- [Modelo entidad-relación](docs/der-tijetravel.md)
- [Despliegue en Railway](docs/despliegue-railway.md)

Para la presentación se prevé ejecutar MySQL, el backend y el frontend como tres
servicios separados en Railway. La guía indica cómo conectarlos y comprobarlos.

El DNI se registra para turistas, administradores y vendedores. Los clientes
usan el del turista asociado, sin duplicarlo. Es obligatorio en las altas y
modificaciones de personas. Los registros previos quedan con DNI pendiente hasta completar
el dato real. Ver [actualización de la base](database/README.md#incorporación-del-dni).
