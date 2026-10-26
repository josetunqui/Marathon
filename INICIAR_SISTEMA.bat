@echo off
title Marathon Sports - Sistema de Conteo Ciclico Sede Atocongo
color 0C
echo ===============================================================================
echo       MARATHON SPORTS - SISTEMA DE CONTEO CICLICO (SEDE ATOCONGO)
echo       Curso Integrador I: Sistemas - Software (UTP 2026)
echo       Autores: Joseph Aaron Churampi (U21212777) - Jose Armando Tunqui (U23331844)
echo       Docente: Mg. Anita Condo Lopez
echo ===============================================================================
echo [1/3] Verificando entorno Java...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] No se encontro Java instalado en el sistema.
    echo Por favor instale JDK 17 o superior para ejecutar el backend.
    pause
    exit /b 1
)

echo [2/3] Iniciando Servidor Backend Spring Boot (Puerto 8080)...
if exist "%~dp0backend\target\inventario-atocongo-1.0.0.jar" (
    start "Backend Spring Boot - Marathon Sports" cmd /k "cd /d "%~dp0backend" && java -jar target\inventario-atocongo-1.0.0.jar"
) else (
    start "Backend Spring Boot - Marathon Sports" cmd /k "cd /d "%~dp0backend" && mvnw.cmd spring-boot:run"
)

echo [3/3] Esperando inicializacion de servicios (5 segundos)...
timeout /t 5 /nobreak >nul

echo [OK] Abriendo Sistema Web en su navegador predeterminado...
start "" "%~dp0index.html"

echo ===============================================================================
echo  El sistema esta en ejecucion:
echo  - Frontend Web: index.html
echo  - API Backend: http://localhost:8080/api/v1
echo  - Swagger UI: http://localhost:8080/swagger-ui/index.html
echo  - Modelos de Datos: modelos-datos.html
echo ===============================================================================
echo Para detener el sistema, cierre la ventana secundaria de Spring Boot.
pause
