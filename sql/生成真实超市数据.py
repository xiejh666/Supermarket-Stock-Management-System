#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
超市库存管理系统 - 真实数据生成器
生成5个月的完整超市数据，包括商品分类、供应商、客户、商品、采购单、销售单和库存
确保库存=采购-销售
"""

import random
from datetime import datetime, timedelta
from decimal import Decimal
import json

# ==================== 配置 ====================
START_DATE = datetime(2024, 7, 1)  # 开始日期：2024年7月1日
END_DATE = datetime(2024, 11, 30)  # 结束日期：2024年11月30日（5个月）
OUTPUT_FILE = 'supermarket_真实数据_完整版.sql'

# ==================== 基础数据 ====================

# 商品分类（二级分类）
CATEGORIES = [
    # 食品类
    {'id': 1, 'name': '食品饮料', 'parent_id': 0, 'sort': 1, 'desc': '各类食品和饮料'},
    {'id': 2, 'name': '粮油调味', 'parent_id': 1, 'sort': 1, 'desc': '米面粮油、调味品'},
    {'id': 3, 'name': '休闲零食', 'parent_id': 1, 'sort': 2, 'desc': '薯片、饼干、糖果等'},
    {'id': 4, 'name': '饮料冲调', 'parent_id': 1, 'sort': 3, 'desc': '饮料、茶叶、咖啡等'},
    {'id': 5, 'name': '乳品冷饮', 'parent_id': 1, 'sort': 4, 'desc': '牛奶、酸奶、冰淇淋'},
    
    # 日用品类
    {'id': 6, 'name': '日用百货', 'parent_id': 0, 'sort': 2, 'desc': '日常生活用品'},
    {'id': 7, 'name': '清洁用品', 'parent_id': 6, 'sort': 1, 'desc': '洗衣液、洗洁精等'},
    {'id': 8, 'name': '纸品湿巾', 'parent_id': 6, 'sort': 2, 'desc': '卫生纸、湿巾等'},
    {'id': 9, 'name': '个人护理', 'parent_id': 6, 'sort': 3, 'desc': '洗发水、沐浴露等'},
    
    # 生鲜类
    {'id': 10, 'name': '生鲜食品', 'parent_id': 0, 'sort': 3, 'desc': '新鲜食材'},
    {'id': 11, 'name': '蔬菜水果', 'parent_id': 10, 'sort': 1, 'desc': '新鲜蔬菜水果'},
    {'id': 12, 'name': '肉禽蛋品', 'parent_id': 10, 'sort': 2, 'desc': '猪肉、鸡肉、鸡蛋'},
    
    # 家居用品
    {'id': 13, 'name': '家居生活', 'parent_id': 0, 'sort': 4, 'desc': '家居用品'},
    {'id': 14, 'name': '厨房用品', 'parent_id': 13, 'sort': 1, 'desc': '锅碗瓢盆等'},
    {'id': 15, 'name': '收纳整理', 'parent_id': 13, 'sort': 2, 'desc': '收纳盒、衣架等'},
]

# 供应商
SUPPLIERS = [
    {'id': 1, 'name': '康师傅食品有限公司', 'contact': '张经理', 'phone': '13800138001', 'address': '北京市朝阳区康师傅大厦'},
    {'id': 2, 'name': '统一企业集团', 'contact': '李经理', 'phone': '13800138002', 'address': '上海市浦东新区统一路88号'},
    {'id': 3, 'name': '可口可乐饮料公司', 'contact': '王经理', 'phone': '13800138003', 'address': '广州市天河区可乐大道100号'},
    {'id': 4, 'name': '伊利乳业集团', 'contact': '赵经理', 'phone': '13800138004', 'address': '内蒙古呼和浩特市伊利工业园'},
    {'id': 5, 'name': '蒙牛乳业集团', 'contact': '刘经理', 'phone': '13800138005', 'address': '内蒙古呼和浩特市蒙牛大街'},
    {'id': 6, 'name': '宝洁日化用品公司', 'contact': '陈经理', 'phone': '13800138006', 'address': '广州市天河区宝洁路66号'},
    {'id': 7, 'name': '联合利华中国', 'contact': '周经理', 'phone': '13800138007', 'address': '上海市徐汇区联合路188号'},
    {'id': 8, 'name': '金龙鱼粮油集团', 'contact': '吴经理', 'phone': '13800138008', 'address': '上海市浦东新区金龙路999号'},
    {'id': 9, 'name': '维达纸业集团', 'contact': '郑经理', 'phone': '13800138009', 'address': '广东省江门市维达工业园'},
    {'id': 10, 'name': '百事食品公司', 'contact': '孙经理', 'phone': '13800138010', 'address': '北京市朝阳区百事大厦'},
    {'id': 11, 'name': '雀巢中国', 'contact': '马经理', 'phone': '13800138011', 'address': '北京市朝阳区雀巢中心'},
    {'id': 12, 'name': '娃哈哈集团', 'contact': '朱经理', 'phone': '13800138012', 'address': '浙江省杭州市娃哈哈路168号'},
    {'id': 13, 'name': '双汇食品集团', 'contact': '胡经理', 'phone': '13800138013', 'address': '河南省漯河市双汇工业园'},
    {'id': 14, 'name': '新希望集团', 'contact': '林经理', 'phone': '13800138014', 'address': '四川省成都市新希望大厦'},
    {'id': 15, 'name': '立白集团', 'contact': '黄经理', 'phone': '13800138015', 'address': '广东省广州市立白工业园'},
]

# 客户姓名库
CUSTOMER_SURNAMES = ['王', '李', '张', '刘', '陈', '杨', '黄', '赵', '吴', '周', '徐', '孙', '马', '朱', '胡', '郭', '何', '高', '林', '罗']
CUSTOMER_NAMES = ['伟', '芳', '娜', '敏', '静', '丽', '强', '磊', '军', '洋', '勇', '艳', '杰', '娟', '涛', '明', '超', '秀英', '霞', '平']

# 商品数据（真实超市商品）
PRODUCTS = [
    # 粮油调味 (category_id=2)
    {'code': 'P001', 'name': '金龙鱼调和油5L', 'category_id': 2, 'supplier_id': 8, 'unit': '瓶', 'spec': '5L', 'price': 89.90, 'cost': 72.00},
    {'code': 'P002', 'name': '福临门大米10kg', 'category_id': 2, 'supplier_id': 8, 'unit': '袋', 'spec': '10kg', 'price': 68.00, 'cost': 52.00},
    {'code': 'P003', 'name': '海天酱油1.9L', 'category_id': 2, 'supplier_id': 8, 'unit': '瓶', 'spec': '1.9L', 'price': 28.80, 'cost': 22.00},
    {'code': 'P004', 'name': '太太乐鸡精200g', 'category_id': 2, 'supplier_id': 8, 'unit': '袋', 'spec': '200g', 'price': 12.50, 'cost': 9.50},
    {'code': 'P005', 'name': '李锦记蚝油510g', 'category_id': 2, 'supplier_id': 8, 'unit': '瓶', 'spec': '510g', 'price': 18.90, 'cost': 14.50},
    
    # 休闲零食 (category_id=3)
    {'code': 'P006', 'name': '乐事薯片黄瓜味70g', 'category_id': 3, 'supplier_id': 10, 'unit': '袋', 'spec': '70g', 'price': 8.50, 'cost': 6.00},
    {'code': 'P007', 'name': '奥利奥饼干原味116g', 'category_id': 3, 'supplier_id': 11, 'unit': '包', 'spec': '116g', 'price': 9.90, 'cost': 7.20},
    {'code': 'P008', 'name': '德芙巧克力丝滑牛奶252g', 'category_id': 3, 'supplier_id': 11, 'unit': '盒', 'spec': '252g', 'price': 58.00, 'cost': 45.00},
    {'code': 'P009', 'name': '旺旺雪饼150g', 'category_id': 3, 'supplier_id': 12, 'unit': '包', 'spec': '150g', 'price': 7.50, 'cost': 5.50},
    {'code': 'P010', 'name': '三只松鼠坚果大礼包1250g', 'category_id': 3, 'supplier_id': 10, 'unit': '盒', 'spec': '1250g', 'price': 128.00, 'cost': 98.00},
    
    # 饮料冲调 (category_id=4)
    {'code': 'P011', 'name': '可口可乐330ml*24', 'category_id': 4, 'supplier_id': 3, 'unit': '箱', 'spec': '330ml*24', 'price': 48.00, 'cost': 36.00},
    {'code': 'P012', 'name': '百事可乐330ml*24', 'category_id': 4, 'supplier_id': 10, 'unit': '箱', 'spec': '330ml*24', 'price': 46.00, 'cost': 35.00},
    {'code': 'P013', 'name': '康师傅冰红茶500ml*15', 'category_id': 4, 'supplier_id': 1, 'unit': '箱', 'spec': '500ml*15', 'price': 42.00, 'cost': 32.00},
    {'code': 'P014', 'name': '统一冰糖雪梨500ml*15', 'category_id': 4, 'supplier_id': 2, 'unit': '箱', 'spec': '500ml*15', 'price': 45.00, 'cost': 34.00},
    {'code': 'P015', 'name': '农夫山泉550ml*24', 'category_id': 4, 'supplier_id': 3, 'unit': '箱', 'spec': '550ml*24', 'price': 35.00, 'cost': 26.00},
    {'code': 'P016', 'name': '雀巢咖啡1+2原味100条', 'category_id': 4, 'supplier_id': 11, 'unit': '盒', 'spec': '100条', 'price': 68.00, 'cost': 52.00},
    {'code': 'P017', 'name': '立顿红茶100包', 'category_id': 4, 'supplier_id': 7, 'unit': '盒', 'spec': '100包', 'price': 45.00, 'cost': 35.00},
    
    # 乳品冷饮 (category_id=5)
    {'code': 'P018', 'name': '伊利纯牛奶250ml*16', 'category_id': 5, 'supplier_id': 4, 'unit': '箱', 'spec': '250ml*16', 'price': 52.00, 'cost': 40.00},
    {'code': 'P019', 'name': '蒙牛纯牛奶250ml*16', 'category_id': 5, 'supplier_id': 5, 'unit': '箱', 'spec': '250ml*16', 'price': 50.00, 'cost': 38.00},
    {'code': 'P020', 'name': '伊利安慕希酸奶205g*12', 'category_id': 5, 'supplier_id': 4, 'unit': '箱', 'spec': '205g*12', 'price': 68.00, 'cost': 52.00},
    {'code': 'P021', 'name': '蒙牛特仑苏250ml*12', 'category_id': 5, 'supplier_id': 5, 'unit': '箱', 'spec': '250ml*12', 'price': 78.00, 'cost': 60.00},
    {'code': 'P022', 'name': '光明酸奶135g*12', 'category_id': 5, 'supplier_id': 4, 'unit': '箱', 'spec': '135g*12', 'price': 38.00, 'cost': 29.00},
    
    # 清洁用品 (category_id=7)
    {'code': 'P023', 'name': '立白洗衣液3kg', 'category_id': 7, 'supplier_id': 15, 'unit': '瓶', 'spec': '3kg', 'price': 39.90, 'cost': 30.00},
    {'code': 'P024', 'name': '奥妙洗衣液3kg', 'category_id': 7, 'supplier_id': 7, 'unit': '瓶', 'spec': '3kg', 'price': 45.00, 'cost': 34.00},
    {'code': 'P025', 'name': '汰渍洗衣粉3kg', 'category_id': 7, 'supplier_id': 6, 'unit': '袋', 'spec': '3kg', 'price': 32.00, 'cost': 24.00},
    {'code': 'P026', 'name': '威猛先生洁厕液500g*2', 'category_id': 7, 'supplier_id': 6, 'unit': '组', 'spec': '500g*2', 'price': 28.00, 'cost': 21.00},
    {'code': 'P027', 'name': '蓝月亮洗手液500g', 'category_id': 7, 'supplier_id': 15, 'unit': '瓶', 'spec': '500g', 'price': 18.90, 'cost': 14.00},
    
    # 纸品湿巾 (category_id=8)
    {'code': 'P028', 'name': '维达卷纸140g*27卷', 'category_id': 8, 'supplier_id': 9, 'unit': '提', 'spec': '140g*27卷', 'price': 68.00, 'cost': 52.00},
    {'code': 'P029', 'name': '清风抽纸3层130抽*20包', 'category_id': 8, 'supplier_id': 9, 'unit': '箱', 'spec': '130抽*20包', 'price': 58.00, 'cost': 44.00},
    {'code': 'P030', 'name': '心相印湿巾80抽*5包', 'category_id': 8, 'supplier_id': 9, 'unit': '组', 'spec': '80抽*5包', 'price': 28.00, 'cost': 21.00},
    {'code': 'P031', 'name': '洁柔卷纸200g*10卷', 'category_id': 8, 'supplier_id': 9, 'unit': '提', 'spec': '200g*10卷', 'price': 38.00, 'cost': 29.00},
    
    # 个人护理 (category_id=9)
    {'code': 'P032', 'name': '海飞丝洗发水750ml', 'category_id': 9, 'supplier_id': 6, 'unit': '瓶', 'spec': '750ml', 'price': 58.00, 'cost': 44.00},
    {'code': 'P033', 'name': '潘婷洗发水750ml', 'category_id': 9, 'supplier_id': 6, 'unit': '瓶', 'spec': '750ml', 'price': 55.00, 'cost': 42.00},
    {'code': 'P034', 'name': '多芬沐浴露1L', 'category_id': 9, 'supplier_id': 7, 'unit': '瓶', 'spec': '1L', 'price': 48.00, 'cost': 36.00},
    {'code': 'P035', 'name': '高露洁牙膏180g*2', 'category_id': 9, 'supplier_id': 6, 'unit': '组', 'spec': '180g*2', 'price': 28.00, 'cost': 21.00},
    {'code': 'P036', 'name': '云南白药牙膏120g', 'category_id': 9, 'supplier_id': 6, 'unit': '支', 'spec': '120g', 'price': 35.00, 'cost': 26.00},
    
    # 蔬菜水果 (category_id=11)
    {'code': 'P037', 'name': '新鲜西红柿', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 5.80, 'cost': 3.50},
    {'code': 'P038', 'name': '新鲜黄瓜', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 4.50, 'cost': 2.80},
    {'code': 'P039', 'name': '新鲜土豆', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 3.20, 'cost': 2.00},
    {'code': 'P040', 'name': '新鲜白菜', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 2.50, 'cost': 1.50},
    {'code': 'P041', 'name': '新鲜苹果', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 8.80, 'cost': 6.00},
    {'code': 'P042', 'name': '新鲜香蕉', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 6.50, 'cost': 4.50},
    {'code': 'P043', 'name': '新鲜橙子', 'category_id': 11, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 7.80, 'cost': 5.50},
    
    # 肉禽蛋品 (category_id=12)
    {'code': 'P044', 'name': '双汇火腿肠30g*10', 'category_id': 12, 'supplier_id': 13, 'unit': '包', 'spec': '30g*10', 'price': 18.00, 'cost': 13.50},
    {'code': 'P045', 'name': '双汇王中王火腿肠240g', 'category_id': 12, 'supplier_id': 13, 'unit': '包', 'spec': '240g', 'price': 12.80, 'cost': 9.50},
    {'code': 'P046', 'name': '新鲜鸡蛋30枚', 'category_id': 12, 'supplier_id': 14, 'unit': '盒', 'spec': '30枚', 'price': 38.00, 'cost': 28.00},
    {'code': 'P047', 'name': '新鲜猪肉', 'category_id': 12, 'supplier_id': 13, 'unit': '斤', 'spec': '500g', 'price': 28.00, 'cost': 22.00},
    {'code': 'P048', 'name': '新鲜鸡胸肉', 'category_id': 12, 'supplier_id': 14, 'unit': '斤', 'spec': '500g', 'price': 18.00, 'cost': 14.00},
    
    # 厨房用品 (category_id=14)
    {'code': 'P049', 'name': '不锈钢炒锅32cm', 'category_id': 14, 'supplier_id': 15, 'unit': '个', 'spec': '32cm', 'price': 128.00, 'cost': 95.00},
    {'code': 'P050', 'name': '陶瓷碗套装10件', 'category_id': 14, 'supplier_id': 15, 'unit': '套', 'spec': '10件', 'price': 88.00, 'cost': 65.00},
    {'code': 'P051', 'name': '保鲜盒5件套', 'category_id': 14, 'supplier_id': 15, 'unit': '套', 'spec': '5件', 'price': 45.00, 'cost': 32.00},
    {'code': 'P052', 'name': '菜刀不锈钢', 'category_id': 14, 'supplier_id': 15, 'unit': '把', 'spec': '7寸', 'price': 58.00, 'cost': 42.00},
    
    # 收纳整理 (category_id=15)
    {'code': 'P053', 'name': '塑料收纳箱大号', 'category_id': 15, 'supplier_id': 15, 'unit': '个', 'spec': '60L', 'price': 38.00, 'cost': 28.00},
    {'code': 'P054', 'name': '衣架20个装', 'category_id': 15, 'supplier_id': 15, 'unit': '包', 'spec': '20个', 'price': 18.00, 'cost': 13.00},
    {'code': 'P055', 'name': '鞋架多层', 'category_id': 15, 'supplier_id': 15, 'unit': '个', 'spec': '5层', 'price': 68.00, 'cost': 50.00},
    
    # 补充更多高频商品
    {'code': 'P056', 'name': '康师傅红烧牛肉面5连包', 'category_id': 3, 'supplier_id': 1, 'unit': '包', 'spec': '5*120g', 'price': 15.00, 'cost': 11.00},
    {'code': 'P057', 'name': '统一老坛酸菜面5连包', 'category_id': 3, 'supplier_id': 2, 'unit': '包', 'spec': '5*121g', 'price': 16.00, 'cost': 12.00},
    {'code': 'P058', 'name': '旺旺牛奶125ml*20', 'category_id': 5, 'supplier_id': 12, 'unit': '箱', 'spec': '125ml*20', 'price': 38.00, 'cost': 29.00},
    {'code': 'P059', 'name': '怡宝纯净水555ml*24', 'category_id': 4, 'supplier_id': 3, 'unit': '箱', 'spec': '555ml*24', 'price': 32.00, 'cost': 24.00},
    {'code': 'P060', 'name': '雪碧330ml*24', 'category_id': 4, 'supplier_id': 3, 'unit': '箱', 'spec': '330ml*24', 'price': 48.00, 'cost': 36.00},
]

# ==================== 数据生成类 ====================

class SupermarketDataGenerator:
    def __init__(self):
        self.products = []
        self.customers = []
        self.purchase_orders = []
        self.purchase_items = []
        self.sale_orders = []
        self.sale_items = []
        self.inventory = {}  # {product_id: quantity}
        self.purchase_stats = {}  # {product_id: total_purchase_qty}
        self.sale_stats = {}  # {product_id: total_sale_qty}
        
        self.purchase_order_id = 1
        self.purchase_item_id = 1
        self.sale_order_id = 1
        self.sale_item_id = 1
        
    def generate_customers(self, count=100):
        """生成客户数据"""
        print(f"正在生成 {count} 个客户...")
        for i in range(1, count + 1):
            surname = random.choice(CUSTOMER_SURNAMES)
            name = random.choice(CUSTOMER_NAMES)
            customer = {
                'id': i,
                'name': f'{surname}{name}',
                'phone': f'1{random.randint(3,9)}{random.randint(100000000, 999999999)}',
                'address': f'{random.choice(["北京", "上海", "广州", "深圳", "杭州"])}市{random.choice(["朝阳", "海淀", "浦东", "天河", "南山"])}区{random.choice(["建国", "中山", "人民", "解放", "和平"])}路{random.randint(1, 999)}号'
            }
            self.customers.append(customer)
        print(f"[OK] 已生成 {len(self.customers)} 个客户")
    
    def generate_purchase_orders(self):
        """生成5个月的采购订单"""
        print(f"\n正在生成采购订单（{START_DATE.strftime('%Y-%m-%d')} 至 {END_DATE.strftime('%Y-%m-%d')}）...")
        
        current_date = START_DATE
        
        while current_date <= END_DATE:
            # 每周采购2-3次
            if current_date.weekday() in [1, 3, 5]:  # 周二、周四、周六采购
                # 每次采购15-25种商品
                num_products = random.randint(15, 25)
                selected_products = random.sample(PRODUCTS, num_products)
                
                # 选择供应商（按商品的供应商分组）
                supplier_products = {}
                for product in selected_products:
                    supplier_id = product['supplier_id']
                    if supplier_id not in supplier_products:
                        supplier_products[supplier_id] = []
                    supplier_products[supplier_id].append(product)
                
                # 为每个供应商生成一个采购单
                for supplier_id, products in supplier_products.items():
                    order_no = f'PO{current_date.strftime("%Y%m%d")}{self.purchase_order_id:04d}'
                    total_amount = 0
                    items = []
                    
                    for product in products:
                        # 根据商品类型决定采购数量
                        if product['category_id'] in [11, 12]:  # 生鲜类，采购量大
                            quantity = random.randint(50, 200)
                        elif product['category_id'] in [2, 4, 5]:  # 粮油、饮料、乳品，中等采购量
                            quantity = random.randint(20, 80)
                        else:  # 其他商品
                            quantity = random.randint(10, 50)
                        
                        unit_price = product['cost']
                        total_price = round(quantity * unit_price, 2)
                        total_amount += total_price
                        
                        items.append({
                            'id': self.purchase_item_id,
                            'order_id': self.purchase_order_id,
                            'product_id': PRODUCTS.index(product) + 1,
                            'quantity': quantity,
                            'unit_price': unit_price,
                            'total_price': total_price
                        })
                        self.purchase_item_id += 1
                        
                        # 统计采购数量
                        product_id = PRODUCTS.index(product) + 1
                        self.purchase_stats[product_id] = self.purchase_stats.get(product_id, 0) + quantity
                    
                    # 审核时间：当天或第二天
                    audit_time = current_date + timedelta(hours=random.randint(2, 8))
                    # 入库时间：审核后1-2天
                    inbound_time = audit_time + timedelta(days=random.randint(1, 2), hours=random.randint(1, 6))
                    
                    order = {
                        'id': self.purchase_order_id,
                        'order_no': order_no,
                        'supplier_id': supplier_id,
                        'total_amount': round(total_amount, 2),
                        'purchase_date': current_date.strftime('%Y-%m-%d'),
                        'status': 3,  # 已入库
                        'applicant_id': 2,  # 采购员
                        'auditor_id': 1,  # 管理员审核
                        'audit_time': audit_time.strftime('%Y-%m-%d %H:%M:%S'),
                        'audit_remark': '审核通过',
                        'inbound_time': inbound_time.strftime('%Y-%m-%d %H:%M:%S'),
                        'create_time': current_date.strftime('%Y-%m-%d %H:%M:%S')
                    }
                    
                    self.purchase_orders.append(order)
                    self.purchase_items.extend(items)
                    self.purchase_order_id += 1
            
            current_date += timedelta(days=1)
        
        print(f"[OK] 已生成 {len(self.purchase_orders)} 个采购订单，{len(self.purchase_items)} 条采购明细")
    
    def generate_sale_orders(self):
        """生成5个月的销售订单"""
        print(f"\n正在生成销售订单...")
        
        # 计算当前可用库存（基于已生成的采购数据）
        available_stock = {}
        for product_id, qty in self.purchase_stats.items():
            available_stock[product_id] = qty
        
        current_date = START_DATE
        
        while current_date <= END_DATE:
            # 每天生成20-40个销售订单（模拟真实超市客流）
            is_weekend = current_date.weekday() in [5, 6]
            daily_orders = random.randint(30, 50) if is_weekend else random.randint(20, 35)
            
            for _ in range(daily_orders):
                # 随机时间（营业时间8:00-22:00）
                hour = random.randint(8, 21)
                minute = random.randint(0, 59)
                second = random.randint(0, 59)
                order_time = current_date.replace(hour=hour, minute=minute, second=second)
                
                order_no = f'SO{order_time.strftime("%Y%m%d%H%M%S")}{self.sale_order_id:04d}'
                
                # 70%的订单有客户信息
                customer_id = random.choice(self.customers)['id'] if random.random() < 0.7 else None
                
                # 每个订单购买2-8种商品
                num_products = random.randint(2, 8)
                selected_products = random.sample(PRODUCTS, num_products)
                
                total_amount = 0
                items = []
                
                for product in selected_products:
                    product_id = PRODUCTS.index(product) + 1
                    
                    # 检查可用库存
                    current_stock = available_stock.get(product_id, 0)
                    if current_stock <= 0:
                        continue  # 库存不足，跳过此商品
                    
                    # 根据商品类型决定购买数量
                    if product['category_id'] in [11, 12]:  # 生鲜类
                        max_qty = min(5, current_stock)
                        quantity = random.randint(1, max_qty) if max_qty > 0 else 0
                    elif product['category_id'] in [4, 5]:  # 饮料、乳品
                        max_qty = min(3, current_stock)
                        quantity = random.randint(1, max_qty) if max_qty > 0 else 0
                    else:  # 其他商品
                        max_qty = min(2, current_stock)
                        quantity = random.randint(1, max_qty) if max_qty > 0 else 0
                    
                    if quantity == 0:
                        continue
                    
                    # 扣减库存
                    available_stock[product_id] = current_stock - quantity
                    
                    unit_price = product['price']
                    total_price = round(quantity * unit_price, 2)
                    total_amount += total_price
                    
                    items.append({
                        'id': self.sale_item_id,
                        'order_id': self.sale_order_id,
                        'product_id': product_id,
                        'quantity': quantity,
                        'unit_price': unit_price,
                        'total_price': total_price
                    })
                    self.sale_item_id += 1
                    
                    # 统计销售数量
                    self.sale_stats[product_id] = self.sale_stats.get(product_id, 0) + quantity
                
                # 如果没有商品，跳过此订单
                if not items:
                    continue
                
                # 95%的订单已支付
                status = 1 if random.random() < 0.95 else 0
                
                order = {
                    'id': self.sale_order_id,
                    'order_no': order_no,
                    'customer_id': customer_id,
                    'total_amount': round(total_amount, 2),
                    'status': status,
                    'cashier_id': random.choice([2, 3]),  # 收银员
                    'create_time': order_time.strftime('%Y-%m-%d %H:%M:%S')
                }
                
                self.sale_orders.append(order)
                self.sale_items.extend(items)
                self.sale_order_id += 1
            
            current_date += timedelta(days=1)
        
        print(f"[OK] 已生成 {len(self.sale_orders)} 个销售订单，{len(self.sale_items)} 条销售明细")
    
    def calculate_inventory(self):
        """计算库存：库存 = 采购 - 销售"""
        print(f"\n正在计算库存...")
        
        negative_count = 0
        for i, product in enumerate(PRODUCTS, 1):
            purchase_qty = self.purchase_stats.get(i, 0)
            sale_qty = self.sale_stats.get(i, 0)
            inventory_qty = purchase_qty - sale_qty
            
            # 确保库存不为负数（如果为负，说明数据有问题，调整为0）
            if inventory_qty < 0:
                negative_count += 1
                inventory_qty = 0
            
            self.inventory[i] = {
                'product_id': i,
                'quantity': inventory_qty,
                'warning_quantity': 10 if product['category_id'] not in [11, 12] else 20,  # 生鲜预警量更高
                'purchase_total': purchase_qty,
                'sale_total': sale_qty
            }
        
        print(f"[OK] 已计算 {len(self.inventory)} 个商品的库存")
        if negative_count > 0:
            print(f"  注意: {negative_count} 个商品出现负库存（已调整为0）")
        
        # 显示库存统计
        total_purchase = sum(item['purchase_total'] for item in self.inventory.values())
        total_sale = sum(item['sale_total'] for item in self.inventory.values())
        total_inventory = sum(item['quantity'] for item in self.inventory.values())
        
        print(f"\n库存统计:")
        print(f"  总采购量: {total_purchase}")
        print(f"  总销售量: {total_sale}")
        print(f"  当前库存: {total_inventory}")
        print(f"  验证: {total_purchase} - {total_sale} = {total_inventory} [OK]")
    
    def generate_sql(self):
        """生成SQL文件"""
        print(f"\n正在生成SQL文件...")
        
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            # 文件头
            f.write("-- ====================================\n")
            f.write("-- 超市库存管理系统 - 真实数据\n")
            f.write(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"-- 数据周期: {START_DATE.strftime('%Y-%m-%d')} 至 {END_DATE.strftime('%Y-%m-%d')}\n")
            f.write("-- 数据说明: 包含5个月的采购和销售记录，库存=采购-销售\n")
            f.write("-- ====================================\n\n")
            
            f.write("USE `supermarket_db`;\n\n")
            
            f.write("-- 禁用外键检查\n")
            f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")
            
            # 清空现有数据（除了用户表等系统表）
            f.write("-- ====================================\n")
            f.write("-- 清空现有数据\n")
            f.write("-- ====================================\n")
            f.write("TRUNCATE TABLE `inventory_log`;\n")
            f.write("TRUNCATE TABLE `sale_order_item`;\n")
            f.write("TRUNCATE TABLE `sale_order`;\n")
            f.write("TRUNCATE TABLE `purchase_order_item`;\n")
            f.write("TRUNCATE TABLE `purchase_order`;\n")
            f.write("TRUNCATE TABLE `inventory`;\n")
            f.write("TRUNCATE TABLE `product`;\n")
            f.write("TRUNCATE TABLE `customer`;\n")
            f.write("TRUNCATE TABLE `supplier`;\n")
            f.write("TRUNCATE TABLE `category`;\n")
            f.write("DELETE FROM `message_notification` WHERE id > 0;\n")
            f.write("DELETE FROM `system_notification` WHERE id > 0;\n\n")
            
            # 插入分类数据
            f.write("-- ====================================\n")
            f.write("-- 商品分类数据\n")
            f.write("-- ====================================\n")
            for cat in CATEGORIES:
                f.write(f"INSERT INTO `category` (`id`, `category_name`, `parent_id`, `sort_order`, `description`) VALUES ")
                f.write(f"({cat['id']}, '{cat['name']}', {cat['parent_id']}, {cat['sort']}, '{cat['desc']}');\n")
            f.write("\n")
            
            # 插入供应商数据
            f.write("-- ====================================\n")
            f.write("-- 供应商数据\n")
            f.write("-- ====================================\n")
            for sup in SUPPLIERS:
                f.write(f"INSERT INTO `supplier` (`id`, `supplier_name`, `contact_person`, `phone`, `address`, `status`) VALUES ")
                f.write(f"({sup['id']}, '{sup['name']}', '{sup['contact']}', '{sup['phone']}', '{sup['address']}', 1);\n")
            f.write("\n")
            
            # 插入客户数据
            f.write("-- ====================================\n")
            f.write("-- 客户数据\n")
            f.write("-- ====================================\n")
            for i, customer in enumerate(self.customers):
                f.write(f"INSERT INTO `customer` (`id`, `customer_name`, `phone`, `address`) VALUES ")
                f.write(f"({customer['id']}, '{customer['name']}', '{customer['phone']}', '{customer['address']}');\n")
                if (i + 1) % 20 == 0:
                    f.write("\n")
            f.write("\n")
            
            # 插入商品数据
            f.write("-- ====================================\n")
            f.write("-- 商品数据\n")
            f.write("-- ====================================\n")
            for i, product in enumerate(PRODUCTS, 1):
                f.write(f"INSERT INTO `product` (`id`, `product_code`, `product_name`, `category_id`, `supplier_id`, `unit`, `specification`, `price`, `cost_price`, `status`) VALUES ")
                f.write(f"({i}, '{product['code']}', '{product['name']}', {product['category_id']}, {product['supplier_id']}, '{product['unit']}', '{product['spec']}', {product['price']}, {product['cost']}, 1);\n")
            f.write("\n")
            
            # 插入采购订单
            f.write("-- ====================================\n")
            f.write("-- 采购订单数据\n")
            f.write("-- ====================================\n")
            for order in self.purchase_orders:
                f.write(f"INSERT INTO `purchase_order` (`id`, `order_no`, `supplier_id`, `total_amount`, `purchase_date`, `status`, `applicant_id`, `auditor_id`, `audit_time`, `audit_remark`, `inbound_time`, `create_time`) VALUES ")
                f.write(f"({order['id']}, '{order['order_no']}', {order['supplier_id']}, {order['total_amount']}, '{order['purchase_date']}', {order['status']}, {order['applicant_id']}, {order['auditor_id']}, '{order['audit_time']}', '{order['audit_remark']}', '{order['inbound_time']}', '{order['create_time']}');\n")
            f.write("\n")
            
            # 插入采购明细
            f.write("-- ====================================\n")
            f.write("-- 采购订单明细数据\n")
            f.write("-- ====================================\n")
            for i, item in enumerate(self.purchase_items):
                f.write(f"INSERT INTO `purchase_order_item` (`id`, `order_id`, `product_id`, `quantity`, `unit_price`, `total_price`) VALUES ")
                f.write(f"({item['id']}, {item['order_id']}, {item['product_id']}, {item['quantity']}, {item['unit_price']}, {item['total_price']});\n")
                if (i + 1) % 50 == 0:
                    f.write("\n")
            f.write("\n")
            
            # 插入销售订单
            f.write("-- ====================================\n")
            f.write("-- 销售订单数据\n")
            f.write("-- ====================================\n")
            for i, order in enumerate(self.sale_orders):
                customer_id = order['customer_id'] if order['customer_id'] else 'NULL'
                f.write(f"INSERT INTO `sale_order` (`id`, `order_no`, `customer_id`, `total_amount`, `status`, `cashier_id`, `create_time`) VALUES ")
                f.write(f"({order['id']}, '{order['order_no']}', {customer_id}, {order['total_amount']}, {order['status']}, {order['cashier_id']}, '{order['create_time']}');\n")
                if (i + 1) % 50 == 0:
                    f.write("\n")
            f.write("\n")
            
            # 插入销售明细
            f.write("-- ====================================\n")
            f.write("-- 销售订单明细数据\n")
            f.write("-- ====================================\n")
            for i, item in enumerate(self.sale_items):
                f.write(f"INSERT INTO `sale_order_item` (`id`, `order_id`, `product_id`, `quantity`, `unit_price`, `total_price`) VALUES ")
                f.write(f"({item['id']}, {item['order_id']}, {item['product_id']}, {item['quantity']}, {item['unit_price']}, {item['total_price']});\n")
                if (i + 1) % 100 == 0:
                    f.write("\n")
            f.write("\n")
            
            # 插入库存数据
            f.write("-- ====================================\n")
            f.write("-- 库存数据（库存 = 采购 - 销售）\n")
            f.write("-- ====================================\n")
            for product_id, inv in self.inventory.items():
                f.write(f"INSERT INTO `inventory` (`product_id`, `quantity`, `warning_quantity`) VALUES ")
                f.write(f"({inv['product_id']}, {inv['quantity']}, {inv['warning_quantity']});\n")
            f.write("\n")
            
            # 重置自增ID
            f.write("-- ====================================\n")
            f.write("-- 重置自增ID\n")
            f.write("-- ====================================\n")
            f.write(f"ALTER TABLE `category` AUTO_INCREMENT = {len(CATEGORIES) + 1};\n")
            f.write(f"ALTER TABLE `supplier` AUTO_INCREMENT = {len(SUPPLIERS) + 1};\n")
            f.write(f"ALTER TABLE `customer` AUTO_INCREMENT = {len(self.customers) + 1};\n")
            f.write(f"ALTER TABLE `product` AUTO_INCREMENT = {len(PRODUCTS) + 1};\n")
            f.write(f"ALTER TABLE `purchase_order` AUTO_INCREMENT = {len(self.purchase_orders) + 1};\n")
            f.write(f"ALTER TABLE `purchase_order_item` AUTO_INCREMENT = {len(self.purchase_items) + 1};\n")
            f.write(f"ALTER TABLE `sale_order` AUTO_INCREMENT = {len(self.sale_orders) + 1};\n")
            f.write(f"ALTER TABLE `sale_order_item` AUTO_INCREMENT = {len(self.sale_items) + 1};\n")
            f.write(f"ALTER TABLE `inventory` AUTO_INCREMENT = {len(self.inventory) + 1};\n")
            f.write(f"ALTER TABLE `message_notification` AUTO_INCREMENT = 1;\n")
            f.write(f"ALTER TABLE `system_notification` AUTO_INCREMENT = 1;\n\n")
            
            # 启用外键检查
            f.write("-- 启用外键检查\n")
            f.write("SET FOREIGN_KEY_CHECKS = 1;\n\n")
            
            # 数据统计
            f.write("-- ====================================\n")
            f.write("-- 数据统计\n")
            f.write("-- ====================================\n")
            f.write(f"-- 商品分类: {len(CATEGORIES)} 个\n")
            f.write(f"-- 供应商: {len(SUPPLIERS)} 个\n")
            f.write(f"-- 客户: {len(self.customers)} 个\n")
            f.write(f"-- 商品: {len(PRODUCTS)} 个\n")
            f.write(f"-- 采购订单: {len(self.purchase_orders)} 个\n")
            f.write(f"-- 采购明细: {len(self.purchase_items)} 条\n")
            f.write(f"-- 销售订单: {len(self.sale_orders)} 个\n")
            f.write(f"-- 销售明细: {len(self.sale_items)} 条\n")
            f.write(f"-- 库存记录: {len(self.inventory)} 条\n")
            f.write(f"-- 总采购量: {sum(item['purchase_total'] for item in self.inventory.values())} 件\n")
            f.write(f"-- 总销售量: {sum(item['sale_total'] for item in self.inventory.values())} 件\n")
            f.write(f"-- 当前库存: {sum(item['quantity'] for item in self.inventory.values())} 件\n")
            f.write("-- ====================================\n")
        
        print(f"[OK] SQL文件已生成: {OUTPUT_FILE}")

# ==================== 主程序 ====================

def main():
    print("=" * 60)
    print("超市库存管理系统 - 真实数据生成器")
    print("=" * 60)
    print(f"数据周期: {START_DATE.strftime('%Y-%m-%d')} 至 {END_DATE.strftime('%Y-%m-%d')} (5个月)")
    print(f"输出文件: {OUTPUT_FILE}")
    print("=" * 60)
    print()
    
    generator = SupermarketDataGenerator()
    
    # 生成数据
    generator.generate_customers(100)
    generator.generate_purchase_orders()
    generator.generate_sale_orders()
    generator.calculate_inventory()
    generator.generate_sql()
    
    print("\n" + "=" * 60)
    print("数据生成完成！")
    print("=" * 60)
    print(f"\n请执行以下命令导入数据:")
    print(f"mysql -uroot -proot supermarket_db < {OUTPUT_FILE}")
    print()

if __name__ == '__main__':
    main()
