@echo off
chcp 65001 >nul
echo ========================================
echo    超市进销存系统 - 前端开发服务器
echo ========================================
echo.

echo [1/2] 检查依赖...
if not exist "node_modules\" (
    echo 未检测到依赖，正在安装...
    call npm install
    if errorlevel 1 (
        echo 依赖安装失败！
        pause
        exit /b 1
    )
) else (
    echo 依赖已存在，跳过安装
)

echo.
echo [2/2] 启动开发服务器...
echo.
echo 服务器将在 http://8.136.43.180:5173 启动
echo 按 Ctrl+C 可停止服务器
echo.

call npm run dev

pause



