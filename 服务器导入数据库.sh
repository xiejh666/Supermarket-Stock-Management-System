#!/bin/bash

# ========================================
#    服务器端数据库导入脚本
# ========================================

# 配置区域
DB_NAME="supermarket_db"
DB_USER="root"
BACKUP_FILE="supermarket_db_backup_*.sql"
BACKUP_PATH="/root/"

echo "========================================"
echo "    数据库导入工具"
echo "========================================"
echo ""
echo "[配置信息]"
echo "数据库名称: $DB_NAME"
echo "MySQL用户: $DB_USER"
echo "备份文件路径: $BACKUP_PATH"
echo ""

# 检查备份文件是否存在
if [ ! -f ${BACKUP_PATH}${BACKUP_FILE} ]; then
    echo "❌ 错误：找不到备份文件！"
    echo "请确认文件路径: ${BACKUP_PATH}${BACKUP_FILE}"
    exit 1
fi

# 查找最新的备份文件
LATEST_BACKUP=$(ls -t ${BACKUP_PATH}supermarket_db_backup_*.sql 2>/dev/null | head -1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "❌ 错误：找不到备份文件！"
    exit 1
fi

echo "找到备份文件: $LATEST_BACKUP"
echo "文件大小: $(du -h $LATEST_BACKUP | cut -f1)"
echo ""

# 询问是否继续
read -p "是否继续导入？(y/n): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消导入"
    exit 0
fi

echo ""
echo "[步骤1] 检查MySQL服务状态..."
if ! systemctl is-active --quiet mysql && ! systemctl is-active --quiet mysqld; then
    echo "⚠️  警告：MySQL服务可能未运行"
    read -p "是否尝试启动MySQL服务？(y/n): " start_mysql
    if [ "$start_mysql" = "y" ] || [ "$start_mysql" = "Y" ]; then
        systemctl start mysql 2>/dev/null || systemctl start mysqld 2>/dev/null
        sleep 2
    fi
fi

echo ""
echo "[步骤2] 创建数据库（如果不存在）..."
mysql -u $DB_USER -p <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES LIKE '$DB_NAME';
EOF

if [ $? -ne 0 ]; then
    echo "❌ 创建数据库失败！"
    exit 1
fi

echo "✅ 数据库准备完成"
echo ""

# 询问是否备份现有数据
read -p "是否备份现有数据库？(y/n): " backup_existing
if [ "$backup_existing" = "y" ] || [ "$backup_existing" = "Y" ]; then
    EXISTING_BACKUP="${BACKUP_PATH}${DB_NAME}_existing_backup_$(date +%Y%m%d_%H%M%S).sql"
    echo "正在备份现有数据库到: $EXISTING_BACKUP"
    mysqldump -u $DB_USER -p --default-character-set=utf8mb4 --databases $DB_NAME > $EXISTING_BACKUP
    if [ $? -eq 0 ]; then
        echo "✅ 现有数据库已备份"
    else
        echo "⚠️  备份失败，但继续导入..."
    fi
    echo ""
fi

echo "[步骤3] 开始导入数据库..."
echo "请输入MySQL密码:"
mysql -u $DB_USER -p --default-character-set=utf8mb4 $DB_NAME < $LATEST_BACKUP

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 导入失败！"
    echo "请检查："
    echo "  1. MySQL密码是否正确"
    echo "  2. 数据库用户权限是否足够"
    echo "  3. SQL文件是否完整"
    exit 1
fi

echo ""
echo "✅ 数据库导入成功！"
echo ""

# 验证导入结果
echo "[步骤4] 验证导入结果..."
TABLE_COUNT=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | wc -l)

if [ -z "$MYSQL_PASSWORD" ]; then
    echo "请输入MySQL密码以验证导入结果:"
    read -s MYSQL_PASSWORD
fi

TABLE_COUNT=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | tail -n +2 | wc -l)

if [ "$TABLE_COUNT" -gt 0 ]; then
    echo "✅ 找到 $TABLE_COUNT 个数据表"
    echo ""
    echo "主要数据表："
    mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | head -10
    echo ""
    
    # 检查关键表的数据量
    echo "数据统计："
    mysql -u $DB_USER -p$MYSQL_PASSWORD -e "
    USE $DB_NAME;
    SELECT 'sys_user' as table_name, COUNT(*) as count FROM sys_user
    UNION ALL
    SELECT 'product', COUNT(*) FROM product
    UNION ALL
    SELECT 'purchase_order', COUNT(*) FROM purchase_order
    UNION ALL
    SELECT 'sale_order', COUNT(*) FROM sale_order
    UNION ALL
    SELECT 'inventory', COUNT(*) FROM inventory;
    " 2>/dev/null
else
    echo "⚠️  警告：未找到数据表，请检查导入是否成功"
fi

echo ""
echo "========================================"
echo "✅ 导入完成！"
echo "========================================"

