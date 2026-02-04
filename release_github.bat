@echo off
chcp 65001 > nul
title SwiftBoot GitHub Release Script
color 0E

echo ================================
echo SwiftBoot Release Script (GitHub)
echo ================================

:: Get current branch
for /f "tokens=*" %%i in ('git branch --show-current') do set BRANCH=%%i

if /I not "%BRANCH%"=="main" (
    echo Current branch is %BRANCH%, not main!
    echo Automatically switching to main branch...
    git checkout main
    if errorlevel 1 (
        echo Failed to switch branch, please check manually!
        pause
        exit /b
    )
    echo Pulling latest code from GitHub...
    git pull github main
) else (
    echo Current branch is main
    echo Pulling latest code from GitHub...
    git pull github main
)

echo.

:: Input commit message
set /p COMMIT_MSG=Input release_notes: 
if "%COMMIT_MSG%"=="" set COMMIT_MSG=[Auto Commit] SwiftBoot Release

echo.
echo Committing changes...
git add .
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo No changes to commit, skipping commit.
) else (
    echo Commit successful.
)

echo.
echo Pushing main branch to GitHub...
git push github main
if errorlevel 1 (
    echo Failed to push main branch!
    pause
    exit /b
)

echo.
:: Input version tag (Optional)
set /p TAG_NAME=Input tag (Press Enter to skip): 

if not "%TAG_NAME%"=="" (
    echo Creating Tag: %TAG_NAME%
    git tag -a %TAG_NAME% -m "%COMMIT_MSG%"

    echo Pushing Tag to GitHub...
    git push github %TAG_NAME%
    if errorlevel 1 (
        echo Failed to push Tag!
        pause
        exit /b
    )
    echo Tag push successful!
) else (
    echo No tag input, skipping tagging.
)

echo.
echo ================================
echo Release Completed!
echo Commit Message: %COMMIT_MSG%
if not "%TAG_NAME%"=="" echo Version Tag: %TAG_NAME%
echo ================================
pause
