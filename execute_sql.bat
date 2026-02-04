@echo off
setlocal EnableDelayedExpansion

REM ============================================================================
REM  SwiftBoot 数据库一键初始化脚本
REM  功能：自动创建数据库并导入表结构和数据
REM ============================================================================

REM ----------------------------------------------------------------------------
REM  【配置区域】请根据您的本地环境修改以下配置
REM ----------------------------------------------------------------------------

REM 数据库主机地址 (默认: localhost)
set DB_HOST=localhost

REM 数据库端口 (默认: 3306)
set DB_PORT=3306

REM 数据库用户名 (默认: root)
set DB_USER=root

REM 数据库密码 (请在此处修改为您本地 MySQL 的 root 密码)
set DB_PASSWORD=root

REM SQL 文件路径 (相对于当前脚本的路径)
set SQL_FILE=swiftboot-backend\sql\swiftboot.sql

REM ----------------------------------------------------------------------------
REM  以下内容无需修改
REM ----------------------------------------------------------------------------

echo.
echo =======================================================
echo        SwiftBoot 数据库初始化工具
echo =======================================================
echo.
echo [当前配置]
echo   Host:     %DB_HOST%
echo   Port:     %DB_PORT%
echo   User:     %DB_USER%
echo   Password: %DB_PASSWORD%
echo   SQL File: %SQL_FILE%
echo.
echo [提示] 如果上述配置不正确，请用文本编辑器打开本文件(execute_sql.bat)修改【配置区域】。
echo.
pause

echo.
echo [1/2] 正在检查 MySQL 环境...
mysql --version > nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 mysql 命令！
    echo 请确保 MySQL 已安装并已添加到系统环境变量 PATH 中。
    pause
    exit /b 1
)
echo [成功] MySQL 环境正常。

echo.
echo [2/2] 正在连接数据库并执行 SQL 脚本...
echo (如果密码包含特殊字符可能导致连接失败，建议密码避免使用特殊字符或手动执行)
echo.

REM 注意：-p和密码之间不能有空格
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% < "%SQL_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo =======================================================
    echo  [成功] 数据库初始化完成！
    echo  已成功创建/重置 swiftboot 数据库。
    echo =======================================================
    echo.
) else (
    echo.
    echo =======================================================
    echo  [失败] SQL 执行出错！
    echo =======================================================
    echo 可能的原因：
    1. 密码错误 (请编辑本文件修改 DB_PASSWORD)
    2. MySQL 服务未启动
    3. 端口号不匹配
    echo.
)

pause
