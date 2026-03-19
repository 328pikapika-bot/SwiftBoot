@echo off
chcp 65001 >nul
title SwiftBoot AI Engine 启动器

:: 尝试自动检测 ai_agent conda 环境的 Python
set PYTHON_BIN=python
for /f "tokens=*" %%i in ('conda run -n ai_agent where python 2^>nul') do (
    set PYTHON_BIN=%%i
    goto :found_python
)

:found_python
:: 如果通过 conda 没找到，检查默认路径
if "%PYTHON_BIN%"=="python" (
    if exist "D:\tools\miniconda3\envs\ai_agent\python.exe" (
        set PYTHON_BIN=D:\tools\miniconda3\envs\ai_agent\python.exe
    )
)

echo ========================================
echo   SwiftBoot AI 引擎启动器
echo ========================================
echo.

cd /d "%~dp0..\ai-engine"

echo [1/3] 检查 Python 环境...
"%PYTHON_BIN%" --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python 环境: %PYTHON_BIN%
    echo 请确保已安装并激活了包含必要依赖的 Python 环境。
    pause
    exit /b 1
)
echo [OK] Python 已就绪: %PYTHON_BIN%

echo.
echo [2/3] 启动向量数据库监听服务...
start "VectorDB Watcher" cmd /k ""%PYTHON_BIN%" file_watcher.py"
timeout /t 3 /nobreak >nul
echo [OK] 向量数据库监听服务已启动

echo.
echo [3/3] 启动 AI 知识检索引擎 API...
start "AI Engine API" cmd /k ""%PYTHON_BIN%" main.py"
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 等待服务启动...
timeout /t 5 /nobreak >nul

echo 检查服务状态...
netstat -ano | findstr ":8001" >nul
if errorlevel 1 (
    echo [警告] AI 引擎可能未成功启动，请检查
) else (
    echo [OK] AI 引擎已启动 (端口 8001)
)

echo.
echo 按任意键打开浏览器访问首页...
pause >nul
start http://localhost:30328

echo.
echo 服务已在后台运行
echo - 向量数据库监听: file_watcher.py
echo - AI 知识检索引擎: main.py (端口 8001)
echo.
echo 按任意键退出...
pause >nul
