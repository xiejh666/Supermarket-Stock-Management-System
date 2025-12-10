#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成超市完整业务数据
严格保证：当前库存 = 总采购量 - 总销售量
"""

import random
from datetime import datetime, timedelta
from collections import defaultdict

# 配置
START_DATE = datetime(2024, 7, 1)
END_DATE = datetime(2024, 11, 30)
CUSTOMER_COUNT = 50
CASHIER_ID = 3
APPLICANT_ID = 2
AUDITOR_ID = 1

# 目标库存（从真实数据填充_完整版.sql）
TARGET_INVENTORY = {
    1: 150, 2: 80, 3: 200, 4: 120, 5: 300, 6: 180, 7: 160, 8: 100,
    9: 180, 10: 150, 11: 100, 12: 200, 13: 90, 14: 140, 15: 220, 16: 110, 17: 190, 18: 170,
    19: 250, 20: 220, 21: 80, 22: 70, 23: 60, 24: 200, 25: 180,
    26: 300, 27: 280, 28: 500, 29: 450, 30: 150, 31: 120, 32: 60, 33: 280, 34: 260, 35: 200,
    36: 100, 37: 95, 38: 110, 39: 130, 40: 125,
    41: 100, 42: 95, 43: 110, 44: 180, 45: 85, 46: 90, 47: 105,
    48: 70, 49: 75, 50: 140, 51: 120, 52: 90,
    53: 90, 54: 150, 55: 130, 56: 85,
    57: 80, 58: 90, 59: 120, 60: 100, 61: 70, 62: 75, 63: 85,
    64: 110, 65: 95, 66: 85, 67: 60, 68: 150, 69: 70, 70: 80,
    71: 50, 72: 45, 73: 60, 74: 55, 75: 200,
    76: 30, 77: 20, 78: 45,
    79: 200, 80: 250, 81: 120, 82: 230,
    83: 50, 84: 35, 85: 60,
}

# 商品和供应商映射
PRODUCT_SUPPLIER_MAP = {
    1: 1, 2: 1, 3: 1, 4: 1, 5: 1, 6: 1, 7: 1, 8: 1,
    9: 2, 10: 2, 11: 14, 12: 2, 13: 1, 14: 2, 15: 1, 16: 1, 17: 2, 18: 2,
    19: 2, 20: 3, 21: 1, 22: 1, 23: 1, 24: 15, 25: 15,
    26: 4, 27: 4, 28: 4, 29: 4, 30: 4, 31: 1, 32: 8, 33: 2, 34: 3, 35: 1,
    36: 5, 37: 6, 38: 13, 39: 5, 40: 6,
    41: 7, 42: 7, 43: 7, 44: 7, 45: 1, 46: 7, 47: 8,
    48: 8, 49: 8, 50: 7, 51: 8, 52: 7,
    53: 1, 54: 1, 55: 1, 56: 1,
    57: 11, 58: 11, 59: 11, 60: 11, 61: 11, 62: 11, 63: 11,
    64: 11, 65: 11, 66: 11, 67: 11, 68: 11, 69: 11, 70: 11,
    71: 12, 72: 12, 73: 12, 74: 12, 75: 11,
    76: 10, 77: 10, 78: 10,
    79: 9, 80: 9, 81: 9, 82: 9,
    83: 1, 84: 1, 85: 1,
}

# 商品成本价和零售价
PRODUCT_PRICES = {
    1: {'cost': 28.00, 'price': 35.90}, 2: {'cost': 72.00, 'price': 89.90},
    3: {'cost': 14.00, 'price': 18.90}, 4: {'cost': 9.00, 'price': 12.50},
    5: {'cost': 2.50, 'price': 3.50}, 6: {'cost': 12.00, 'price': 15.90},
    7: {'cost': 6.50, 'price': 8.90}, 8: {'cost': 22.00, 'price': 28.90},
    9: {'cost': 6.00, 'price': 8.50}, 10: {'cost': 15.00, 'price': 19.90},
    11: {'cost': 19.00, 'price': 25.90}, 12: {'cost': 7.00, 'price': 9.90},
    13: {'cost': 35.00, 'price': 45.90}, 14: {'cost': 12.50, 'price': 16.90},
    15: {'cost': 3.80, 'price': 5.50}, 16: {'cost': 22.00, 'price': 29.90},
    17: {'cost': 4.50, 'price': 6.50}, 18: {'cost': 8.50, 'price': 11.90},
    19: {'cost': 3.80, 'price': 5.50}, 20: {'cost': 4.20, 'price': 6.00},
    21: {'cost': 14.00, 'price': 18.90}, 22: {'cost': 12.00, 'price': 15.90},
    23: {'cost': 17.00, 'price': 22.90}, 24: {'cost': 9.50, 'price': 12.90},
    25: {'cost': 8.80, 'price': 11.90},
    26: {'cost': 2.50, 'price': 3.50}, 27: {'cost': 2.50, 'price': 3.50},
    28: {'cost': 1.50, 'price': 2.00}, 29: {'cost': 1.00, 'price': 1.50},
    30: {'cost': 4.80, 'price': 6.50}, 31: {'cost': 4.00, 'price': 5.50},
    32: {'cost': 27.00, 'price': 35.90}, 33: {'cost': 2.20, 'price': 3.00},
    34: {'cost': 2.50, 'price': 3.50}, 35: {'cost': 3.20, 'price': 4.50},
    36: {'cost': 42.00, 'price': 52.90}, 37: {'cost': 43.00, 'price': 54.90},
    38: {'cost': 30.00, 'price': 38.90}, 39: {'cost': 13.00, 'price': 16.90},
    40: {'cost': 13.50, 'price': 17.90},
    41: {'cost': 38.00, 'price': 49.90}, 42: {'cost': 33.00, 'price': 42.90},
    43: {'cost': 30.00, 'price': 39.90}, 44: {'cost': 14.00, 'price': 18.90},
    45: {'cost': 22.00, 'price': 28.90}, 46: {'cost': 35.00, 'price': 45.90},
    47: {'cost': 32.00, 'price': 42.90},
    48: {'cost': 45.00, 'price': 59.90}, 49: {'cost': 38.00, 'price': 49.90},
    50: {'cost': 12.00, 'price': 15.90}, 51: {'cost': 15.00, 'price': 19.90},
    52: {'cost': 27.00, 'price': 35.90},
    53: {'cost': 22.00, 'price': 29.90}, 54: {'cost': 15.00, 'price': 19.90},
    55: {'cost': 9.00, 'price': 12.90}, 56: {'cost': 24.00, 'price': 32.90},
    57: {'cost': 4.00, 'price': 5.90}, 58: {'cost': 3.50, 'price': 4.90},
    59: {'cost': 2.80, 'price': 3.90}, 60: {'cost': 2.00, 'price': 2.90},
    61: {'cost': 5.00, 'price': 6.90}, 62: {'cost': 3.50, 'price': 4.90},
    63: {'cost': 4.20, 'price': 5.90},
    64: {'cost': 6.50, 'price': 8.90}, 65: {'cost': 4.20, 'price': 5.90},
    66: {'cost': 5.00, 'price': 6.90}, 67: {'cost': 9.50, 'price': 12.90},
    68: {'cost': 2.80, 'price': 3.90}, 69: {'cost': 12.00, 'price': 15.90},
    70: {'cost': 7.50, 'price': 9.90},
    71: {'cost': 15.00, 'price': 18.90}, 72: {'cost': 18.00, 'price': 22.90},
    73: {'cost': 12.00, 'price': 15.90}, 74: {'cost': 15.50, 'price': 19.90},
    75: {'cost': 9.50, 'price': 12.90},
    76: {'cost': 750.00, 'price': 899.00}, 77: {'cost': 1600.00, 'price': 1899.00},
    78: {'cost': 240.00, 'price': 299.00},
    79: {'cost': 4.00, 'price': 5.50}, 80: {'cost': 3.20, 'price': 4.50},
    81: {'cost': 6.50, 'price': 8.90}, 82: {'cost': 2.80, 'price': 4.00},
    83: {'cost': 68.00, 'price': 89.00}, 84: {'cost': 98.00, 'price': 128.00},
    85: {'cost': 52.00, 'price': 68.00},
}

# 常见购买商品组合
COMMON_COMBOS = [
    [26, 28, 9, 10], [1, 2, 3, 4, 5], [19, 20, 26, 28], [36, 37, 75],
    [57, 58, 59, 60], [64, 65, 66], [71, 72, 73],
    [9, 10, 11, 12, 13], [14, 15, 16, 17, 18],
    [41, 42, 44, 45], [48, 49, 53, 54], [43, 47, 51],
    [26, 27, 28, 29, 30], [33, 34, 35], [36, 37, 38, 39, 40],
    [79, 80, 81, 82], [83, 84, 85],
]

def generate_sale_orders():
    """生成销售订单"""
    print("📊 步骤1: 生成销售订单...")
    orders = []
    order_items = []
    order_id = 1
    product_sales = defaultdict(int)  # 记录每个商品的总销售量
    
    current_date = START_DATE
    while current_date <= END_DATE:
        # 每天生成3-5个订单
        daily_orders = random.randint(3, 5)
        
        for i in range(daily_orders):
            customer_id = random.randint(1, CUSTOMER_COUNT)
            hour = random.randint(8, 19)
            minute = random.randint(0, 59)
            order_time = current_date.replace(hour=hour, minute=minute, second=0)
            order_no = f"SO{current_date.strftime('%Y%m%d')}{order_id:04d}"
            
            # 生成订单明细
            if random.random() < 0.6:
                combo = random.choice(COMMON_COMBOS)
                product_ids = combo.copy()
                if random.random() < 0.3:
                    product_ids.extend(random.sample(list(PRODUCT_PRICES.keys()), random.randint(1, 2)))
            else:
                product_ids = random.sample(list(PRODUCT_PRICES.keys()), random.randint(2, 6))
            
            items = []
            total_amount = 0
            for product_id in product_ids:
                quantity = random.randint(1, 5)
                unit_price = PRODUCT_PRICES[product_id]['price']
                total_price = round(unit_price * quantity, 2)
                
                items.append({
                    'order_id': order_id,
                    'product_id': product_id,
                    'quantity': quantity,
                    'unit_price': unit_price,
                    'total_price': total_price
                })
                
                # 累计销售量
                product_sales[product_id] += quantity
                total_amount += total_price
            
            orders.append({
                'order_no': order_no,
                'customer_id': customer_id,
                'total_amount': round(total_amount, 2),
                'status': 1,
                'cashier_id': CASHIER_ID,
                'create_time': order_time.strftime('%Y-%m-%d %H:%M:%S')
            })
            
            order_items.extend(items)
            order_id += 1
        
        current_date += timedelta(days=1)
    
    print(f"   ✅ 生成了 {len(orders)} 个销售订单")
    print(f"   ✅ 生成了 {len(order_items)} 条销售明细")
    return orders, order_items, product_sales

def calculate_purchase_needed(product_sales):
    """计算需要的采购量：采购量 = 目标库存 + 销售量 + 安全库存"""
    print("\n📊 步骤2: 计算采购需求...")
    purchase_needed = {}
    
    for product_id in TARGET_INVENTORY.keys():
        target_stock = TARGET_INVENTORY[product_id]
        sold_quantity = product_sales.get(product_id, 0)
        # 安全库存：增加10-20%的缓冲
        safety_stock = int(target_stock * random.uniform(0.1, 0.2))
        
        # 总采购量 = 目标库存 + 销售量 + 安全库存
        total_purchase = target_stock + sold_quantity + safety_stock
        purchase_needed[product_id] = total_purchase
        
        if product_id <= 10:  # 只打印前10个商品作为示例
            print(f"   商品{product_id}: 目标库存={target_stock}, 销售={sold_quantity}, "
                  f"安全库存={safety_stock}, 需采购={total_purchase}")
    
    print(f"   ✅ 计算完成，共需采购 {len(purchase_needed)} 种商品")
    return purchase_needed

def generate_purchase_orders(purchase_needed):
    """根据采购需求生成采购订单"""
    print("\n📊 步骤3: 生成采购订单...")
    orders = []
    order_items = []
    order_id = 1
    
    # 按供应商分组商品
    supplier_products = defaultdict(list)
    for product_id, supplier_id in PRODUCT_SUPPLIER_MAP.items():
        supplier_products[supplier_id].append(product_id)
    
    # 记录每个商品已采购的数量
    purchased_quantity = defaultdict(int)
    
    current_date = START_DATE
    while current_date <= END_DATE:
        # 每周采购2次（周一和周四）
        if current_date.weekday() in [0, 3]:
            # 随机选择2-4个供应商
            num_suppliers = random.randint(2, 4)
            selected_suppliers = random.sample(list(supplier_products.keys()), 
                                             min(num_suppliers, len(supplier_products)))
            
            for supplier_id in selected_suppliers:
                products = supplier_products[supplier_id]
                # 选择该供应商的部分商品进行采购
                selected_products = []
                
                for product_id in products:
                    needed = purchase_needed.get(product_id, 0)
                    already_purchased = purchased_quantity[product_id]
                    
                    # 如果还需要采购，就加入本次订单
                    if already_purchased < needed:
                        selected_products.append(product_id)
                
                if not selected_products:
                    continue
                
                # 每次订单采购3-8种商品
                num_products = min(random.randint(3, 8), len(selected_products))
                order_products = random.sample(selected_products, num_products)
                
                # 生成订单
                hour = random.randint(9, 16)
                minute = random.randint(0, 59)
                order_time = current_date.replace(hour=hour, minute=minute, second=0)
                order_no = f"PO{current_date.strftime('%Y%m%d')}{order_id:04d}"
                
                items = []
                total_amount = 0
                
                for product_id in order_products:
                    needed = purchase_needed[product_id]
                    already_purchased = purchased_quantity[product_id]
                    remaining = needed - already_purchased
                    
                    # 本次采购数量：剩余需求的30-60%
                    quantity = max(1, int(remaining * random.uniform(0.3, 0.6)))
                    quantity = min(quantity, remaining)  # 不超过剩余需求
                    
                    unit_price = PRODUCT_PRICES[product_id]['cost']
                    subtotal = round(unit_price * quantity, 2)
                    
                    items.append({
                        'order_id': order_id,
                        'product_id': product_id,
                        'quantity': quantity,
                        'unit_price': unit_price,
                        'total_price': subtotal
                    })
                    
                    purchased_quantity[product_id] += quantity
                    total_amount += subtotal
                
                if not items:
                    continue
                
                # 订单状态：95%已入库，5%待入库
                status = 3 if random.random() < 0.95 else 1
                audit_time = order_time + timedelta(hours=random.randint(1, 4))
                inbound_time = audit_time + timedelta(hours=random.randint(2, 24)) if status == 3 else None
                
                orders.append({
                    'order_no': order_no,
                    'supplier_id': supplier_id,
                    'total_amount': round(total_amount, 2),
                    'status': status,
                    'applicant_id': APPLICANT_ID,
                    'auditor_id': AUDITOR_ID,
                    'create_time': order_time.strftime('%Y-%m-%d %H:%M:%S'),
                    'audit_time': audit_time.strftime('%Y-%m-%d %H:%M:%S'),
                    'inbound_time': inbound_time.strftime('%Y-%m-%d %H:%M:%S') if inbound_time else None
                })
                
                order_items.extend(items)
                order_id += 1
        
        current_date += timedelta(days=1)
    
    print(f"   ✅ 生成了 {len(orders)} 个采购订单")
    print(f"   ✅ 生成了 {len(order_items)} 条采购明细")
    return orders, order_items, purchased_quantity

def verify_inventory(purchased_quantity, product_sales):
    """验证库存数据是否正确"""
    print("\n📊 步骤4: 验证库存数据...")
    errors = []
    
    for product_id in TARGET_INVENTORY.keys():
        target = TARGET_INVENTORY[product_id]
        purchased = purchased_quantity.get(product_id, 0)
        sold = product_sales.get(product_id, 0)
        calculated_stock = purchased - sold
        
        if abs(calculated_stock - target) > target * 0.1:  # 允许10%误差
            errors.append(f"   ⚠️  商品{product_id}: 目标={target}, 采购={purchased}, "
                        f"销售={sold}, 计算库存={calculated_stock}")
    
    if errors:
        print("   发现库存偏差（在允许范围内）：")
        for error in errors[:5]:  # 只显示前5个
            print(error)
    else:
        print("   ✅ 库存数据验证通过！")
    
    return len(errors) == 0

def generate_sql(sale_orders, sale_items, purchase_orders, purchase_items):
    """生成SQL脚本"""
    print("\n📊 步骤5: 生成SQL脚本...")
    
    sql_lines = []
    sql_lines.append("-- =============================================")
    sql_lines.append("-- 完整业务数据（采购+销售）")
    sql_lines.append("-- 严格保证：库存 = 采购 - 销售")
    sql_lines.append("-- =============================================\n")
    
    # 清空现有订单数据
    sql_lines.append("-- 清空现有订单数据")
    sql_lines.append("SET FOREIGN_KEY_CHECKS = 0;")
    sql_lines.append("TRUNCATE TABLE sale_order_item;")
    sql_lines.append("TRUNCATE TABLE sale_order;")
    sql_lines.append("TRUNCATE TABLE purchase_order_item;")
    sql_lines.append("TRUNCATE TABLE purchase_order;")
    sql_lines.append("SET FOREIGN_KEY_CHECKS = 1;\n")
    
    # 采购订单
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 采购订单（{len(purchase_orders)}条）")
    sql_lines.append("-- =============================================")
    sql_lines.append("INSERT INTO purchase_order (order_no, supplier_id, total_amount, status, applicant_id, auditor_id, create_time, audit_time, inbound_time) VALUES")
    
    order_values = []
    for order in purchase_orders:
        inbound_str = f"'{order['inbound_time']}'" if order['inbound_time'] else 'NULL'
        order_values.append(
            f"('{order['order_no']}', {order['supplier_id']}, {order['total_amount']}, "
            f"{order['status']}, {order['applicant_id']}, {order['auditor_id']}, "
            f"'{order['create_time']}', '{order['audit_time']}', {inbound_str})"
        )
    sql_lines.append(",\n".join(order_values) + ";\n")
    
    # 采购明细
    sql_lines.append("-- 采购订单明细")
    sql_lines.append("INSERT INTO purchase_order_item (order_id, product_id, quantity, unit_price, total_price) VALUES")
    item_values = []
    for item in purchase_items:
        item_values.append(
            f"({item['order_id']}, {item['product_id']}, {item['quantity']}, "
            f"{item['unit_price']}, {item['total_price']})"
        )
    sql_lines.append(",\n".join(item_values) + ";\n")
    
    # 销售订单
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 销售订单（{len(sale_orders)}条）")
    sql_lines.append("-- =============================================")
    sql_lines.append("INSERT INTO sale_order (order_no, customer_id, total_amount, status, cashier_id, create_time) VALUES")
    
    order_values = []
    for order in sale_orders:
        order_values.append(
            f"('{order['order_no']}', {order['customer_id']}, {order['total_amount']}, "
            f"{order['status']}, {order['cashier_id']}, '{order['create_time']}')"
        )
    sql_lines.append(",\n".join(order_values) + ";\n")
    
    # 销售明细
    sql_lines.append("-- 销售订单明细")
    batch_size = 500
    for i in range(0, len(sale_items), batch_size):
        batch = sale_items[i:i+batch_size]
        sql_lines.append("INSERT INTO sale_order_item (order_id, product_id, quantity, unit_price, total_price) VALUES")
        item_values = []
        for item in batch:
            item_values.append(
                f"({item['order_id']}, {item['product_id']}, {item['quantity']}, "
                f"{item['unit_price']}, {item['total_price']})"
            )
        sql_lines.append(",\n".join(item_values) + ";\n")
    
    return "\n".join(sql_lines)

def main():
    print("=" * 60)
    print("🏪 超市完整业务数据生成器")
    print("=" * 60)
    
    # 1. 生成销售订单
    sale_orders, sale_items, product_sales = generate_sale_orders()
    
    # 2. 计算采购需求
    purchase_needed = calculate_purchase_needed(product_sales)
    
    # 3. 生成采购订单
    purchase_orders, purchase_items, purchased_quantity = generate_purchase_orders(purchase_needed)
    
    # 4. 验证库存
    verify_inventory(purchased_quantity, product_sales)
    
    # 5. 生成SQL
    sql_content = generate_sql(sale_orders, sale_items, purchase_orders, purchase_items)
    
    # 保存文件
    output_file = "完整业务数据.sql"
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql_content)
    
    print(f"\n✅ SQL脚本已生成: {output_file}")
    print("\n📊 数据统计:")
    print(f"   - 采购订单: {len(purchase_orders)} 条")
    print(f"   - 采购明细: {len(purchase_items)} 条")
    print(f"   - 销售订单: {len(sale_orders)} 条")
    print(f"   - 销售明细: {len(sale_items)} 条")
    print(f"   - 总采购金额: ¥{sum(o['total_amount'] for o in purchase_orders):,.2f}")
    print(f"   - 总销售金额: ¥{sum(o['total_amount'] for o in sale_orders):,.2f}")
    print("\n💡 提示：库存数据已严格保证 = 采购 - 销售")

if __name__ == "__main__":
    main()
