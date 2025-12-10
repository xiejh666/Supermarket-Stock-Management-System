@echo off
chcp 65001
echo ====================================
echo 超市库存管理系统 - 数据库结构导出工具
echo ====================================
echo.

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=supermarket_db
set DB_USER=root
set DB_PASS=root
set OUTPUT_FILE=supermarket_db_structure_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
set OUTPUT_FILE=%OUTPUT_FILE: =0%

echo 正在导出数据库结构...
echo 数据库: %DB_NAME%
echo 输出文件: %OUTPUT_FILE%
echo.

mysqldump -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --no-data --skip-add-drop-table --skip-comments %DB_NAME% > %OUTPUT_FILE%

if %errorlevel% equ 0 (
    echo.
    echo ====================================
    echo 导出成功！
    echo 文件位置: %cd%\%OUTPUT_FILE%
    echo ====================================
) else (
    echo.
    echo ====================================
    echo 导出失败！请检查：
    echo 1. MySQL是否已安装并配置环境变量
    echo 2. 数据库连接信息是否正确
    echo 3. 数据库是否存在
    echo ====================================
)

echo.
pause
