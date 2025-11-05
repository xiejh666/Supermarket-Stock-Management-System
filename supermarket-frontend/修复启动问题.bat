@echo off
chcp 65001 >nul
echo ========================================
echo    修复 Vite 启动问题
echo ========================================
echo.

echo [1/4] 删除旧的依赖...
if exist "node_modules\" (
    echo 正在删除 node_modules...
    rmdir /s /q node_modules
)

if exist "package-lock.json" (
    echo 正在删除 package-lock.json...
    del /f /q package-lock.json
)

echo.
echo [2/4] 清理 npm 缓存...
call npm cache clean --force

echo.
echo [3/4] 重新安装依赖...
call npm install

if errorlevel 1 (
    echo.
    echo ❌ 依赖安装失败！
    echo.
    echo 可能的原因：
    echo 1. Node.js 版本过低（需要 18+）
    echo 2. 网络问题
    echo.
    echo 请尝试：
    echo - 升级 Node.js 到最新 LTS 版本
    echo - 使用国内镜像：npm config set registry https://registry.npmmirror.com
    echo.
    pause
    exit /b 1
)

echo.
echo [4/4] 启动开发服务器...
echo.

call npm run dev

pause



