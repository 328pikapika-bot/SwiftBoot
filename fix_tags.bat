@echo off
chcp 65001 > nul
title Fix Git Tags Strict

echo ========================================================
echo Fix Tag Descriptions and Cleanup (Strict Mode)
echo ========================================================
echo.

echo [1/4] Deleting '迭代v0.1.2'...
git tag -d 迭代v0.1.2 >nul 2>&1
git push origin :refs/tags/迭代v0.1.2 >nul 2>&1
echo Done.
echo.

echo [2/4] Fixing v0.1.1...
git tag -d v0.1.1 >nul 2>&1
git push origin :refs/tags/v0.1.1 >nul 2>&1
git tag -a v0.1.1 -m "修复权限配置与菜单展示的核心链路问题，提升系统的稳定性与可用性。" 5ed370761b014b3df64b4bf42bd968687b74b976
git push origin v0.1.1
echo Done.
echo.

echo [3/4] Fixing v0.1.2...
git tag -d v0.1.2 >nul 2>&1
git push origin :refs/tags/v0.1.2 >nul 2>&1
git tag -a v0.1.2 -m "实现了字典系统的深度集成与代码生成的智能化绑定，极大降低了前后端数据一致性的维护成本。" 625cd6e15278860a3af1866196c710b60e65374b
git push origin v0.1.2
echo Done.
echo.

echo [4/4] Fixing v0.1.3...
git tag -d v0.1.3 >nul 2>&1
git push origin :refs/tags/v0.1.3 >nul 2>&1
git tag -a v0.1.3 -m "接入了智能问答助手，将项目知识转化为随时调用的 AI 导师，解决了新人上手难与文档查找繁琐的问题。" 2c9282e45c9de3f2b0afa68528de8617a3e7145b
git push origin v0.1.3
echo Done.
echo.

echo ========================================================
echo All Operations Completed!
echo Please refresh your Gitee page.
echo ========================================================
pause
