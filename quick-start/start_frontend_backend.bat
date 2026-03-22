@echo off
chcp 65001 >nul
setlocal
title SwiftBoot Frontend + Backend Starter

echo ========================================
echo   SwiftBoot Frontend + Backend Starter
echo ========================================
echo.

set "ROOT=%~dp0.."
set "CONFIG_FILE=%~dp0start_config.ini"
set "REDIS_DIR="
set "DB_PASSWORD="
set "REDIS_PASSWORD="
set "DEEPSEEK_API_KEY="

call :load_config
call :apply_env

echo [1/3] Cleaning ports 8080 and 30328...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo Found process on port 8080 (PID %%a), killing...
        taskkill /F /PID %%a
    )
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":30328" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo Found process on port 30328 (PID %%a), killing...
        taskkill /F /PID %%a
    )
)
echo [OK] Ports cleaned.
timeout /t 1 /nobreak >nul

echo.
echo [2/3] Checking Redis...
if not "%REDIS_DIR%"=="" if exist "%REDIS_DIR%\redis-server.exe" (
  start "Redis Service" "%REDIS_DIR%\redis-server.exe"
  echo [OK] Redis startup command sent.
) else (
  echo [WARN] REDIS_DIR is not configured or redis-server.exe was not found.
)
timeout /t 2 /nobreak >nul

echo.
echo [3/3] Starting backend...
call mvn -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven was not found.
    pause
    exit /b 1
)
if defined SWIFTBOOT_DB_PASSWORD echo [OK] Loaded DB password from start_config.ini
if defined SWIFTBOOT_REDIS_PASSWORD echo [OK] Loaded Redis password from start_config.ini
if defined SWIFTBOOT_DEEPSEEK_API_KEY echo [OK] Loaded DeepSeek API key from start_config.ini
start "SwiftBoot Backend" cmd /k "cd /d "%ROOT%\swiftboot-backend" && echo Building dependencies and starting backend... && mvn clean install -DskipTests -pl swiftboot-admin -am && mvn -pl swiftboot-admin spring-boot:run"
echo [OK] Backend start command sent.

echo.
echo [3/3] Starting frontend...
call npm -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm was not found.
    pause
    exit /b 1
)
start "SwiftBoot Frontend" cmd /k "cd /d "%ROOT%\swiftboot-ui" && npm run dev"
echo [OK] Frontend start command sent.

echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:30328
echo.
pause >nul
exit /b 0

:load_config
if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="REDIS_DIR" set "REDIS_DIR=%%B"
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
