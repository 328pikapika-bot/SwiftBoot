@echo off
chcp 65001 > nul
title SwiftBoot Auto Release

:: Get commit message from parameter or use default
set COMMIT_MSG=%~1
if "%COMMIT_MSG%"=="" set COMMIT_MSG=[Auto Commit] SwiftBoot Update

echo ================================
echo SwiftBoot Auto Release (Gitee)
echo Commit: %COMMIT_MSG%
echo ================================

:: Switch to main and pull
git checkout main
git pull origin main

:: Add, commit and push
git add .
git commit -m "%COMMIT_MSG%"
git push origin main

if errorlevel 1 (
    echo Push failed!
    exit /b 1
)

echo.
echo ================================
echo Release Completed!
echo ================================
