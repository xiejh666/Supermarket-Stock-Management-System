#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成超市采购订单SQL脚本
根据库存数据生成合理的采购订单
"""

import random
from datetime import datetime, timedelta

# 配置
START_DATE = datetime(2024, 7, 1)
END_DATE = datetime(2024, 11, 30)
APPLICANT_ID = 2  # 采购员ID
AUDITOR_ID = 1    # 审核人ID（管理员）

# 商品和供应商映射（根据真实数据填充_完整版.sql）
PRODUCT_SUPPLIER_MAP = {
    # 粮油调味（供应商1）
    1: 1, 2: 1, 3: 1, 4: 1, 5: 1, 6: 1, 7: 1, 8: 1,
    # 休闲零食（供应商2,14）
    9: 2, 10: 2, 11: 14, 12: 2, 13: 1, 14: 2, 15: 1, 16: 1, 17: 2, 18: 2,
    # 方便速食（供应商2,3,1,15）
    19: 2, 20: 3, 21: 1, 22: 1, 23: 1, 24: 15, 25: 15,
    # 饮料冲调（供应商4,1,8,2,3）
    26: 4, 27: 4, 28: 4, 29: 4, 30: 4, 31: 1, 32: 8, 33: 2, 34: 3, 35: 1,
    # 乳制品（供应商5,6,13）
    36: 5, 37: 6, 38: 13, 39: 5, 40: 6,
    # 个人护理（供应商7,1,8）
    41: 7, 42: 7, 43: 7, 44: 7, 45: 1, 46: 7, 47: 8,
    # 家居清洁（供应商8,7）
    48: 8, 49: 8, 50: 7, 51: 8, 52: 7,
    # 纸品湿巾（供应商1）
    53: 1, 54: 1, 55: 1, 56: 1,
    # 蔬菜（供应商11）
    57: 11, 58: 11, 59: 11, 60: 11, 61: 11, 62: 11, 63: 11,
    # 水果（供应商11）
    64: 11, 65: 11, 66: 11, 67: 11, 68: 11, 69: 11, 70: 11,
    # 肉禽蛋（供应商12,11）
    71: 12, 72: 12, 73: 12, 74: 12, 75: 11,
    # 白酒（供应商10）
    76: 10, 77: 10, 78: 10,
    # 啤酒（供应商9）
    79: 9, 80: 9, 81: 9, 82: 9,
    # 红酒（供应商1）
    83: 1, 84: 1, 85: 1,
}

# 商品成本价（根据真实数据）
PRODUCT_COST_PRICE = {
    1: 28.00, 2: 72.00, 3: 14.00, 4: 9.00, 5: 2.50, 6: 12.00, 7: 6.50, 8: 22.00,
    9: 6.00, 10: 15.00, 11: 19.00, 12: 7.00, 13: 35.00, 14: 12.50, 15: 3.80, 16: 22.00, 17: 4.50, 18: 8.50,
    19: 3.80, 20: 4.20, 21: 14.00, 22: 12.00, 23: 17.00, 24: 9.50, 25: 8.80,
    26: 2.50, 27: 2.50, 28: 1.50, 29: 1.00, 30: 4.80, 31: 4.00, 32: 27.00, 33: 2.20, 34: 2.50, 35: 3.20,
    36: 42.00, 37: 43.00, 38: 30.00, 39: 13.00, 40: 13.50,
    41: 38.00, 42: 33.00, 43: 30.00, 44: 14.00, 45: 22.00, 46: 35.00, 47: 32.00,
    48: 45.00, 49: 38.00, 50: 12.00, 51: 15.00, 52: 27.00,
    53: 22.00, 54: 15.00, 55: 9.00, 56: 24.00,
    57: 4.00, 58: 3.50, 59: 2.80, 60: 2.00, 61: 5.00, 62: 3.50, 63: 4.20,
    64: 6.50, 65: 4.20, 66: 5.00, 67: 9.50, 68: 2.80, 69: 12.00, 70: 7.50,
    71: 15.00, 72: 18.00, 73: 12.00, 74: 15.50, 75: 9.50,
    76: 750.00, 77: 1600.00, 78: 240.00,
    79: 4.00, 80: 3.20, 81: 6.50, 82: 2.80,
    83: 68.00, 84: 98.00, 85: 52.00,
}

# 商品名称
PRODUCT_NAMES = {
    1: '金龙鱼大米', 2: '福临门食用油', 3: '海天酱油', 4: '太太乐鸡精', 5: '中盐食用盐',
    6: '李锦记蚝油', 7: '老干妈豆豉', 8: '香满园面粉',
    9: '乐事薯片', 10: '奥利奥饼干', 11: '三只松鼠坚果', 12: '旺旺雪饼', 13: '德芙巧克力',
    14: '好丽友派', 15: '卫龙辣条', 16: '徐福记糖果', 17: '上好佳薯片', 18: '旺旺仙贝',
    19: '康师傅红烧牛肉面', 20: '统一老坛酸菜面', 21: '三全水饺', 22: '思念汤圆', 23: '湾仔码头馄饨',
    24: '今麦郎方便面', 25: '白象方便面',
    26: '可口可乐', 27: '百事可乐', 28: '农夫山泉', 29: '娃哈哈纯净水', 30: '红牛功能饮料',
    31: '雀巢咖啡', 32: '立顿红茶', 33: '康师傅冰红茶', 34: '统一冰糖雪梨', 35: '王老吉凉茶',
    36: '蒙牛纯牛奶', 37: '伊利纯牛奶', 38: '光明酸奶', 39: '蒙牛酸酸乳', 40: '伊利优酸乳',
    41: '海飞丝洗发水', 42: '飘柔洗发水', 43: '舒肤佳沐浴露', 44: '高露洁牙膏', 45: '云南白药牙膏',
    46: '潘婷洗发水', 47: '多芬沐浴露',
    48: '奥妙洗衣液', 49: '立白洗衣液', 50: '威猛先生清洁剂', 51: '蓝月亮洗手液', 52: '汰渍洗衣粉',
    53: '维达卷纸', 54: '清风抽纸', 55: '心相印湿巾', 56: '洁柔卷纸',
    57: '新鲜西红柿', 58: '新鲜黄瓜', 59: '新鲜土豆', 60: '新鲜白菜', 61: '新鲜菠菜',
    62: '新鲜芹菜', 63: '新鲜青椒',
    64: '红富士苹果', 65: '香蕉', 66: '橙子', 67: '葡萄', 68: '西瓜', 69: '草莓', 70: '猕猴桃',
    71: '猪肉（五花肉）', 72: '猪肉（里脊肉）', 73: '鸡腿', 74: '鸡翅', 75: '鸡蛋',
    76: '五粮液', 77: '茅台酒', 78: '洋河蓝色经典',
    79: '青岛啤酒', 80: '雪花啤酒', 81: '百威啤酒', 82: '燕京啤酒',
    83: '长城干红葡萄酒', 84: '张裕红酒', 85: '王朝干红',
}

def generate_order_no(date, index):
    """生成采购订单编号"""
    return f"PO{date.strftime('%Y%m%d')}{index:04d}"

def generate_purchase_orders():
    """生成采购订单"""
    orders = []
    order_items = []
    order_id = 1
    
    current_date = START_DATE
    
    # 按供应商分组商品
    supplier_products = {}
    for product_id, supplier_id in PRODUCT_SUPPLIER_MAP.items():
        if supplier_id not in supplier_products:
            supplier_products[supplier_id] = []
        supplier_products[supplier_id].append(product_id)
    
    while current_date <= END_DATE:
        # 每周生成2-4个采购订单（不同供应商）
        if current_date.weekday() in [0, 3]:  # 周一和周四采购
            num_orders = random.randint(2, 4)
            selected_suppliers = random.sample(list(supplier_products.keys()), 
                                             min(num_orders, len(supplier_products)))
            
            for idx, supplier_id in enumerate(selected_suppliers):
                # 生成订单时间
                hour = random.randint(9, 16)
                minute = random.randint(0, 59)
                order_time = current_date.replace(hour=hour, minute=minute, second=0)
                
                # 生成订单编号
                order_no = generate_order_no(current_date, order_id)
                
                # 从该供应商的商品中随机选择3-8种商品
                products = supplier_products[supplier_id]
                selected_products = random.sample(products, min(random.randint(3, 8), len(products)))
                
                # 生成订单明细
                items = []
                total_amount = 0
                
                for product_id in selected_products:
                    # 根据商品类型确定采购数量
                    if product_id <= 8:  # 粮油调味 - 大批量
                        quantity = random.randint(20, 50)
                    elif product_id <= 25:  # 零食、方便食品 - 中批量
                        quantity = random.randint(30, 80)
                    elif product_id <= 40:  # 饮料、乳制品 - 大批量
                        quantity = random.randint(50, 150)
                    elif product_id <= 56:  # 日用品、纸品 - 中批量
                        quantity = random.randint(20, 60)
                    elif product_id <= 70:  # 生鲜 - 小批量（易损耗）
                        quantity = random.randint(30, 80)
                    elif product_id <= 75:  # 肉禽蛋 - 小批量
                        quantity = random.randint(20, 50)
                    elif product_id <= 78:  # 白酒 - 小批量（高价值）
                        quantity = random.randint(5, 15)
                    elif product_id <= 82:  # 啤酒 - 大批量
                        quantity = random.randint(50, 120)
                    else:  # 红酒 - 中批量
                        quantity = random.randint(10, 30)
                    
                    unit_price = PRODUCT_COST_PRICE[product_id]
                    subtotal = round(unit_price * quantity, 2)
                    total_amount += subtotal
                    
                    items.append({
                        'order_id': order_id,
                        'product_id': product_id,
                        'quantity': quantity,
                        'unit_price': unit_price,
                        'subtotal': subtotal
                    })
                
                # 确定订单状态和时间
                # 80%已入库，15%待入库，5%已拒绝
                rand = random.random()
                if rand < 0.80:
                    status = 3  # 已入库
                    audit_time = order_time + timedelta(hours=random.randint(1, 4))
                    inbound_time = audit_time + timedelta(hours=random.randint(2, 24))
                elif rand < 0.95:
                    status = 1  # 待入库
                    audit_time = order_time + timedelta(hours=random.randint(1, 4))
                    inbound_time = None
                else:
                    status = 2  # 已拒绝
                    audit_time = order_time + timedelta(hours=random.randint(1, 4))
                    inbound_time = None
                
                # 添加订单
                order = {
                    'order_no': order_no,
                    'supplier_id': supplier_id,
                    'total_amount': round(total_amount, 2),
                    'status': status,
                    'applicant_id': APPLICANT_ID,
                    'auditor_id': AUDITOR_ID if status in [1, 3] else None,
                    'create_time': order_time.strftime('%Y-%m-%d %H:%M:%S'),
                    'audit_time': audit_time.strftime('%Y-%m-%d %H:%M:%S') if status in [1, 3] else None,
                    'inbound_time': inbound_time.strftime('%Y-%m-%d %H:%M:%S') if inbound_time else None
                }
                orders.append(order)
                
                # 添加订单明细
                order_items.extend(items)
                order_id += 1
        
        current_date += timedelta(days=1)
    
    return orders, order_items

def generate_sql():
    """生成SQL脚本"""
    orders, order_items = generate_purchase_orders()
    
    sql_lines = []
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 采购订单数据（{len(orders)}条订单）")
    sql_lines.append("-- 时间范围：2024-07-01 至 2024-11-30")
    sql_lines.append("-- 说明：根据库存数据生成，商品和供应商对应关系准确")
    sql_lines.append("-- =============================================\n")
    
    # 生成采购订单SQL
    sql_lines.append("-- 采购订单")
    sql_lines.append("INSERT INTO purchase_order (order_no, supplier_id, total_amount, status, applicant_id, auditor_id, create_time, audit_time, inbound_time) VALUES")
    
    order_values = []
    for order in orders:
        auditor_str = str(order['auditor_id']) if order['auditor_id'] else 'NULL'
        audit_time_str = f"'{order['audit_time']}'" if order['audit_time'] else 'NULL'
        inbound_time_str = f"'{order['inbound_time']}'" if order['inbound_time'] else 'NULL'
        
        order_values.append(
            f"('{order['order_no']}', {order['supplier_id']}, {order['total_amount']}, "
            f"{order['status']}, {order['applicant_id']}, {auditor_str}, "
            f"'{order['create_time']}', {audit_time_str}, {inbound_time_str})"
        )
    
    sql_lines.append(",\n".join(order_values) + ";\n")
    
    # 生成采购订单明细SQL（分批，每500条一批）
    sql_lines.append("-- 采购订单明细")
    batch_size = 500
    for i in range(0, len(order_items), batch_size):
        batch = order_items[i:i+batch_size]
        sql_lines.append("INSERT INTO purchase_order_item (order_id, product_id, quantity, unit_price, subtotal) VALUES")
        
        item_values = []
        for item in batch:
            item_values.append(
                f"({item['order_id']}, {item['product_id']}, {item['quantity']}, "
                f"{item['unit_price']}, {item['subtotal']})"
            )
        
        sql_lines.append(",\n".join(item_values) + ";\n")
    
    # 统计信息
    sql_lines.append("\n-- =============================================")
    sql_lines.append("-- 数据统计")
    sql_lines.append("-- =============================================")
    sql_lines.append(f"-- 采购订单总数: {len(orders)}")
    sql_lines.append(f"-- 订单明细总数: {len(order_items)}")
    
    status_count = {}
    for order in orders:
        status = order['status']
        status_count[status] = status_count.get(status, 0) + 1
    
    sql_lines.append(f"-- 待审核订单: {status_count.get(0, 0)}")
    sql_lines.append(f"-- 待入库订单: {status_count.get(1, 0)}")
    sql_lines.append(f"-- 已拒绝订单: {status_count.get(2, 0)}")
    sql_lines.append(f"-- 已入库订单: {status_count.get(3, 0)}")
    sql_lines.append(f"-- 总采购金额: {sum(o['total_amount'] for o in orders):.2f}")
    sql_lines.append(f"-- 平均订单金额: {sum(o['total_amount'] for o in orders) / len(orders):.2f}")
    
    return "\n".join(sql_lines)

if __name__ == "__main__":
    print("正在生成采购订单SQL脚本...")
    sql_content = generate_sql()
    
    # 保存到文件
    output_file = "采购订单数据.sql"
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql_content)
    
    print(f"✅ SQL脚本已生成: {output_file}")
    print(f"📊 请执行该脚本来导入采购订单数据")
    print(f"💡 提示：采购订单中的商品和供应商对应关系与库存数据完全一致")
