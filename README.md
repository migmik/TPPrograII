# Tije Travel

Trabajo práctico de Programación II para administrar una cadena de agencias de
viajes. Permite gestionar sucursales, hoteles, vuelos, turistas, reservas y usuarios.

El backend está implementado y probado con MySQL. El frontend web está pendiente.

## Funcionalidades

- Consulta de hoteles, vuelos, sucursales y disponibilidad.
- Alta, modificación y eliminación de datos según los permisos del usuario.
- Reservas con control de fechas, destino y plazas disponibles.
- Gestión de turistas titulares y sus familiares.
- Inicio y cierre de sesión.

| Usuario | Qué puede hacer |
|---|---|
| Administrador | Administrar todos los recursos. |
| Vendedor | Administrar turistas y reservas. |
| Cliente | Consultar su grupo familiar y sus reservas. |

Los catálogos de hoteles, vuelos y sucursales pueden consultarse sin iniciar sesión.

## Tecnologías

Java 21, Spring Boot, Spring Data JPA con Hibernate, Spring Security y MySQL.
Maven compila el proyecto y ejecuta las pruebas. Flyway crea y actualiza las tablas
a partir de los scripts SQL del proyecto.

## Organización

```text
tije-back/     Backend Java y pruebas
tije-front/    Carpeta destinada al frontend
database/      Script para crear la base y guía de configuración
docs/         Documentación de la API y diagramas UML
```

En el backend, los controladores REST reciben las solicitudes, los servicios
aplican las reglas del negocio y los repositorios acceden a la base de datos.
Los modelos representan las entidades y los DTOs definen los datos de la API.

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
`http://localhost:8080`; por ejemplo, `GET /api/v1/hoteles` devuelve los hoteles.

La [guía de base de datos](database/README.md) explica la configuración y cómo
crear el primer administrador. Las credenciales locales no deben subirse a Git.

## Pruebas

Desde `tije-back`:

```powershell
.\mvnw.cmd test
```

Por defecto se usa H2 en memoria, sin necesitar MySQL. Las pruebas cubren reglas
de negocio, persistencia, API, permisos y operaciones simultáneas. La suite también
se verificó contra MySQL, junto con una prueba HTTP y de reinicio del backend.

## Reglas importantes

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

Falta implementar el frontend y preparar la presentación con MySQL en una PC,
el backend y frontend en otra, y el acceso desde la computadora del profesor.
