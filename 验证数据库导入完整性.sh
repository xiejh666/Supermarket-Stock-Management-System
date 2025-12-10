#!/bin/bash

# ========================================
#    数据库导入完整性验证脚本
# ========================================

DB_NAME="supermarket"
DB_USER="root"

echo "========================================"
echo "    数据库导入完整性验证"
echo "========================================"
echo ""
echo "数据库名称: $DB_NAME"
echo "MySQL用户: $DB_USER"
echo ""

# 提示输入密码
read -sp "请输入MySQL密码: " MYSQL_PASSWORD
echo ""

# 测试连接
echo "[步骤1] 测试数据库连接..."
mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME;" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ 数据库连接失败！请检查："
    echo "   1. 数据库名称是否正确"
    echo "   2. 用户名和密码是否正确"
    echo "   3. MySQL服务是否运行"
    exit 1
fi

echo "✅ 数据库连接成功"
echo ""

# 检查数据库是否存在
echo "[步骤2] 检查数据库..."
DB_EXISTS=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep -c "$DB_NAME")

if [ $DB_EXISTS -eq 0 ]; then
    echo "❌ 数据库 $DB_NAME 不存在！"
    exit 1
fi

echo "✅ 数据库 $DB_NAME 存在"
echo ""

# 获取所有表
echo "[步骤3] 检查数据表..."
TABLES=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | tail -n +2)

if [ -z "$TABLES" ]; then
    echo "❌ 数据库中没有表！导入可能失败。"
    exit 1
fi

TABLE_COUNT=$(echo "$TABLES" | wc -l)
echo "✅ 找到 $TABLE_COUNT 个数据表"
echo ""
echo "数据表列表："
echo "$TABLES" | nl
echo ""

# 检查关键表是否存在
echo "[步骤4] 检查关键表..."
KEY_TABLES=("sys_user" "sys_role" "product" "category" "supplier" "purchase_order" "sale_order" "inventory" "message_notification" "system_config")

MISSING_TABLES=()
for table in "${KEY_TABLES[@]}"; do
    if echo "$TABLES" | grep -q "^${table}$"; then
        echo "  ✅ $table"
    else
        echo "  ❌ $table (缺失)"
        MISSING_TABLES+=("$table")
    fi
done

if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
    echo ""
    echo "⚠️  警告：以下关键表缺失："
    printf '  - %s\n' "${MISSING_TABLES[@]}"
fi

echo ""

# 检查数据量
echo "[步骤5] 检查数据量..."
echo ""

# 定义要检查的表和数据量阈值
declare -A TABLE_MIN_COUNTS=(
    ["sys_user"]=1
    ["sys_role"]=1
    ["product"]=0
    ["category"]=0
    ["supplier"]=0
    ["purchase_order"]=0
    ["sale_order"]=0
    ["inventory"]=0
)

TOTAL_RECORDS=0
EMPTY_TABLES=()

for table in "${KEY_TABLES[@]}"; do
    if echo "$TABLES" | grep -q "^${table}$"; then
        COUNT=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT COUNT(*) FROM $table;" 2>/dev/null | tail -1)
        
        if [ -z "$COUNT" ]; then
            COUNT=0
        fi
        
        TOTAL_RECORDS=$((TOTAL_RECORDS + COUNT))
        
        MIN_COUNT=${TABLE_MIN_COUNTS[$table]:-0}
        
        if [ "$COUNT" -eq 0 ] && [ "$MIN_COUNT" -gt 0 ]; then
            echo "  ⚠️  $table: $COUNT 条记录 (期望至少 $MIN_COUNT 条)"
            EMPTY_TABLES+=("$table")
        elif [ "$COUNT" -lt "$MIN_COUNT" ]; then
            echo "  ⚠️  $table: $COUNT 条记录 (期望至少 $MIN_COUNT 条)"
        else
            echo "  ✅ $table: $COUNT 条记录"
        fi
    fi
done

echo ""
echo "总记录数: $TOTAL_RECORDS"
echo ""

# 检查表结构
echo "[步骤6] 检查关键表结构..."
echo ""

KEY_TABLE="sys_user"
if echo "$TABLES" | grep -q "^${KEY_TABLE}$"; then
    echo "表 $KEY_TABLE 的结构："
    mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; DESCRIBE $KEY_TABLE;" 2>/dev/null
    echo ""
fi

# 检查数据完整性
echo "[步骤7] 检查数据完整性..."
echo ""

# 检查用户表数据
if echo "$TABLES" | grep -q "^sys_user$"; then
    echo "用户数据示例（前5条）："
    mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT id, username, real_name, role_id, status FROM sys_user LIMIT 5;" 2>/dev/null
    echo ""
fi

# 检查商品表数据
if echo "$TABLES" | grep -q "^product$"; then
    PRODUCT_COUNT=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT COUNT(*) FROM product;" 2>/dev/null | tail -1)
    if [ "$PRODUCT_COUNT" -gt 0 ]; then
        echo "商品数据示例（前3条）："
        mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT id, product_code, product_name, price, status FROM product LIMIT 3;" 2>/dev/null
        echo ""
    fi
fi

# 检查外键关系
echo "[步骤8] 检查外键关系..."
echo ""

# 检查用户和角色的关联
if echo "$TABLES" | grep -q "^sys_user$" && echo "$TABLES" | grep -q "^sys_role$"; then
    ORPHAN_USERS=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT COUNT(*) FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id WHERE r.id IS NULL AND u.role_id IS NOT NULL;" 2>/dev/null | tail -1)
    if [ "$ORPHAN_USERS" -gt 0 ]; then
        echo "  ⚠️  发现 $ORPHAN_USERS 个用户的角色关联无效"
    else
        echo "  ✅ 用户角色关联正常"
    fi
fi

# 检查商品和分类的关联
if echo "$TABLES" | grep -q "^product$" && echo "$TABLES" | grep -q "^category$"; then
    ORPHAN_PRODUCTS=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT COUNT(*) FROM product p LEFT JOIN category c ON p.category_id = c.id WHERE c.id IS NULL AND p.category_id IS NOT NULL;" 2>/dev/null | tail -1)
    if [ "$ORPHAN_PRODUCTS" -gt 0 ]; then
        echo "  ⚠️  发现 $ORPHAN_PRODUCTS 个商品的分类关联无效"
    else
        echo "  ✅ 商品分类关联正常"
    fi
fi

echo ""

# 检查字符集
echo "[步骤9] 检查数据库字符集..."
DB_CHARSET=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SELECT DEFAULT_CHARACTER_SET_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = '$DB_NAME';" 2>/dev/null | tail -1)

if [ "$DB_CHARSET" = "utf8mb4" ]; then
    echo "  ✅ 数据库字符集: $DB_CHARSET"
else
    echo "  ⚠️  数据库字符集: $DB_CHARSET (建议使用 utf8mb4)"
fi

echo ""

# 检查索引
echo "[步骤10] 检查关键表索引..."
KEY_TABLE="sys_user"
if echo "$TABLES" | grep -q "^${KEY_TABLE}$"; then
    INDEX_COUNT=$(mysql -u $DB_USER -p$MYSQL_PASSWORD -e "USE $DB_NAME; SHOW INDEX FROM $KEY_TABLE;" 2>/dev/null | wc -l)
    if [ "$INDEX_COUNT" -gt 1 ]; then
        echo "  ✅ 表 $KEY_TABLE 有 $((INDEX_COUNT - 1)) 个索引"
    else
        echo "  ⚠️  表 $KEY_TABLE 没有索引"
    fi
fi

echo ""

# 生成验证报告
echo "========================================"
echo "    验证报告"
echo "========================================"
echo ""
echo "数据库名称: $DB_NAME"
echo "数据表数量: $TABLE_COUNT"
echo "总记录数: $TOTAL_RECORDS"
echo "数据库字符集: $DB_CHARSET"
echo ""

if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
    echo "❌ 缺失的表: ${#MISSING_TABLES[@]} 个"
    printf '   - %s\n' "${MISSING_TABLES[@]}"
    echo ""
fi

if [ ${#EMPTY_TABLES[@]} -gt 0 ]; then
    echo "⚠️  空表: ${#EMPTY_TABLES[@]} 个"
    printf '   - %s\n' "${EMPTY_TABLES[@]}"
    echo ""
fi

if [ ${#MISSING_TABLES[@]} -eq 0 ] && [ ${#EMPTY_TABLES[@]} -eq 0 ]; then
    echo "✅ 数据库导入完整！所有关键表都存在且包含数据。"
else
    echo "⚠️  数据库导入可能不完整，请检查上述问题。"
fi

echo ""
echo "========================================"

# 提供详细查询命令
echo ""
echo "💡 详细检查命令："
echo ""
echo "# 查看所有表"
echo "mysql -u $DB_USER -p -e \"USE $DB_NAME; SHOW TABLES;\""
echo ""
echo "# 查看表结构"
echo "mysql -u $DB_USER -p -e \"USE $DB_NAME; DESCRIBE sys_user;\""
echo ""
echo "# 查看数据量"
echo "mysql -u $DB_USER -p -e \"USE $DB_NAME; SELECT 'sys_user' as table_name, COUNT(*) as count FROM sys_user UNION ALL SELECT 'product', COUNT(*) FROM product UNION ALL SELECT 'purchase_order', COUNT(*) FROM purchase_order;\""
echo ""

