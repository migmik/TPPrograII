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
- API REST, seguridad web, migracion de datos y frontend aun pendientes.

Los archivos de `tije-back/datos` se mantienen unicamente como referencia para
crear los futuros scripts de carga inicial. La aplicacion ya no los lee ni los
escribe.

## Arquitectura

```text
Frontend web                         (pendiente)
        |
        v
Controllers REST + DTOs             (pendiente)
        |
        v
Controladores de negocio            controladores/
        |
        v
Repositorios Spring Data JPA        repositorios/
        |
        v
MySQL                               configuracion pendiente
```

Los controladores de negocio usan `@Service`. Se conserva el nombre
`controladores` para mantener el estilo del proyecto Gestion Militar; cuando
se agregue la API, los controllers HTTP deben vivir en un paquete separado,
por ejemplo `api`.

## Estructura

```text
TPPrograII/
  database/                         scripts SQL futuros
  docs/                             UML y documentacion
  tije-front/                       frontend futuro
  tije-back/
    datos/                          datos heredados, solo referencia
    pom.xml
    src/
      main/
        java/com/tijetravel/tijeback/
          controladores/            casos de uso y reglas de negocio
          enums/                    roles, permisos y tipos del dominio
          excepciones/              errores esperables del negocio
          modelos/                  entidades y validaciones
          repositorios/             contratos de acceso a datos
          TijeBackApplication.java
        resources/
          application.example.properties
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
- La llegada debe coincidir con la fecha del vuelo y la ciudad del hotel con su
  destino.
- Se controla la capacidad por clase del vuelo y la ocupacion superpuesta del
  hotel.
- Una reduccion de capacidad no puede dejar reservas existentes fuera de cupo.

## Configuracion

El ejemplo de configuracion se encuentra en
`tije-back/src/main/resources/application.example.properties`. Admite estas
variables de entorno:

```text
DB_URL
DB_USER
DB_PASSWORD
```

La configuracion definitiva de MySQL y los scripts de `database/` deben
completarse antes de ejecutar la aplicacion contra datos reales.

## Verificacion

Desde `tije-back`:

```powershell
.\mvnw.cmd test
```

Las pruebas usan el perfil `test` con H2 en memoria, no dependen de la
configuracion local y no requieren una instancia de MySQL. La suite verifica
las relaciones JPA y las principales reglas heredadas del sistema de consola.

## Proximas etapas

1. Definir `schema.sql` o migraciones versionadas y convertir los `.txt` en
   datos iniciales.
2. Incorporar DTOs y controllers REST sin exponer directamente las entidades.
3. Agregar un manejador global que traduzca excepciones a respuestas HTTP.
4. Reemplazar la comparacion de contrasenias en texto plano por hash y configurar
   Spring Security.
5. Implementar el frontend y consumir exclusivamente la API del backend.
