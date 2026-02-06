@echo off
for %%I in ("%~dp0..") do set ROOT=%%~fI
set CONFIG_FILE=%~dp0start_config.ini
set REDIS_DIR=
set PYTHON_EXEC=python

if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="REDIS_DIR" set REDIS_DIR=%%B
    if /i "%%A"=="PYTHON_EXEC" set PYTHON_EXEC=%%B
  )
)

if not "%REDIS_DIR%"=="" if exist "%REDIS_DIR%\redis-server.exe" (
  start "Redis" "%REDIS_DIR%\redis-server.exe"
) else (
  echo Redis 未找到或未配置，跳过启动 Redis
)

echo Using Python: %PYTHON_EXEC%

start "AI-Engine" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\ai-engine\"; & '%PYTHON_EXEC%' main.py"
start "AI-Watcher" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\ai-engine\"; & '%PYTHON_EXEC%' file_watcher.py"
start "Backend" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\swiftboot-backend\"; echo 'Building dependencies...'; mvn clean install -DskipTests -pl swiftboot-admin -am; if ($?) { echo 'Starting application...'; mvn -pl swiftboot-admin spring-boot:run }"
start "Frontend" powershell -NoExit -Command "Set-Location -Path \"%ROOT%\swiftboot-ui\"; npm run dev"
