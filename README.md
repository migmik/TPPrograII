# Tije Travel - TP Programacion II

Trabajo practico de Programacion II para gestionar una cadena de agencias de viajes.

Esta version corresponde a la entrega del TP original: una aplicacion de consola en Java, con persistencia en archivos de texto y sin base de datos ni interfaz grafica.

## Alcance

El sistema administra la informacion principal de Tije Travel:

- Sucursales de la agencia.
- Hoteles contratados.
- Vuelos disponibles.
- Turistas titulares y familiares.
- Reservas de vuelos y hospedajes.
- Usuarios con distintos roles de acceso.

La aplicacion aplica conceptos de programacion orientada a objetos: abstraccion, encapsulamiento, herencia, polimorfismo y persistencia.

## Funcionalidades

- Inicio de sesion con usuarios de tipo cliente, vendedor y administrador.
- Consultas generales de hoteles, vuelos y sucursales.
- Consulta de reservas por parte de clientes.
- Administracion de clientes y reservas por parte de vendedores.
- Administracion completa por parte de administradores.
- Alta, modificacion, busqueda, listado y baja de entidades principales.
- Validacion de permisos segun el rol del usuario.
- Validacion de disponibilidad de vuelos y hoteles al crear o modificar reservas.
- Validacion de compatibilidad entre vuelo, hotel y fecha de llegada.
- Validacion de relaciones entre turistas titulares y familiares.
- Persistencia de los cambios en archivos de texto.

## Usuarios de prueba

Los usuarios iniciales estan definidos en `TijeTravel/datos/usuarios.txt`.

| Usuario | Contrasenia | Rol | Descripcion |
| --- | --- | --- | --- |
| `admin` | `admin` | Administrador | Puede administrar todo el sistema. |
| `vendedor` | `1234` | Vendedor | Puede administrar clientes y reservas. |
| `juan` | `juan` | Cliente | Puede consultar hoteles, vuelos y sus reservas. |

## Estructura del proyecto

```text
TPPrograII/
  README.md
  consignas.txt
  pruebas.txt
  docs/
  TijeTravel/
    build.xml
    manifest.mf
    nbproject/
    datos/
    src/
      tijetravel/
        Main.java
        controladores/
        modelos/
        persistencia/
        vistas/
```

## Paquetes principales

- `modelos`: contiene las clases del dominio, como `Agencia`, `Sucursal`, `Hotel`, `Vuelo`, `Turista`, `Reserva` y `Usuario`.
- `controladores`: contiene la logica que coordina las operaciones del sistema y valida permisos.
- `persistencia`: contiene las clases encargadas de cargar y guardar datos en archivos de texto.
- `vistas`: contiene los menus de consola para cada tipo de usuario.

## Modelo de usuarios

El sistema trabaja con tres roles:

- `Cliente`: puede consultar hoteles, vuelos y reservas propias.
- `Vendedor`: puede administrar clientes y reservas.
- `Administrador`: puede administrar sucursales, hoteles, vuelos, clientes, reservas y usuarios.

Para aplicar herencia y polimorfismo, `Cliente`, `Vendedor` y `Administrador` heredan de `Usuario`. Cada tipo de usuario define su comportamiento frente a los permisos disponibles.

La clase `UsuarioFactory` centraliza la creacion del tipo correcto de usuario cuando se cargan datos desde archivo o cuando se administra un usuario desde el sistema.

## Persistencia

Los datos se guardan en archivos `.txt` dentro de `TijeTravel/datos`:

- `sucursales.txt`
- `hoteles.txt`
- `vuelos.txt`
- `turistas.txt`
- `usuarios.txt`
- `reservas.txt`

Cada linea representa un registro y los campos se separan con `;`.

La aplicacion carga los datos al iniciar y guarda los cambios cuando se realizan altas, bajas o modificaciones desde los menus.

## Compilar y ejecutar

Los comandos deben ejecutarse desde la raiz del repositorio.

### PowerShell

```powershell
New-Item -ItemType Directory -Force TijeTravel\out | Out-Null
javac -encoding UTF-8 -d TijeTravel\out (Get-ChildItem -Recurse TijeTravel\src -Filter *.java).FullName
java -cp TijeTravel\out tijetravel.Main
```

### Linux/macOS

```bash
mkdir -p TijeTravel/out
javac -encoding UTF-8 -d TijeTravel/out $(find TijeTravel/src -name "*.java")
java -cp TijeTravel/out tijetravel.Main
```

## Ejecutar desde NetBeans

Tambien se puede abrir la carpeta `TijeTravel` como proyecto de NetBeans y ejecutar la clase principal:

```text
tijetravel.Main
```

Los archivos privados de NetBeans no forman parte del repositorio.

## Pruebas manuales sugeridas

El archivo `pruebas.txt` contiene una lista de pruebas manuales para revisar el funcionamiento general.

Casos recomendados:

- Iniciar el sistema y salir con la opcion `0`.
- Entrar como `admin/admin` y verificar el menu de administrador.
- Entrar como `vendedor/1234` y verificar el menu de vendedor.
- Entrar como `juan/juan` y verificar el menu de cliente.
- Listar sucursales, hoteles, vuelos, clientes, reservas y usuarios.
- Crear una reserva valida con vuelo y hotel del mismo destino.
- Intentar crear una reserva con hotel de otra ciudad y verificar que el sistema la rechace.
- Intentar usar una fecha de llegada distinta a la fecha del vuelo y verificar que el sistema la rechace.
- Modificar y cancelar una reserva.
- Cerrar y volver a abrir el programa para verificar que los cambios persisten.

## Documentacion

La carpeta `docs` contiene documentacion complementaria de la entrega:

- Documentacion del TP en PDF y DOCX.
- Diagrama UML en PDF y SVG.
- Fuente del diagrama UML en PlantUML.

## Estado de esta version

Esta version esta pensada como cierre de la entrega original del TP.

Para una version futura con base de datos y frontend web, conviene mantener esta entrega marcada en Git con un tag, por ejemplo `v1.0-tp-entregado`, y continuar el desarrollo en una rama separada.
