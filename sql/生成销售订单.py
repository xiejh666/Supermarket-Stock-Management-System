#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成超市销售订单SQL脚本
生成5个月（7月-11月）的销售订单数据，约500条订单
"""

import random
from datetime import datetime, timedelta

# 配置
START_DATE = datetime(2024, 7, 1)
END_DATE = datetime(2024, 11, 30)
CUSTOMER_COUNT = 50  # 客户数量
CASHIER_ID = 3  # 收银员ID

# 商品ID范围（1-85）
PRODUCT_IDS = list(range(1, 86))

# 常见购买商品组合（商品ID列表）
COMMON_COMBOS = [
    # 日常购物
    [26, 28, 9, 10],  # 可乐+水+薯片+饼干
    [1, 2, 3, 4, 5],  # 米油酱盐鸡精
    [19, 20, 26, 28],  # 方便面+饮料
    [36, 37, 75],  # 牛奶+鸡蛋
    [57, 58, 59, 60],  # 蔬菜组合
    [64, 65, 66],  # 水果组合
    [71, 72, 73],  # 肉类组合
    
    # 零食组合
    [9, 10, 11, 12, 13],  # 各种零食
    [14, 15, 16, 17, 18],  # 零食组合2
    
    # 日用品组合
    [41, 42, 44, 45],  # 洗发水+牙膏
    [48, 49, 53, 54],  # 洗衣液+纸巾
    [43, 47, 51],  # 沐浴露+洗手液
    
    # 饮料组合
    [26, 27, 28, 29, 30],  # 各种饮料
    [33, 34, 35],  # 茶饮料
    [36, 37, 38, 39, 40],  # 乳制品
    
    # 酒水组合
    [79, 80, 81, 82],  # 啤酒
    [83, 84, 85],  # 红酒
]

def generate_order_no(date):
    """生成订单编号"""
    return f"SO{date.strftime('%Y%m%d')}{random.randint(1, 9999):04d}"

def generate_order_items(order_id):
    """生成订单明细"""
    # 随机选择购买模式
    if random.random() < 0.6:  # 60%概率使用常见组合
        combo = random.choice(COMMON_COMBOS)
        product_ids = combo.copy()
        # 可能添加1-2个随机商品
        if random.random() < 0.3:
            product_ids.extend(random.sample(PRODUCT_IDS, random.randint(1, 2)))
    else:  # 40%概率完全随机
        product_ids = random.sample(PRODUCT_IDS, random.randint(2, 6))
    
    items = []
    for product_id in product_ids:
        quantity = random.randint(1, 5)
        # 根据商品类型设置价格范围
        if product_id <= 8:  # 粮油调味
            unit_price = round(random.uniform(3.5, 89.9), 2)
        elif product_id <= 18:  # 零食
            unit_price = round(random.uniform(5.5, 45.9), 2)
        elif product_id <= 25:  # 方便食品
            unit_price = round(random.uniform(5.5, 22.9), 2)
        elif product_id <= 35:  # 饮料
            unit_price = round(random.uniform(1.5, 6.5), 2)
        elif product_id <= 40:  # 乳制品
            unit_price = round(random.uniform(16.9, 54.9), 2)
        elif product_id <= 47:  # 个人护理
            unit_price = round(random.uniform(18.9, 49.9), 2)
        elif product_id <= 52:  # 清洁用品
            unit_price = round(random.uniform(15.9, 59.9), 2)
        elif product_id <= 56:  # 纸品
            unit_price = round(random.uniform(12.9, 32.9), 2)
        elif product_id <= 63:  # 蔬菜
            unit_price = round(random.uniform(2.9, 6.9), 2)
        elif product_id <= 70:  # 水果
            unit_price = round(random.uniform(3.9, 15.9), 2)
        elif product_id <= 75:  # 肉禽蛋
            unit_price = round(random.uniform(12.9, 22.9), 2)
        elif product_id <= 78:  # 白酒
            unit_price = round(random.uniform(299.0, 1899.0), 2)
        elif product_id <= 82:  # 啤酒
            unit_price = round(random.uniform(4.0, 8.9), 2)
        else:  # 红酒
            unit_price = round(random.uniform(68.0, 128.0), 2)
        
        total_price = round(unit_price * quantity, 2)
        items.append({
            'order_id': order_id,
            'product_id': product_id,
            'quantity': quantity,
            'unit_price': unit_price,
            'total_price': total_price
        })
    
    return items

def generate_orders():
    """生成所有订单"""
    orders = []
    order_items = []
    order_id = 1
    
    current_date = START_DATE
    while current_date <= END_DATE:
        # 每天生成3-5个订单
        daily_orders = random.randint(3, 5)
        
        for i in range(daily_orders):
            # 随机选择客户（1-50）
            customer_id = random.randint(1, CUSTOMER_COUNT)
            
            # 生成订单时间（营业时间8:00-20:00）
            hour = random.randint(8, 19)
            minute = random.randint(0, 59)
            order_time = current_date.replace(hour=hour, minute=minute, second=0)
            
            # 生成订单编号
            order_no = generate_order_no(current_date) + f"{i+1:02d}"
            
            # 生成订单明细
            items = generate_order_items(order_id)
            total_amount = sum(item['total_price'] for item in items)
            
            # 添加订单
            orders.append({
                'order_no': order_no,
                'customer_id': customer_id,
                'total_amount': round(total_amount, 2),
                'status': 1,
                'cashier_id': CASHIER_ID,
                'create_time': order_time.strftime('%Y-%m-%d %H:%M:%S')
            })
            
            # 添加订单明细
            order_items.extend(items)
            order_id += 1
        
        # 下一天
        current_date += timedelta(days=1)
    
    return orders, order_items

def generate_sql():
    """生成SQL脚本"""
    orders, order_items = generate_orders()
    
    sql_lines = []
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 销售订单数据（{len(orders)}条订单）")
    sql_lines.append("-- 时间范围：2024-07-01 至 2024-11-30")
    sql_lines.append("-- =============================================\n")
    
    # 生成订单SQL
    sql_lines.append("-- 销售订单")
    sql_lines.append("INSERT INTO sale_order (order_no, customer_id, total_amount, status, cashier_id, create_time) VALUES")
    
    order_values = []
    for order in orders:
        order_values.append(
            f"('{order['order_no']}', {order['customer_id']}, {order['total_amount']}, "
            f"{order['status']}, {order['cashier_id']}, '{order['create_time']}')"
        )
    
    sql_lines.append(",\n".join(order_values) + ";\n")
    
    # 生成订单明细SQL（分批，每500条一批）
    sql_lines.append("-- 销售订单明细")
    batch_size = 500
    for i in range(0, len(order_items), batch_size):
        batch = order_items[i:i+batch_size]
        sql_lines.append("INSERT INTO sale_order_item (order_id, product_id, quantity, unit_price, total_price) VALUES")
        
        item_values = []
        for item in batch:
            item_values.append(
                f"({item['order_id']}, {item['product_id']}, {item['quantity']}, "
                f"{item['unit_price']}, {item['total_price']})"
            )
        
        sql_lines.append(",\n".join(item_values) + ";\n")
    
    # 统计信息
    sql_lines.append("\n-- =============================================")
    sql_lines.append("-- 数据统计")
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 订单总数: {len(orders)}")
    sql_lines.append(f"-- 订单明细总数: {len(order_items)}")
    sql_lines.append(f"-- 总销售额: {sum(o['total_amount'] for o in orders):.2f}")
    sql_lines.append(f"-- 平均订单金额: {sum(o['total_amount'] for o in orders) / len(orders):.2f}")
    
    return "\n".join(sql_lines)

if __name__ == "__main__":
    print("正在生成销售订单SQL脚本...")
    sql_content = generate_sql()
    
    # 保存到文件
    output_file = "销售订单数据.sql"
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql_content)
    
    print(f"✅ SQL脚本已生成: {output_file}")
    print(f"📊 请执行该脚本来导入销售订单数据")
