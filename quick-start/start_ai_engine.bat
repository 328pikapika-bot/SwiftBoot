@echo off
chcp 65001 >nul
setlocal
title SwiftBoot AI Engine Starter

set "CONFIG_FILE=%~dp0start_config.ini"
set "PYTHON_BIN=python"

if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="PYTHON_EXEC" set "PYTHON_BIN=%%B"
  )
)

echo ========================================
echo   SwiftBoot AI Engine Starter
echo ========================================
echo.

cd /d "%~dp0..\ai-engine"

echo [1/3] Checking Python...
"%PYTHON_BIN%" --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python was not found: %PYTHON_BIN%
    pause
    exit /b 1
)
echo [OK] Python ready: %PYTHON_BIN%

echo.
echo [2/3] Starting vector watcher...
start "VectorDB Watcher" cmd /k ""%PYTHON_BIN%" file_watcher.py"
timeout /t 3 /nobreak >nul
echo [OK] Vector watcher started.

echo.
echo [3/3] Starting AI API...
start "AI Engine API" cmd /k ""%PYTHON_BIN%" main.py"
timeout /t 5 /nobreak >nul

echo.
netstat -ano | findstr ":8001" >nul
if errorlevel 1 (
    echo [WARN] AI engine may not have started correctly.
) else (
    echo [OK] AI engine is listening on port 8001.
)

echo.
pause >nul
