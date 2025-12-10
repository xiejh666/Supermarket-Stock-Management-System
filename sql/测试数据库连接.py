#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试数据库连接
"""

print("正在测试数据库连接...")

# 测试1: 检查是否安装了mysql-connector-python
print("\n测试1: 检查MySQL连接库...")
try:
    import mysql.connector
    print("✅ mysql-connector-python 已安装")
except ImportError:
    print("❌ 未安装 mysql-connector-python")
    print("请运行: pip install mysql-connector-python")
    exit(1)

# 测试2: 尝试连接数据库
print("\n测试2: 连接数据库...")
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',  # 请修改为你的数据库密码
    'database': 'supermarket_db',
    'charset': 'utf8mb4'
}

try:
    conn = mysql.connector.connect(**DB_CONFIG)
    print("✅ 数据库连接成功")
    
    # 测试3: 执行简单查询
    print("\n测试3: 执行简单查询...")
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM product")
    count = cursor.fetchone()[0]
    print(f"✅ 查询成功，商品总数: {count}")
    
    # 测试4: 检查表是否存在
    print("\n测试4: 检查必要的表...")
    tables = ['product', 'inventory', 'purchase_order', 'purchase_order_item', 
              'sale_order', 'sale_order_item']
    for table in tables:
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"  {table}: {count} 条记录")
    
    cursor.close()
    conn.close()
    
    print("\n✅ 所有测试通过！可以运行修正脚本了。")
    
except mysql.connector.Error as e:
    print(f"❌ 数据库错误: {e}")
    print("\n可能的原因:")
    print("1. 数据库密码错误（请修改脚本中的 password）")
    print("2. 数据库服务未启动")
    print("3. 数据库名称错误")
except Exception as e:
    print(f"❌ 发生错误: {e}")
