#!/bin/bash

# ========================================
#    SQL文件编码修复工具
#    解决 "ERROR: ASCII '\0' appeared" 问题
# ========================================

if [ $# -eq 0 ]; then
    echo "用法: $0 <SQL文件路径>"
    echo "示例: $0 /tmp/supermarket_db_backup2.sql"
    exit 1
fi

SQL_FILE="$1"
OUTPUT_FILE="${SQL_FILE%.sql}_utf8.sql"

if [ ! -f "$SQL_FILE" ]; then
    echo "❌ 错误：文件不存在: $SQL_FILE"
    exit 1
fi

echo "========================================"
echo "    SQL文件编码修复工具"
echo "========================================"
echo ""
echo "原始文件: $SQL_FILE"
echo "输出文件: $OUTPUT_FILE"
echo ""

# 检查文件编码
echo "[步骤1] 检查文件编码..."
FILE_ENCODING=$(file -bi "$SQL_FILE" | cut -d= -f2)
echo "检测到的编码: $FILE_ENCODING"

# 检查BOM标记
echo ""
echo "[步骤2] 检查BOM标记..."
BOM_CHECK=$(head -c 3 "$SQL_FILE" | od -An -tx1 | tr -d ' \n')
echo "文件前3个字节: $BOM_CHECK"

if [[ "$BOM_CHECK" == *"fffe"* ]] || [[ "$BOM_CHECK" == *"feff"* ]]; then
    echo "⚠️  检测到UTF-16 BOM标记（这就是问题所在！）"
    ENCODING_TYPE="UTF-16"
elif [[ "$BOM_CHECK" == *"efbbbf"* ]]; then
    echo "检测到UTF-8 BOM标记"
    ENCODING_TYPE="UTF-8"
else
    echo "未检测到BOM标记，尝试自动识别..."
    ENCODING_TYPE="auto"
fi

echo ""
echo "[步骤3] 开始转换编码..."

# 尝试不同的转换方式
if command -v iconv &> /dev/null; then
    # 方法1：UTF-16 LE -> UTF-8
    if iconv -f UTF-16LE -t UTF-8 "$SQL_FILE" > "$OUTPUT_FILE" 2>/dev/null; then
        echo "✅ 成功：使用 UTF-16LE -> UTF-8 转换"
        SUCCESS=true
    # 方法2：UTF-16 BE -> UTF-8
    elif iconv -f UTF-16BE -t UTF-8 "$SQL_FILE" > "$OUTPUT_FILE" 2>/dev/null; then
        echo "✅ 成功：使用 UTF-16BE -> UTF-8 转换"
        SUCCESS=true
    # 方法3：自动检测编码
    elif iconv -f "$FILE_ENCODING" -t UTF-8 "$SQL_FILE" > "$OUTPUT_FILE" 2>/dev/null; then
        echo "✅ 成功：使用自动检测编码转换"
        SUCCESS=true
    else
        echo "❌ iconv转换失败，尝试其他方法..."
        SUCCESS=false
    fi
else
    echo "❌ 未找到 iconv 命令"
    echo "正在安装 iconv..."
    if command -v yum &> /dev/null; then
        yum install -y glibc-langpack-en
    elif command -v apt-get &> /dev/null; then
        apt-get update && apt-get install -y locales
    fi
    SUCCESS=false
fi

# 如果iconv失败，尝试使用sed移除BOM
if [ "$SUCCESS" != true ]; then
    echo ""
    echo "[步骤3-备选] 使用sed移除BOM并转换..."
    sed '1s/^\xEF\xBB\xBF//' "$SQL_FILE" | sed '1s/^\xFF\xFE//' | sed '1s/^\xFE\xFF//' > "$OUTPUT_FILE"
    
    # 检查输出文件
    if [ -f "$OUTPUT_FILE" ] && [ -s "$OUTPUT_FILE" ]; then
        echo "✅ 使用sed方法转换完成"
        SUCCESS=true
    else
        echo "❌ sed转换也失败"
    fi
fi

if [ "$SUCCESS" = true ]; then
    echo ""
    echo "[步骤4] 验证转换结果..."
    
    # 检查新文件的编码
    NEW_ENCODING=$(file -bi "$OUTPUT_FILE" | cut -d= -f2)
    echo "新文件编码: $NEW_ENCODING"
    
    # 检查文件大小
    ORIGINAL_SIZE=$(du -h "$SQL_FILE" | cut -f1)
    NEW_SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)
    echo "原始文件大小: $ORIGINAL_SIZE"
    echo "新文件大小: $NEW_SIZE"
    
    # 检查是否还有BOM
    NEW_BOM=$(head -c 3 "$OUTPUT_FILE" | od -An -tx1 | tr -d ' \n')
    if [[ "$NEW_BOM" == *"fffe"* ]] || [[ "$NEW_BOM" == *"feff"* ]]; then
        echo "⚠️  警告：新文件仍包含BOM标记"
    else
        echo "✅ 新文件已移除BOM标记"
    fi
    
    # 检查文件前几行是否正常
    echo ""
    echo "[步骤5] 预览文件内容（前5行）..."
    head -5 "$OUTPUT_FILE"
    
    echo ""
    echo "========================================"
    echo "✅ 转换完成！"
    echo "========================================"
    echo ""
    echo "原始文件: $SQL_FILE"
    echo "修复后文件: $OUTPUT_FILE"
    echo ""
    echo "现在可以使用以下命令导入："
    echo "  mysql -u root -p supermarket < $OUTPUT_FILE"
    echo ""
    echo "或者先创建数据库："
    echo "  mysql -u root -p"
    echo "  CREATE DATABASE IF NOT EXISTS supermarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "  exit;"
    echo "  mysql -u root -p supermarket < $OUTPUT_FILE"
    echo ""
else
    echo ""
    echo "❌ 转换失败！"
    echo ""
    echo "建议："
    echo "1. 在Windows上重新导出，使用以下命令："
    echo "   chcp 65001"
    echo "   mysqldump -u root -p --default-character-set=utf8mb4 --set-charset --databases supermarket_db > backup.sql"
    echo ""
    echo "2. 或者使用MySQL Workbench导出为UTF-8编码"
    echo ""
    exit 1
fi

