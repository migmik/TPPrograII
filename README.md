# TPPrograII

Trabajo practico de Programacion II.

## Tije Travel

El sistema se usa desde consola y guarda los datos en archivos de texto. Permite trabajar con sucursales, hoteles, vuelos, turistas, reservas y usuarios.

## Modelo usado

- `Agencia`: guarda las listas principales y tiene metodos de busqueda, alta y baja.
- `Sucursal`: codigo, direccion y telefono.
- `Hotel`: codigo, nombre, direccion, ciudad, telefono y plazas.
- `Vuelo`: numero, fecha y hora, origen, destino, plazas totales, plazas turista y plazas primera.
- `Turista`: codigo, nombre, apellido, direccion, mail, telefonos y sucursal donde contrato.
- `Reserva`: turista, sucursal, vuelo, hotel, clase de vuelo, hospedaje, llegada y partida.
- `Usuario`: base para los usuarios que entran al sistema.

## Usuarios

Hay tres tipos de usuario:

- `Cliente`: puede consultar hoteles, vuelos y sus reservas.
- `Vendedor`: puede administrar clientes y reservas.
- `Administrador`: puede administrar todo.

Para cumplir herencia y polimorfismo, `Cliente`, `Vendedor` y `Administrador` heredan de `Usuario`. Cada uno responde distinto al metodo `tienePermiso`.

`UsuarioFactory` queda como ayuda para crear el usuario correcto cuando se carga desde archivo o cuando el administrador da de alta un usuario.

## Pantallas

- `VistaPrincipal`: menu inicial, consultas generales e inicio de sesion.
- `VistaCliente`: consultas del cliente.
- `VistaVendedor`: clientes, reservas y consultas.
- `VistaAdministrador`: sucursales, hoteles, vuelos, clientes, reservas y usuarios.

## Persistencia

Los datos estan en `TijeTravel/datos`:

- `sucursales.txt`
- `hoteles.txt`
- `vuelos.txt`
- `turistas.txt`
- `usuarios.txt`
- `reservas.txt`

Cada tipo de archivo tiene su clase en el paquete `persistencia`.

## Compilar y ejecutar

En PowerShell:

```powershell
javac -encoding UTF-8 -d TijeTravel\out (Get-ChildItem -Recurse TijeTravel\src -Filter *.java).FullName
java -cp TijeTravel\out tijetravel.Main
```

Desde Linux/macOS:

```bash
javac -encoding UTF-8 -d TijeTravel/out $(find TijeTravel/src -name "*.java")
java -cp TijeTravel/out tijetravel.Main
```
