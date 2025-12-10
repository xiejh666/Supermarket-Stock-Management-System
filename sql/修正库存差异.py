#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修正库存差异
确保：当前库存 = 采购 - 销售，差异控制在20以内
"""

import mysql.connector
from datetime import datetime, timedelta
import random

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',  # 请修改为你的数据库密码
    'database': 'supermarket_db',
    'charset': 'utf8mb4'
}

def get_inventory_validation():
    """获取库存验证数据"""
    print("   正在连接数据库...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("   ✅ 数据库连接成功")
    except Exception as e:
        print(f"   ❌ 数据库连接失败: {e}")
        raise
    
    cursor = conn.cursor(dictionary=True)
    print("   正在查询库存数据...")
    
    query = """
    SELECT 
        p.id AS product_id,
        p.product_code,
        p.product_name,
        COALESCE(purchase.total_purchase, 0) AS total_purchase,
        COALESCE(sales.total_sale, 0) AS total_sale,
        COALESCE(purchase.total_purchase, 0) - COALESCE(sales.total_sale, 0) AS calculated_stock,
        i.quantity AS current_stock,
        ABS(COALESCE(purchase.total_purchase, 0) - COALESCE(sales.total_sale, 0) - COALESCE(i.quantity, 0)) AS difference
    FROM product p
    LEFT JOIN (
        SELECT poi.product_id, SUM(poi.quantity) as total_purchase
        FROM purchase_order_item poi
        JOIN purchase_order po ON poi.order_id = po.id
        WHERE po.status = 3
        GROUP BY poi.product_id
    ) purchase ON p.id = purchase.product_id
    LEFT JOIN (
        SELECT soi.product_id, SUM(soi.quantity) as total_sale
        FROM sale_order_item soi
        JOIN sale_order so ON soi.order_id = so.id
        WHERE so.status = 1
        GROUP BY soi.product_id
    ) sales ON p.id = sales.product_id
    LEFT JOIN inventory i ON p.id = i.product_id
    HAVING difference > 20
    ORDER BY difference DESC
    """
    
    try:
        cursor.execute(query)
        print("   查询执行完成，正在获取结果...")
        results = cursor.fetchall()
        print(f"   ✅ 获取到 {len(results)} 条记录")
    except Exception as e:
        print(f"   ❌ 查询执行失败: {e}")
        cursor.close()
        conn.close()
        raise
    
    cursor.close()
    conn.close()
    
    return results

def get_product_cost_price(product_id):
    """获取商品成本价"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("SELECT cost_price FROM product WHERE id = %s", (product_id,))
    result = cursor.fetchone()
    
    cursor.close()
    conn.close()
    
    return result['cost_price'] if result else 0

def get_supplier_id(product_id):
    """获取商品的供应商ID"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("SELECT supplier_id FROM product WHERE id = %s", (product_id,))
    result = cursor.fetchone()
    
    cursor.close()
    conn.close()
    
    return result['supplier_id'] if result else 1

def generate_adjustment_sql(problems):
    """生成调整SQL"""
    sql_lines = []
    sql_lines.append("-- =============================================")
    sql_lines.append("-- 库存差异修正脚本")
    sql_lines.append(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    sql_lines.append(f"-- 需要调整的商品数: {len(problems)}")
    sql_lines.append("-- =============================================\n")
    
    order_id_offset = 10000  # 使用较大的订单ID避免冲突
    
    for idx, item in enumerate(problems, 1):
        product_id = item['product_id']
        product_name = item['product_name']
        total_purchase = item['total_purchase']
        total_sale = item['total_sale']
        current_stock = item['current_stock']
        calculated_stock = item['calculated_stock']
        difference = item['difference']
        
        sql_lines.append(f"-- 商品{idx}: {product_name} (ID: {product_id})")
        sql_lines.append(f"-- 当前: 采购={total_purchase}, 销售={total_sale}, 库存={current_stock}")
        sql_lines.append(f"-- 计算库存={calculated_stock}, 差异={difference}")
        
        # 目标：让 采购 - 销售 = 库存，且差异在20以内
        # 策略：调整采购量
        target_purchase = current_stock + total_sale + random.randint(5, 15)  # 增加5-15的安全库存
        adjustment = target_purchase - total_purchase
        
        if adjustment > 0:
            # 需要增加采购
            sql_lines.append(f"-- 调整方案: 增加采购 {adjustment} 件")
            
            # 获取商品信息
            cost_price = get_product_cost_price(product_id)
            supplier_id = get_supplier_id(product_id)
            
            # 生成新的采购订单
            order_no = f"PO_ADJ_{datetime.now().strftime('%Y%m%d')}_{product_id:04d}"
            create_time = (datetime.now() - timedelta(days=random.randint(1, 30))).strftime('%Y-%m-%d %H:%M:%S')
            audit_time = (datetime.strptime(create_time, '%Y-%m-%d %H:%M:%S') + timedelta(hours=2)).strftime('%Y-%m-%d %H:%M:%S')
            inbound_time = (datetime.strptime(audit_time, '%Y-%m-%d %H:%M:%S') + timedelta(hours=6)).strftime('%Y-%m-%d %H:%M:%S')
            total_amount = round(cost_price * adjustment, 2)
            
            # 插入采购订单
            sql_lines.append(f"INSERT INTO purchase_order (order_no, supplier_id, total_amount, status, applicant_id, auditor_id, create_time, audit_time, inbound_time)")
            sql_lines.append(f"VALUES ('{order_no}', {supplier_id}, {total_amount}, 3, 2, 1, '{create_time}', '{audit_time}', '{inbound_time}');")
            
            # 插入采购明细
            sql_lines.append(f"INSERT INTO purchase_order_item (order_id, product_id, quantity, unit_price, total_price)")
            sql_lines.append(f"SELECT LAST_INSERT_ID(), {product_id}, {adjustment}, {cost_price}, {total_amount};")
            
        elif adjustment < 0:
            # 需要减少采购（通过增加销售来平衡）
            # 但题目要求只允许采购大于销售，所以我们增加采购而不是增加销售
            target_purchase = current_stock + total_sale + random.randint(5, 15)
            adjustment = target_purchase - total_purchase
            
            sql_lines.append(f"-- 调整方案: 增加采购 {adjustment} 件（确保采购>销售）")
            
            cost_price = get_product_cost_price(product_id)
            supplier_id = get_supplier_id(product_id)
            
            order_no = f"PO_ADJ_{datetime.now().strftime('%Y%m%d')}_{product_id:04d}"
            create_time = (datetime.now() - timedelta(days=random.randint(1, 30))).strftime('%Y-%m-%d %H:%M:%S')
            audit_time = (datetime.strptime(create_time, '%Y-%m-%d %H:%M:%S') + timedelta(hours=2)).strftime('%Y-%m-%d %H:%M:%S')
            inbound_time = (datetime.strptime(audit_time, '%Y-%m-%d %H:%M:%S') + timedelta(hours=6)).strftime('%Y-%m-%d %H:%M:%S')
            total_amount = round(cost_price * adjustment, 2)
            
            sql_lines.append(f"INSERT INTO purchase_order (order_no, supplier_id, total_amount, status, applicant_id, auditor_id, create_time, audit_time, inbound_time)")
            sql_lines.append(f"VALUES ('{order_no}', {supplier_id}, {total_amount}, 3, 2, 1, '{create_time}', '{audit_time}', '{inbound_time}');")
            
            sql_lines.append(f"INSERT INTO purchase_order_item (order_id, product_id, quantity, unit_price, total_price)")
            sql_lines.append(f"SELECT LAST_INSERT_ID(), {product_id}, {adjustment}, {cost_price}, {total_amount};")
        
        sql_lines.append(f"-- 调整后: 采购={target_purchase}, 销售={total_sale}, 库存={current_stock}, 差异={target_purchase - total_sale - current_stock}\n")
    
    sql_lines.append("\n-- =============================================")
    sql_lines.append("-- 调整完成")
    sql_lines.append("-- 请执行验证SQL确认差异已减少到20以内")
    sql_lines.append("-- =============================================")
    
    return "\n".join(sql_lines)

def main():
    print("=" * 60)
    print("🔧 库存差异修正工具")
    print("=" * 60)
    
    print("\n📊 步骤1: 检查库存差异...")
    problems = get_inventory_validation()
    
    if not problems:
        print("   ✅ 所有商品库存准确，无需调整！")
        return
    
    print(f"   ⚠️  发现 {len(problems)} 个商品库存差异超过20")
    print("\n前10个差异最大的商品:")
    for i, item in enumerate(problems[:10], 1):
        print(f"   {i}. {item['product_name']}: 差异={item['difference']}, "
              f"采购={item['total_purchase']}, 销售={item['total_sale']}, 库存={item['current_stock']}")
    
    print("\n📊 步骤2: 生成调整SQL...")
    sql_content = generate_adjustment_sql(problems)
    
    # 保存到文件
    output_file = "库存差异修正.sql"
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql_content)
    
    print(f"   ✅ SQL脚本已生成: {output_file}")
    print(f"\n💡 使用方法:")
    print(f"   1. 检查生成的SQL文件")
    print(f"   2. 执行: mysql -u root -p supermarket_db < {output_file}")
    print(f"   3. 再次运行验证SQL确认差异已减少")
    print(f"\n⚠️  注意: 此脚本会添加新的采购订单来调整库存")

if __name__ == "__main__":
    try:
        main()
    except mysql.connector.Error as e:
        print(f"\n❌ 数据库连接错误: {e}")
        print("   请检查数据库配置（host, user, password, database）")
    except Exception as e:
        print(f"\n❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()
