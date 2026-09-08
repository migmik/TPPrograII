# UML Tije Travel

Este diagrama representa el nucleo de la version web. La version PlantUML se
encuentra en `uml-tijetravel.puml`.

Las exportaciones `uml.svg` y `uml.pdf` corresponden al UML de la primera
entrega y se conservan como referencia historica.

## Capas

```mermaid
flowchart TB
    WEB[Frontend web - pendiente]
    SEC[Spring Security - sesion, CSRF y roles]
    API[API REST publica y protegida]
    APIERR[Manejador global de errores]
    CTRL[Servicios de negocio]
    REPO[Repositorios Spring Data JPA]
    MODEL[Modelos y enums]
    ERR[Excepciones de negocio]
    MIG[Flyway - migraciones SQL]
    DB[(MySQL)]

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
    class Usuario {
        <<abstract>>
        -Integer codigo
        -String nombreUsuario
        -String contrasenia
        -RolUsuario rol
        +actualizarCredenciales()
        +tienePermiso(Permiso) boolean
    }
    class Administrador
    class Vendedor
    class Cliente
    class Turista {
        -Integer codigo
        -String nombre
        -String apellido
        -String email
        +actualizarDatos()
        +isTitular() boolean
    }
    class Sucursal {
        -Integer codigo
        -String direccion
        -String telefono
    }
    class Hotel {
        -Integer codigo
        -String nombre
        -String ciudad
        -int capacidadTotal
    }
    class Vuelo {
        -Integer numero
        -LocalDateTime fechaYHora
        -String origen
        -String destino
        -int totalPlazas
        -int plazasTurista
        -int plazasPrimera
    }
    class Reserva {
        -Integer codigo
        -ClaseVuelo claseVuelo
        -TipoHospedaje tipoHospedaje
        -LocalDate fechaLlegada
        -LocalDate fechaPartida
    }

    Usuario <|-- Administrador
    Usuario <|-- Vendedor
    Usuario <|-- Cliente
    Cliente --> Turista
    Turista --> Sucursal : sucursalContratacion
    Turista --> Turista : titular
    Reserva --> Turista
    Reserva --> Sucursal : sucursal historica
    Reserva --> Vuelo
    Reserva --> Hotel
```

## Componentes

```mermaid
classDiagram
    class JpaRepository {
        <<interface>>
    }
    class SucursalRepositorio
    class HotelRepositorio
    class VueloRepositorio
    class TuristaRepositorio
    class ReservaRepositorio
    class UsuarioRepositorio

    class SucursalServicio
    class HotelServicio
    class VueloServicio
    class TuristaServicio
    class ReservaServicio
    class UsuarioServicio
    class BloqueoEscrituras
    class OcupacionHotel
    class DisponibilidadServicio
    class UsuarioFactory
    class CreadorUsuario {
        <<interface>>
    }
    UsuarioServicio --> UsuarioFactory
    UsuarioFactory --> CreadorUsuario
    class AutorizacionServicio
    class AutenticacionRestControlador
    class TuristasRestControlador
    class ReservasRestControlador
    class UsuariosRestControlador
    class ConfiguracionSeguridad
    class UsuarioDetallesServicio
    class UsuarioActualServicio
    class UsuarioAutenticado

    JpaRepository <|-- SucursalRepositorio
    JpaRepository <|-- HotelRepositorio
    JpaRepository <|-- VueloRepositorio
    JpaRepository <|-- TuristaRepositorio
    JpaRepository <|-- ReservaRepositorio
    JpaRepository <|-- UsuarioRepositorio

    SucursalServicio --> SucursalRepositorio
    HotelServicio --> HotelRepositorio
    VueloServicio --> VueloRepositorio
    TuristaServicio --> TuristaRepositorio
    ReservaServicio --> BloqueoEscrituras
    HotelServicio --> BloqueoEscrituras
    VueloServicio --> BloqueoEscrituras
    TuristaServicio --> BloqueoEscrituras
    UsuarioServicio --> BloqueoEscrituras
    SucursalServicio --> BloqueoEscrituras
    HotelServicio --> OcupacionHotel
    DisponibilidadServicio --> OcupacionHotel
    ReservaServicio --> DisponibilidadServicio
    DisponibilidadServicio --> ReservaRepositorio
    DisponibilidadServicio --> VueloRepositorio
    DisponibilidadServicio --> HotelRepositorio
    ReservaServicio --> ReservaRepositorio
    UsuarioServicio --> UsuarioRepositorio
    AutenticacionRestControlador --> ConfiguracionSeguridad
    SucursalesRestControlador --> UsuarioActualServicio
    HotelesRestControlador --> UsuarioActualServicio
    VuelosRestControlador --> UsuarioActualServicio
    HotelesRestControlador --> DisponibilidadServicio : consulta disponibilidad
    VuelosRestControlador --> DisponibilidadServicio : consulta disponibilidad
    TuristasRestControlador --> TuristaServicio
    ReservasRestControlador --> ReservaServicio
    UsuariosRestControlador --> UsuarioServicio
    TuristasRestControlador --> UsuarioActualServicio
    ReservasRestControlador --> UsuarioActualServicio
    UsuariosRestControlador --> UsuarioActualServicio
    ConfiguracionSeguridad --> UsuarioDetallesServicio
    UsuarioActualServicio --> UsuarioRepositorio
    UsuarioDetallesServicio --> UsuarioRepositorio
    UsuarioDetallesServicio --> UsuarioAutenticado
```
