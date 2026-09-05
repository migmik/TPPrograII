# Tije Travel - Version Web

Proyecto de Programacion II para gestionar una cadena de agencias de viajes.

Esta rama contiene la migracion de la entrega original de consola hacia una aplicacion web con backend Spring Boot, base de datos MySQL y frontend separado.

La version entregada del TP original se conserva en `main` y en el tag `v1.0-tp-entregado`.

## Estado Actual

- Backend creado como proyecto Maven/Spring Boot en `tije-back`.
- Codigo de la version consola movido dentro de la estructura Maven.
- Frontend reservado en `tije-front`.
- Scripts y recursos de base de datos reservados en `database`.
- Datos `.txt` heredados conservados temporalmente en `tije-back/datos`.
- Persistencia MySQL, entidades JPA, servicios REST y frontend web pendientes de implementacion.

## Arquitectura Objetivo

```text
Navegador del usuario
        |
        v
Frontend web
        |
        v
Backend Spring Boot
        |
        v
Base de datos MySQL
```

Para la demostracion en red, la base de datos puede ejecutarse en un equipo y el backend/frontend en otro. El navegador del profesor debe entrar al equipo donde se sirva la aplicacion web.

## Estructura Del Repo

```text
TPPrograII/
  README.md
  consignas.txt
  pruebas.txt
  docs/
  database/
  tije-back/
    pom.xml
    mvnw
    mvnw.cmd
    .mvn/
    datos/
    src/
      main/
        java/
          com/tijetravel/tije_back/
        resources/
      test/
        java/
  tije-front/
```

## Backend

El backend esta ubicado en `tije-back` y usa:

- Java 21.
- Maven.
- Spring Boot.
- Spring Web MVC.
- Spring Data JPA.
- MySQL Driver.
- Bean Validation.

Clase principal de Spring Boot:

```text
tije-back/src/main/java/com/tijetravel/tije_back/TijeBackApplication.java
```

El codigo heredado de la version consola todavia existe como base de migracion:

- `modelos`: clases del dominio original.
- `controladores`: logica de negocio original, pendiente de migrar a servicios.
- `persistencia`: lectura y escritura en archivos `.txt`, pendiente de reemplazar por Spring Data JPA.
- `vistas`: menus de consola, pendientes de reemplazar por controllers REST y frontend.

## Base De Datos

La base de datos objetivo es MySQL.

La carpeta `database` queda reservada para:

- `schema.sql`: estructura de tablas.
- `seed.sql`: datos iniciales.
- scripts auxiliares de carga o reinicio de datos.

Todavia no hay datasource definitivo configurado. Hasta completar `application.properties`, el arranque de Spring Boot puede fallar por falta de configuracion de MySQL.

## Frontend

La carpeta `tije-front` queda reservada para la interfaz web.

El frontend no debe conectarse directo a MySQL. El flujo correcto es:

```text
Frontend -> API REST del backend -> MySQL
```

Cuando el backend este listo, el frontend debe consumir endpoints bajo rutas como:

```text
/api/hoteles
/api/vuelos
/api/turistas
/api/reservas
/api/usuarios
```

## Version Original

La version 1 era una aplicacion de consola en Java con persistencia en archivos de texto.

Para verla:

```powershell
git switch main
```

O desde el tag:

```powershell
git checkout v1.0-tp-entregado
```

Para volver al desarrollo web:

```powershell
git switch migue-v2
```

## Comandos Utiles

Desde la raiz del repositorio:

```powershell
cd tije-back
.\mvnw.cmd -v
```

Muestra la version del Maven Wrapper y confirma que el backend Maven puede ejecutarse.

Cuando MySQL y `application.properties` esten configurados:

```powershell
cd tije-back
.\mvnw.cmd spring-boot:run
```

Levanta el backend Spring Boot.

## Flujo De Trabajo Con Git

- `main`: version estable entregada.
- `v2-web`: rama integradora de la version web.
- `migue-v2`: rama personal de trabajo.

El flujo recomendado es:

```text
rama personal -> Pull Request -> v2-web
```

`main` solo deberia actualizarse cuando la version web este estable y lista para presentar.

Antes de empezar a trabajar:

```powershell
git status --short --branch
```

Verificar que la rama actual sea la esperada y que no haya cambios pendientes inesperados.

## Proximas Prioridades

1. Migrar modelos a entidades JPA.
2. Crear repositorios con Spring Data JPA.
3. Migrar la logica de negocio a servicios.
4. Crear controllers REST.
5. Configurar login, roles y permisos.
6. Agregar validaciones y manejo centralizado de errores.
7. Conectar el frontend con la API.
8. Actualizar documentacion y UML de la version web.
