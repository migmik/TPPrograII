# UML Tije Travel

Este diagrama representa el nucleo de la version web. La version PlantUML se
encuentra en `uml-tijetravel.puml`.

Las exportaciones `uml.svg` y `uml.pdf` corresponden al UML de la primera
entrega y se conservan como referencia historica.

## Capas

```mermaid
flowchart TB
    WEB[Frontend web - pendiente]
    API[Controllers REST y DTOs - pendiente]
    CTRL[Controladores de negocio]
    REPO[Repositorios Spring Data JPA]
    MODEL[Modelos y enums]
    ERR[Excepciones de negocio]
    DB[(MySQL)]

    WEB --> API
    API --> CTRL
    CTRL --> REPO
    CTRL --> MODEL
    CTRL --> ERR
    REPO --> MODEL
    REPO --> DB
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
        -int plazasDisponibles
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
    Reserva --> Sucursal
    Reserva --> Vuelo
    Reserva --> Hotel
```

## Componentes

```mermaid
classDiagram
    class GenericoRepositorio {
        <<interface>>
    }
    class SucursalRepositorio
    class HotelRepositorio
    class VueloRepositorio
    class TuristaRepositorio
    class ReservaRepositorio
    class UsuarioRepositorio

    class SucursalesControlador
    class HotelesControlador
    class VuelosControlador
    class TuristasControlador
    class ReservasControlador
    class UsuariosControlador
    class AutenticacionControlador
    class AutorizacionControlador

    GenericoRepositorio <|-- SucursalRepositorio
    GenericoRepositorio <|-- HotelRepositorio
    GenericoRepositorio <|-- VueloRepositorio
    GenericoRepositorio <|-- TuristaRepositorio
    GenericoRepositorio <|-- ReservaRepositorio
    GenericoRepositorio <|-- UsuarioRepositorio

    SucursalesControlador --> SucursalRepositorio
    HotelesControlador --> HotelRepositorio
    VuelosControlador --> VueloRepositorio
    TuristasControlador --> TuristaRepositorio
    ReservasControlador --> ReservaRepositorio
    UsuariosControlador --> UsuarioRepositorio
    AutenticacionControlador --> UsuarioRepositorio
```
