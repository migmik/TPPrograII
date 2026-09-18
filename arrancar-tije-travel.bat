@echo off
setlocal EnableExtensions

echo ========================================
echo       TIJE TRAVEL - BUILD Y ARRANQUE
echo ========================================
echo.

set "ROOT=%~dp0"
set "BACK_DIR=%ROOT%tije-back"
set "FRONT_DIR=%ROOT%tije-front"
set "DB_HOST=localhost"
set "DB_PORT=3306"
if not defined BACK_PORT set "BACK_PORT=8080"

if not defined DB_URL set "DB_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/tijetravel"
if not defined DB_USER set "DB_USER=root"
if not defined DB_PASSWORD set "DB_PASSWORD="
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev"
if not defined BACKEND_URL set "BACKEND_URL=http://localhost:%BACK_PORT%"
if not defined FRONT_PORT set "FRONT_PORT=8081"

if not exist "%BACK_DIR%\mvnw.cmd" (
    echo ERROR: No se encontro tije-back\mvnw.cmd.
    pause
    exit /b 1
)

if not exist "%FRONT_DIR%\mvnw.cmd" (
    echo ERROR: No se encontro tije-front\mvnw.cmd.
    pause
    exit /b 1
)

echo [1/5] Comprobando MySQL en %DB_HOST%:%DB_PORT%...
powershell -NoProfile -Command "$client = New-Object System.Net.Sockets.TcpClient; try { $task = $client.ConnectAsync('%DB_HOST%', %DB_PORT%); if (-not $task.Wait(3000)) { exit 1 }; exit 0 } catch { exit 1 } finally { $client.Dispose() }"
if errorlevel 1 (
    echo.
    echo ERROR: MySQL no responde en %DB_HOST%:%DB_PORT%.
    echo Inicia el servicio MySQL y verifica que exista la base tijetravel.
    echo Puedes crearla ejecutando database\crear_base.sql.
    pause
    exit /b 1
)

echo OK: MySQL responde.
echo.
if "%BACK_PORT%"=="%FRONT_PORT%" (
    echo ERROR: BACK_PORT y FRONT_PORT deben ser distintos.
    pause
    exit /b 1
)
echo [2/5] Comprobando puertos de las aplicaciones...
powershell -NoProfile -Command "try { $listeners = [System.Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners(); $busy = @($listeners | Where-Object { $_.Port -in %BACK_PORT%,%FRONT_PORT% }); if ($busy.Count -gt 0) { $busy | ForEach-Object { Write-Output ('Puerto ocupado: ' + $_.Port) }; exit 1 }; exit 0 } catch { Write-Output $_.Exception.Message; exit 2 }"
if errorlevel 1 (
    echo.
    echo ERROR: Uno de los puertos necesarios ya esta ocupado.
    echo Backend: %BACK_PORT% - Frontend: %FRONT_PORT%
    echo Cierra Tomcat u otra aplicacion que use esos puertos y volve a ejecutar este archivo.
    echo Tambien podes cambiar BACK_PORT o FRONT_PORT antes de ejecutarlo.
    pause
    exit /b 1
)

echo OK: Los puertos estan disponibles.
echo.
echo [3/5] Compilando backend...
pushd "%BACK_DIR%"
call mvnw.cmd -DskipTests package
if errorlevel 1 (
    popd
    echo.
    echo ERROR: No se pudo compilar tije-back.
    pause
    exit /b 1
)
popd

echo.
echo [4/5] Compilando frontend...
pushd "%FRONT_DIR%"
call mvnw.cmd -DskipTests package
if errorlevel 1 (
    popd
    echo.
    echo ERROR: No se pudo compilar tije-front.
    pause
    exit /b 1
)
popd

echo.
echo [5/5] Iniciando backend y frontend...
start "TIJE BACK - http://localhost:%BACK_PORT%" /D "%BACK_DIR%" cmd /k mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=%BACK_PORT%"

powershell -NoProfile -Command "Start-Sleep -Seconds 5"

start "TIJE FRONT - http://localhost:%FRONT_PORT%" /D "%FRONT_DIR%" cmd /k mvnw.cmd spring-boot:run

echo.
echo ========================================
echo Aplicaciones iniciadas en ventanas separadas.
echo Backend:  http://localhost:%BACK_PORT%
echo Frontend: http://localhost:%FRONT_PORT%
echo Base:     %DB_URL%
echo Perfil:   %SPRING_PROFILES_ACTIVE%
echo.
echo Si el backend no arranca, revisa su ventana: ahi aparecera el error
echo exacto de credenciales, esquema, Flyway o migraciones.
echo Para detenerlas, presiona Ctrl+C en cada ventana.
echo ========================================
echo.
pause