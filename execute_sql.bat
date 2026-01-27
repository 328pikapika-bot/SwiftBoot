@echo off
echo ========================================
echo SwiftBoot Database Initialization
echo ========================================
echo.
echo This batch file will execute the SQL script to initialize the database.
echo Make sure MySQL is installed and running.
echo.
echo Press any key to continue, or Ctrl+C to cancel...
pause > nul

echo.
echo Connecting to MySQL and executing script...
echo.

REM Check if mysql command exists
mysql --version > nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MySQL client not found!
    echo Please install MySQL or add MySQL bin directory to PATH
    echo.
    echo Alternative methods:
    echo 1. Use Navicat or other GUI tools
    echo 2. Use MySQL Workbench
    echo 3. Use phpMyAdmin if available
    echo.
    pause
    exit /b 1
)

REM Execute the SQL script
mysql -u root -p swiftboot < swiftboot-backend\sql\swiftboot.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS: Database initialized successfully!
    echo ========================================
    echo.
    echo Next steps:
    echo 1. Start the backend service
    echo 2. Test the application
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Failed to execute SQL script!
    echo ========================================
    echo.
    echo Please check:
    echo - MySQL service is running
    echo - Database 'swiftboot' exists
    echo - Username/password are correct
    echo.
)

pause