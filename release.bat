@echo off
title SwiftBoot Gitee 发布脚本（增强版 - 可选 Tag）
color 0A

echo ================================
echo SwiftBoot 发布脚本（Gitee）
echo ================================

:: 获取当前分支
for /f "tokens=*" %%i in ('git branch --show-current') do set BRANCH=%%i

if /I not "%BRANCH%"=="main" (
    echo 当前分支是 %BRANCH%，不是 main 分支！
    echo 自动切换到 main 分支...
    git checkout main
    if errorlevel 1 (
        echo 切换分支失败，请手动检查！
        pause
        exit /b
    )
    git pull origin main
) else (
    echo 当前分支是 main
    git pull origin main
)

echo.

:: 输入提交描述（可留空使用默认）
set /p COMMIT_MSG=请输入提交描述（可留空使用默认）：
if "%COMMIT_MSG%"=="" set COMMIT_MSG=[Auto Commit] SwiftBoot 发布

echo.
echo 正在提交代码...
git add .
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo 没有变更可提交，跳过 commit
) else (
    echo 提交完成
)

echo.
echo 正在推送 main 分支到 Gitee...
git push origin main
if errorlevel 1 (
    echo 推送 main 分支失败！
    pause
    exit /b
)

echo.
:: 输入版本号（可选）
set /p TAG_NAME=请输入版本号（可留空则不打 Tag）：

if not "%TAG_NAME%"=="" (
    echo 正在创建 Tag：%TAG_NAME%
    git tag -a %TAG_NAME% -m "%COMMIT_MSG%"

    echo 正在推送 Tag 到 Gitee...
    git push origin %TAG_NAME%
    if errorlevel 1 (
        echo 推送 Tag 失败！
        pause
        exit /b
    )
    echo Tag 推送完成！
) else (
    echo 没有输入版本号，跳过打 Tag
)

echo.
echo ================================
echo 发布完成！
echo 提交描述: %COMMIT_MSG%
if not "%TAG_NAME%"=="" echo 版本号: %TAG_NAME%
echo ================================
pause
