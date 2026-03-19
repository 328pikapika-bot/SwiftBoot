@echo off
chcp 65001 >nul
title SwiftBoot 前后端一键启动器

echo ========================================
echo   SwiftBoot 前后端启动器
echo ========================================
echo.

set ROOT=%~dp0..
set CONFIG_FILE=%~dp0start_config.ini
set REDIS_DIR=

:: 读取配置文件获取 Redis 路径
if exist "%CONFIG_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%CONFIG_FILE%") do (
    if /i "%%A"=="REDIS_DIR" set REDIS_DIR=%%B
  )
)

echo [1/3] 正在清理可能残留的进程 (8080端口, 30328端口)...
:: 查找并结束占用 8080 端口的进程 (后端)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo 发现端口 8080 被进程 %%a 占用，正在尝试结束...
        taskkill /F /PID %%a
    )
)
:: 查找并结束占用 30328 端口的进程 (前端)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":30328" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        echo 发现端口 30328 被进程 %%a 占用，正在尝试结束...
        taskkill /F /PID %%a
    )
)
echo [OK] 端口清理完成。
timeout /t 1 /nobreak >nul

echo.
echo [2/3] 正在检查和启动 Redis 服务...
if not "%REDIS_DIR%"=="" if exist "%REDIS_DIR%\redis-server.exe" (
  start "Redis Service" "%REDIS_DIR%\redis-server.exe"
  echo [OK] Redis 启动指令已发送。
) else (
  echo [警告] 未找到 Redis 或 start_config.ini 中未配置 REDIS_DIR。
  echo [警告] 如果 Redis 已经作为 Windows 服务运行，可忽略此警告。
)
timeout /t 2 /nobreak >nul

echo.
echo [3/3] 正在启动 SwiftBoot Java 后端...
:: 检查 maven
call mvn -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Maven，请确保 mvn 命令可用。
    pause
    exit /b 1
)
start "SwiftBoot Backend" cmd /k "cd /d "%ROOT%\swiftboot-backend" && echo 正在构建依赖并启动后端... && mvn clean install -DskipTests -pl swiftboot-admin -am && mvn -pl swiftboot-admin spring-boot:run"
echo [OK] 后端启动进程已在独立窗口中运行。

echo.
echo [3/3] 正在启动 SwiftBoot Vue 前端...
:: 检查 npm
call npm -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 npm，请确保已安装 Node.js。
    pause
    exit /b 1
)
start "SwiftBoot Frontend" cmd /k "cd /d "%ROOT%\swiftboot-ui" && npm run dev"
echo [OK] 前端启动进程已在独立窗口中运行。

echo.
echo ========================================
echo   全部启动指令已发送！
echo ========================================
echo.
echo 后端将运行在: http://localhost:8080
echo 前端将运行在: http://localhost:30328 (请以弹出的前端控制台实际端口为准)
echo.
echo 按任意键退出当前启动器窗口...
pause >nul
