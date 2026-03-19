@echo off
chcp 65001 >nul
title SwiftBoot Backend Starter

echo ========================================
echo   SwiftBoot Backend Independent Starter
echo   (Auto kill port 8080 + Rebuild + Run)
echo ========================================
echo.

cd /d "%~dp0.."
set ROOT=%cd%

echo [1/2] Checking and cleaning port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo Found old backend process (PID: %%a), killing...
        taskkill /F /PID %%a >nul
    )
)
echo [OK] Port cleaned.

echo.
echo [2/2] Starting backend service...
echo Launching Maven build and run in new window...

:: Check if path exists
if not exist "%ROOT%\swiftboot-backend" (
    echo [ERROR] Backend directory not found: "%ROOT%\swiftboot-backend"
    pause
    exit /b 1
)

start "SwiftBoot Backend" cmd /k "cd /d "%ROOT%\swiftboot-backend" && title SwiftBoot Backend (8080) && echo --------------------------------------------- && echo   Rebuilding and Starting Backend... && echo --------------------------------------------- && mvn clean install -DskipTests -pl swiftboot-admin -am && echo. && echo Starting Spring Boot... && mvn -pl swiftboot-admin spring-boot:run"

echo.
echo Startup command sent. Please check the popped up "SwiftBoot Backend" window.
echo.
echo Press any key to exit this launcher...
pause >nul
