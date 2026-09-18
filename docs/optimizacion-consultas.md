# Revisión de la hoja de ruta de optimización

Se aplicaron las fases 1, 2, 3 y 8 al backend: consultas con relaciones explícitas
y pruebas de regresión. No requieren migraciones de base de datos ni cambios en
el contrato HTTP del frontend.

## Cambios aplicados

- `ReservaRepositorio.findByHotelCodigo` carga el vuelo en la misma consulta.
  La validación de ciudad en `HotelServicio.modificar` evita una consulta
  adicional por cada vuelo distinto.
- `ReservaRepositorio.findByVueloNumero` carga el hotel en la misma consulta.
  La validación de destino en `VueloServicio.modificar` evita una consulta
  adicional por cada hotel distinto.
- `Reserva.completa` concentra el grafo utilizado por las cuatro consultas de
  reservas completas, incluyendo el titular del turista.
- `UsuarioRepositorio` carga explícitamente el turista de los clientes mediante
  `left join fetch treat(u as Cliente).turista`, tanto al listar como al buscar
  por código **y por nombre de usuario**. Esta última consulta es la utilizada
  durante el inicio de sesión y también necesitaba contemplarse.
- Se quitaron las llamadas a `getCodigoTurista()` cuyo resultado se descartaba.
  Se conserva la llamada en `UsuarioAutenticado.desde`: su resultado forma parte
  de los datos de la sesión, por lo que eliminarla sería incorrecto.

El uso de `join fetch` y `treat` se contrastó con la
[documentación de Hibernate sobre consultas HQL](https://docs.jboss.org/hibernate/orm/7.0/querylanguage/html_single/Hibernate_Query_Language.html).
Las pruebas comprueban su comportamiento con la versión usada por el proyecto.
La carga del turista no implica cargar también todas sus relaciones anidadas;
si un DTO futuro necesita otras relaciones, deberá declararlas explícitamente.

## Propuestas que no se aplican en este cambio

| Propuesta | Evaluación sobre el código actual |
|---|---|
| Quitar el bloqueo de usuarios, turistas y sucursales | No es seguro hacerlo como sugiere el documento. Dos eliminaciones concurrentes de administradores pueden dejar al sistema sin administradores. Crear un familiar mientras se traslada el titular puede dejar sucursales distintas. También existen comprobaciones de dependencias antes de eliminar entidades. |
| Bloqueos por vuelo/hotel o `@Version` | Requieren diseñar la coordinación de todas las reglas anteriores. Agregar `@Version` a vuelo/hotel no evita por sí solo dos altas de reservas: insertar una reserva no modifica necesariamente esas entidades ni incrementa sus versiones. Se conserva el bloqueo global existente. |
| Anotación y aspecto para bloquear | El aspecto propuesto necesita ejecutarse dentro de la transacción y contemplar las llamadas internas de `TuristaServicio.crear` a `crearTitular`/`crearFamiliar`. Las llamadas internas no atraviesan el proxy de Spring. Se mantiene la coordinación explícita, que ya está probada. |
| Actualización masiva de familiares | Es una optimización futura. Evitaría las validaciones de `Turista.cambiarSucursal` y exigiría gestionar las entidades ya cargadas para que el contexto de persistencia no conserve datos anteriores. No hay evidencia de grupos suficientemente grandes para justificarlo ahora. |
| Records de comandos | Son una opción de diseño válida, pero no corrigen consultas ni un fallo actual. Se mantienen las firmas existentes para evitar una refactorización transversal de servicios, controladores y pruebas en este cambio. |
| Helper genérico `obtenerOFallar` | Los métodos actuales son pequeños y expresan la entidad y su error directamente. Extraerlos no mejora el rendimiento y añade una abstracción para muy pocas líneas; se mantienen. |

## Verificación

`CargaRelacionesIntegracionTest` usa una base H2 independiente, transacciones con
rollback y estadísticas de Hibernate habilitadas únicamente para esa clase.
Antes de medir, hace `flush`, limpia el contexto de persistencia y reinicia los
contadores, para que las entidades guardadas al preparar datos no oculten N+1.

Las pruebas verifican:

- Una sola sentencia SQL para reservas de un hotel con tres vuelos diferentes.
- Una sola sentencia SQL para reservas de un vuelo con tres hoteles diferentes.
- Las cuatro consultas que comparten `Reserva.completa`, con acceso a sus
  relaciones después de separar las entidades del contexto de persistencia.
- Una sola sentencia al listar usuarios, conservando clientes, administradores
  y vendedores, y lectura del nombre del turista sin carga diferida posterior.
- Búsqueda por código y por nombre para los tres roles, conservando la búsqueda
  sin distinguir mayúsculas y el resultado vacío cuando el usuario no existe.

Desde `tije-back`, ejecutar `./mvnw.cmd test` para correr también las pruebas
existentes de servicios, API, seguridad, migraciones y concurrencia.
Resultado de la verificación: **95 pruebas, 0 fallos, 0 errores, 0 omitidas**;
14 corresponden a la nueva clase de regresión.
La medición de consultas de esta clase usa H2; no sustituye una prueba de carga
contra MySQL.
