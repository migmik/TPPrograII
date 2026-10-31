# UML Tije Travel

Este diagrama representa el nucleo de la version web. La version PlantUML se
encuentra en `uml-tijetravel.puml`.

## Capas

```mermaid
flowchart TB
    NAV[Navegador - HTML y formularios]
    WEB[Frontend Java - Spring MVC, JSP y cliente HTTP]
    SEC[Spring Security - sesion, CSRF y permisos]
    API[API REST publica y protegida]
    APIERR[Manejador global de errores]
    CTRL[Servicios de negocio]
    REPO[Repositorios Spring Data JPA]
    MODEL[Modelos y enums]
    ERR[Excepciones de negocio]
    MIG[Flyway - migraciones SQL]
    DB[(MySQL)]

    NAV --> WEB
    WEB --> SEC
    SEC --> API
    SEC --> REPO
    API --> CTRL
    API --> APIERR
    CTRL --> REPO
    CTRL --> MODEL
    CTRL --> ERR
    REPO --> MODEL
    REPO --> DB
    MIG --> DB
```

## Dominio

```mermaid
classDiagram
    class EntidadBase {
        <<abstract>>
        -LocalDateTime fechaCreacion
        -LocalDateTime fechaBaja
        +darDeBaja(LocalDateTime)
        +reactivar()
        +estaActivo() boolean
        +seDioDeBajaJuntoCon(EntidadBase) boolean
    }
    class Usuario {
        <<abstract>>
        -Integer codigo
        -String nombreUsuario
        -String contrasenia
        -String dni
        +getTipoUsuario() TipoUsuario*
        +cambiarRol(Rol)
        +actualizarCredenciales()
        +tienePermiso(Permiso) boolean
        +puedeVerTurista(Turista) boolean
        +getCodigoGrupoFamiliar() Integer
        +tieneDniPropio() boolean
    }
    class Empleado {
        +getTipoUsuario() TipoUsuario
    }
    class Cliente {
        +getTipoUsuario() TipoUsuario
        +puedeVerTurista(Turista) boolean
        +getCodigoGrupoFamiliar() Integer
        +tieneDniPropio() boolean
    }
    class Rol {
        -Integer codigo
        -String nombre
        -TipoUsuario tipoUsuario
        -Set~Permiso~ permisos
        +actualizar(String, List~Permiso~)
        +tienePermiso(Permiso) boolean
    }
    class TipoUsuario {
        <<enumeration>>
        EMPLEADO
        CLIENTE
        +admite(Permiso) boolean
    }
    class Permiso {
        <<enumeration>>
        CONSULTAR
        CREAR_RESERVAS
        VER_CATALOGO_COMPLETO
        VER_BALANCE
        ADMINISTRAR_TURISTAS
        ADMINISTRAR_RESERVAS
        ADMINISTRAR_SUCURSALES
        ADMINISTRAR_HOTELES
        ADMINISTRAR_VUELOS
        ADMINISTRAR_USUARIOS
    }
    class Turista {
        -Integer codigo
        -String dni
        -String nombre
        -String apellido
        -String email
        +actualizarDatos()
        +cambiarSucursal(Sucursal)
        +isTitular() boolean
    }
    class Sucursal {
        -Integer codigo
        -String direccion
        -String ciudad
        -String telefono
    }
    class Hotel {
        -Integer codigo
        -String nombre
        -String ciudad
        -int capacidadTotal
        -int precioNochePersona
    }
    class Vuelo {
        -Integer numero
        -LocalDateTime fechaYHora
        -String origen
        -String destino
        -int totalPlazas
        -int plazasTurista
        -int plazasPrimera
        -int precio
    }
    class Reserva {
        -Integer codigo
        -ClaseVuelo claseVuelo
        -TipoHospedaje tipoHospedaje
        -LocalDate fechaLlegada
        -LocalDate fechaPartida
        -int precioVuelo
        -int precioHotel
        -int total
        -boolean pagada
        +actualizarDatos()
        +confirmarPago()
    }
    class ClaseVuelo {
        <<enumeration>>
        TURISTA
        PRIMERA
        +getPorcentajeRecargo() int
    }
    class TipoHospedaje {
        <<enumeration>>
        MEDIA_PENSION
        PENSION_COMPLETA
        +getPorcentajeRecargo() int
    }

    EntidadBase <|-- Usuario
    EntidadBase <|-- Rol
    EntidadBase <|-- Turista
    EntidadBase <|-- Sucursal
    EntidadBase <|-- Hotel
    EntidadBase <|-- Vuelo
    EntidadBase <|-- Reserva
    Usuario <|-- Empleado
    Usuario <|-- Cliente
    Usuario --> Rol
    Rol --> TipoUsuario
    Rol --> Permiso
    Cliente --> Turista
    Turista --> Sucursal : sucursalContratacion
    Turista --> Turista : titular
    Reserva --> Turista
    Reserva --> Sucursal : sucursal historica
    Reserva --> Vuelo
    Reserva --> Hotel
    Reserva --> ClaseVuelo
    Reserva --> TipoHospedaje
```

Los permisos de un usuario salen de su `Rol`, que se edita desde la aplicación
(por ejemplo un rol "Hotelero" que solo administra hoteles). `TipoUsuario` limita
qué permisos puede recibir un rol de clientes. `Cliente` redefine `puedeVerTurista`
y `getCodigoGrupoFamiliar` para limitarse a su grupo familiar, así los servicios no
preguntan por el tipo de usuario (polimorfismo).

Todas las entidades heredan de `EntidadBase`: las bajas son lógicas y guardan la
fecha, para conservar el historial y poder reactivar.

## Componentes

Los controladores dependen de las interfaces de los servicios y no de sus
implementaciones (principio de inversión de dependencias). Spring inyecta la
clase `...Impl` que corresponde.

```mermaid
classDiagram
    class HotelServicio {
        <<interface>>
    }
    class VueloServicio {
        <<interface>>
    }
    class SucursalServicio {
        <<interface>>
    }
    class TuristaServicio {
        <<interface>>
    }
    class ReservaServicio {
        <<interface>>
    }
    class UsuarioServicio {
        <<interface>>
    }
    class DisponibilidadServicio {
        <<interface>>
    }
    class CiudadServicio {
        <<interface>>
    }

    HotelServicio <|.. HotelServicioImpl
    VueloServicio <|.. VueloServicioImpl
    SucursalServicio <|.. SucursalServicioImpl
    TuristaServicio <|.. TuristaServicioImpl
    ReservaServicio <|.. ReservaServicioImpl
    UsuarioServicio <|.. UsuarioServicioImpl
    DisponibilidadServicio <|.. DisponibilidadServicioImpl
    CiudadServicio <|.. CiudadServicioImpl

    HotelesRestControlador --> HotelServicio
    VuelosRestControlador --> VueloServicio
    SucursalesRestControlador --> SucursalServicio
    TuristasRestControlador --> TuristaServicio
    ReservasRestControlador --> ReservaServicio
    UsuariosRestControlador --> UsuarioServicio
    CiudadesRestControlador --> CiudadServicio

    ReservaServicioImpl --> DisponibilidadServicio
    HotelServicioImpl --> DisponibilidadServicio
    UsuarioServicioImpl --> UsuarioFactory
    UsuarioFactory --> CreadorUsuario
    class CreadorUsuario {
        <<interface>>
    }
    CreadorUsuario <|.. CreadorEmpleado
    CreadorUsuario <|.. CreadorCliente
```

### Servicios, repositorios y seguridad

```mermaid
classDiagram
    class JpaRepository {
        <<interface>>
    }
    JpaRepository <|-- SucursalRepositorio
    JpaRepository <|-- HotelRepositorio
    JpaRepository <|-- VueloRepositorio
    JpaRepository <|-- TuristaRepositorio
    JpaRepository <|-- ReservaRepositorio
    JpaRepository <|-- UsuarioRepositorio

    HotelServicioImpl --> HotelRepositorio
    VueloServicioImpl --> VueloRepositorio
    SucursalServicioImpl --> SucursalRepositorio
    TuristaServicioImpl --> TuristaRepositorio
    ReservaServicioImpl --> ReservaRepositorio
    UsuarioServicioImpl --> UsuarioRepositorio
    DisponibilidadServicioImpl --> ReservaRepositorio

    HotelServicioImpl --> AutorizacionServicio
    ReservaServicioImpl --> AutorizacionServicio
    HotelServicioImpl --> BloqueoEscrituras
    ReservaServicioImpl --> BloqueoEscrituras
    HotelServicioImpl --> FiltrosConsulta
    VueloServicioImpl --> ComparadorCercaniaFecha
    DisponibilidadServicioImpl --> OcupacionHotel

    class AuthenticationEntryPoint {
        <<interface>>
    }
    class AccessDeniedHandler {
        <<interface>>
    }
    AuthenticationEntryPoint <|.. EscritorErrorSeguridad
    AccessDeniedHandler <|.. EscritorErrorSeguridad
    ConfiguracionSeguridad --> EscritorErrorSeguridad
    ConfiguracionSeguridad --> UsuarioDetallesServicio
    UsuarioDetallesServicio --> UsuarioRepositorio
    UsuarioActualServicio --> UsuarioRepositorio
```

### Mapeadores (DTO)

```mermaid
classDiagram
    class MapeadorBase~E, R~ {
        <<abstract>>
        +aRespuesta(E) R*
        +aRespuestas(List~E~) List~R~
        +aPagina(Page~E~) PaginaRespuesta~R~
    }
    MapeadorBase <|-- HotelMapeador
    MapeadorBase <|-- VueloMapeador
    MapeadorBase <|-- SucursalMapeador
    MapeadorBase <|-- TuristaMapeador
    MapeadorBase <|-- ReservaMapeador
    MapeadorBase <|-- UsuarioMapeador
```

`MapeadorBase` aplica el patrón Template Method: cada subclase solo dice cómo
convertir un elemento y la base reutiliza esa conversión para listas y páginas.
