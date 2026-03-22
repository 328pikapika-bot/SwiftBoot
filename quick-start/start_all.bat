@echo off
setlocal

for %%I in ("%~dp0..") do set "ROOT=%%~fI"
set "CONFIG_FILE=%~dp0start_config.ini"
set "REDIS_DIR="
set "PYTHON_EXEC=python"
set "DB_PASSWORD="
set "REDIS_PASSWORD="
set "DEEPSEEK_API_KEY="

call :load_config
call :apply_env

if not "%REDIS_DIR%"=="" if exist "%REDIS_DIR%\redis-server.exe" (
  start "Redis" "%REDIS_DIR%\redis-server.exe"
) else (
  echo Redis not found or REDIS_DIR is not configured. Skipping Redis startup.
)

echo Using Python: %PYTHON_EXEC%
if defined SWIFTBOOT_DB_PASSWORD echo Loaded DB password from start_config.ini
if defined SWIFTBOOT_REDIS_PASSWORD echo Loaded Redis password from start_config.ini
if defined SWIFTBOOT_DEEPSEEK_API_KEY echo Loaded DeepSeek API key from start_config.ini

start "AI-Engine" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\ai-engine\"; & '%PYTHON_EXEC%' main.py"
start "AI-Watcher" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\ai-engine\"; & '%PYTHON_EXEC%' file_watcher.py"
start "Backend" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\swiftboot-backend\"; echo 'Building dependencies...'; mvn clean install -DskipTests -pl swiftboot-admin -am; if ($?) { echo 'Starting application...'; mvn -pl swiftboot-admin spring-boot:run }"
start "Frontend" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\swiftboot-ui\"; npm run dev"
exit /b 0

:load_config
if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="REDIS_DIR" set "REDIS_DIR=%%B"
    if /i "%%A"=="PYTHON_EXEC" set "PYTHON_EXEC=%%B"
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
