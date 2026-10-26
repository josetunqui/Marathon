@echo off
title Ejecutar Bateria de Pruebas Automatizadas - Marathon Atocongo
color 0A
echo ===============================================================================
echo       MARATHON SPORTS - EJECUCION DE PRUEBAS AUTOMATIZADAS
echo       Curso Integrador I: Sistemas - Software (UTP 2026)
echo       17 Tests Unitarios y de Integracion con JUnit 5 y Mockito
echo ===============================================================================
cd /d "%~dp0backend"
call mvnw.cmd test
echo ===============================================================================
echo  Pruebas finalizadas. Revise los resultados arriba.
echo ===============================================================================
pause
