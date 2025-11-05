@echo off
chcp 65001 >nul
echo ========================================
echo    超市进销存系统 - 构建生产版本
echo ========================================
echo.

echo [1/3] 检查依赖...
if not exist "node_modules\" (
    echo 未检测到依赖，正在安装...
    call npm install
    if errorlevel 1 (
        echo 依赖安装失败！
        pause
        exit /b 1
    )
) else (
    echo 依赖已存在
)

echo.
echo [2/3] 清理旧的构建文件...
if exist "dist\" (
    rmdir /s /q dist
    echo 已删除旧的 dist 目录
)

echo.
echo [3/3] 开始构建...
call npm run build

if errorlevel 1 (
    echo.
    echo ❌ 构建失败！
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 构建成功！
    echo.
    echo 构建文件位置: %~dp0dist
    echo.
    echo 下一步：
    echo 1. 将 dist 目录部署到 Nginx 或其他 Web 服务器
    echo 2. 或者使用 "启动开发服务器.bat" 进行开发调试
    echo.
)

pause



