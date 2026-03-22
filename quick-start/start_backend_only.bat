@echo off
chcp 65001 >nul
setlocal
title SwiftBoot Backend Starter

echo ========================================
echo   SwiftBoot Backend Starter
echo ========================================
echo.

cd /d "%~dp0.."
set "ROOT=%cd%"
set "CONFIG_FILE=%~dp0start_config.ini"
set "DB_PASSWORD="
set "REDIS_PASSWORD="
set "DEEPSEEK_API_KEY="

call :load_config
call :apply_env

echo [1/2] Cleaning port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo Found old backend process (PID %%a), killing...
        taskkill /F /PID %%a >nul
    )
)
echo [OK] Port cleaned.

echo.
echo [2/2] Starting backend service...
if not exist "%ROOT%\swiftboot-backend" (
    echo [ERROR] Backend directory not found: "%ROOT%\swiftboot-backend"
    pause
    exit /b 1
)

if defined SWIFTBOOT_DB_PASSWORD echo [OK] Loaded DB password from start_config.ini
if defined SWIFTBOOT_REDIS_PASSWORD echo [OK] Loaded Redis password from start_config.ini
if defined SWIFTBOOT_DEEPSEEK_API_KEY echo [OK] Loaded DeepSeek API key from start_config.ini

start "SwiftBoot Backend" cmd /k "cd /d "%ROOT%\swiftboot-backend" && title SwiftBoot Backend (8080) && echo --------------------------------------------- && echo   Rebuilding and Starting Backend... && echo --------------------------------------------- && mvn clean install -DskipTests -pl swiftboot-admin -am && echo. && echo Starting Spring Boot... && mvn -pl swiftboot-admin spring-boot:run"

echo.
echo Startup command sent. Check the new backend window.
echo.
pause >nul
exit /b 0

:load_config
if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="DB_PASSWORD" set "DB_PASSWORD=%%B"
    if /i "%%A"=="REDIS_PASSWORD" set "REDIS_PASSWORD=%%B"
    if /i "%%A"=="DEEPSEEK_API_KEY" set "DEEPSEEK_API_KEY=%%B"
  )
)
exit /b 0

:apply_env
if defined DB_PASSWORD set "SWIFTBOOT_DB_PASSWORD=%DB_PASSWORD%"
if defined REDIS_PASSWORD set "SWIFTBOOT_REDIS_PASSWORD=%REDIS_PASSWORD%"
if defined DEEPSEEK_API_KEY set "SWIFTBOOT_DEEPSEEK_API_KEY=%DEEPSEEK_API_KEY%"
exit /b 0
