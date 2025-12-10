@echo off
chcp 65001 >nul
echo ========================================
echo    数据库导出并上传到服务器工具
echo ========================================
echo.

:: ========== 配置区域 ==========
set DB_NAME=supermarket_db
set DB_USER=root
set SERVER_IP=8.136.43.180
set SERVER_USER=root
set SERVER_PATH=/root/
set BACKUP_FILE=supermarket_db_backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%

echo [配置信息]
echo 数据库名称: %DB_NAME%
echo MySQL用户: %DB_USER%
echo 服务器IP: %SERVER_IP%
echo 服务器用户: %SERVER_USER%
echo 备份文件名: %BACKUP_FILE%
echo.

:: ========== 步骤1：导出数据库 ==========
echo [步骤1] 正在导出数据库...
echo 设置UTF-8编码以避免编码问题...
chcp 65001 >nul
echo 请输入MySQL密码:
mysqldump -u %DB_USER% -p --default-character-set=utf8mb4 --set-charset --databases %DB_NAME% > %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ❌ 导出失败！请检查：
    echo    1. MySQL服务是否启动
    echo    2. 数据库名称是否正确
    echo    3. 用户名和密码是否正确
    echo    4. mysqldump命令是否在PATH中
    pause
    exit /b 1
)

if not exist %BACKUP_FILE% (
    echo ❌ 导出文件不存在！
    pause
    exit /b 1
)

echo ✅ 数据库导出成功: %BACKUP_FILE%
echo 文件大小:
dir %BACKUP_FILE% | findstr %BACKUP_FILE%
echo.

:: ========== 步骤2：上传到服务器 ==========
echo [步骤2] 正在上传文件到服务器...
echo 请输入服务器密码:
scp %BACKUP_FILE% %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%

if %errorlevel% neq 0 (
    echo.
    echo ❌ 上传失败！请检查：
    echo    1. 服务器SSH服务是否开启
    echo    2. 服务器IP和用户名是否正确
    echo    3. 网络连接是否正常
    echo    4. 防火墙是否开放22端口
    echo.
    echo 💡 提示：可以使用WinSCP等工具手动上传文件
    echo    文件位置: %cd%\%BACKUP_FILE%
    pause
    exit /b 1
)

echo ✅ 文件上传成功！
echo.

:: ========== 步骤3：提示导入命令 ==========
echo ========================================
echo ✅ 导出和上传完成！
echo ========================================
echo.
echo [下一步] 请在服务器上执行以下命令导入数据库：
echo.
echo     ssh %SERVER_USER%@%SERVER_IP%
echo     mysql -u root -p ^< %SERVER_PATH%%BACKUP_FILE%
echo.
echo 或者先创建数据库：
echo     mysql -u root -p
echo     CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
echo     exit;
echo     mysql -u root -p %DB_NAME% ^< %SERVER_PATH%%BACKUP_FILE%
echo.
echo ========================================
pause

