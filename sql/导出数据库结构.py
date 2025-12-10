#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
超市库存管理系统 - 数据库结构导出工具
使用Python连接MySQL数据库并导出表结构
"""

import pymysql
from datetime import datetime
import os

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'supermarket_db',
    'charset': 'utf8mb4'
}

def export_database_structure():
    """导出数据库结构到SQL文件"""
    try:
        # 连接数据库
        print("正在连接数据库...")
        connection = pymysql.connect(**DB_CONFIG)
        cursor = connection.cursor()
        
        # 生成输出文件名
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_file = f'supermarket_db_structure_{timestamp}.sql'
        
        print(f"数据库: {DB_CONFIG['database']}")
        print(f"输出文件: {output_file}")
        print()
        
        with open(output_file, 'w', encoding='utf-8') as f:
            # 写入文件头
            f.write(f"-- ====================================\n")
            f.write(f"-- 超市库存管理系统 - 数据库结构\n")
            f.write(f"-- 导出时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"-- 数据库: {DB_CONFIG['database']}\n")
            f.write(f"-- ====================================\n\n")
            
            f.write(f"CREATE DATABASE IF NOT EXISTS `{DB_CONFIG['database']}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n")
            f.write(f"USE `{DB_CONFIG['database']}`;\n\n")
            
            # 获取所有表名
            cursor.execute("SHOW TABLES")
            tables = cursor.fetchall()
            
            print(f"找到 {len(tables)} 个表，正在导出...")
            print()
            
            # 遍历每个表
            for (table_name,) in tables:
                print(f"正在导出表: {table_name}")
                
                # 获取建表语句
                cursor.execute(f"SHOW CREATE TABLE `{table_name}`")
                create_table = cursor.fetchone()[1]
                
                f.write(f"-- ====================================\n")
                f.write(f"-- 表结构: {table_name}\n")
                f.write(f"-- ====================================\n")
                f.write(f"DROP TABLE IF EXISTS `{table_name}`;\n")
                f.write(f"{create_table};\n\n")
                
                # 获取表注释和字段信息
                cursor.execute(f"""
                    SELECT 
                        COLUMN_NAME as '字段名',
                        COLUMN_TYPE as '类型',
                        IS_NULLABLE as '允许空',
                        COLUMN_KEY as '键',
                        COLUMN_DEFAULT as '默认值',
                        COLUMN_COMMENT as '注释'
                    FROM information_schema.COLUMNS 
                    WHERE TABLE_SCHEMA = '{DB_CONFIG['database']}' 
                    AND TABLE_NAME = '{table_name}'
                    ORDER BY ORDINAL_POSITION
                """)
                
                columns = cursor.fetchall()
                f.write(f"-- 字段说明:\n")
                for col in columns:
                    col_name, col_type, nullable, key, default, comment = col
                    f.write(f"-- {col_name}: {col_type}")
                    if key == 'PRI':
                        f.write(" [主键]")
                    elif key == 'UNI':
                        f.write(" [唯一]")
                    elif key == 'MUL':
                        f.write(" [索引]")
                    if comment:
                        f.write(f" - {comment}")
                    f.write("\n")
                f.write("\n")
            
            # 获取所有视图
            cursor.execute("""
                SELECT TABLE_NAME 
                FROM information_schema.VIEWS 
                WHERE TABLE_SCHEMA = %s
            """, (DB_CONFIG['database'],))
            views = cursor.fetchall()
            
            if views:
                print()
                print(f"找到 {len(views)} 个视图，正在导出...")
                print()
                
                for (view_name,) in views:
                    print(f"正在导出视图: {view_name}")
                    
                    cursor.execute(f"SHOW CREATE VIEW `{view_name}`")
                    create_view = cursor.fetchone()[1]
                    
                    f.write(f"-- ====================================\n")
                    f.write(f"-- 视图: {view_name}\n")
                    f.write(f"-- ====================================\n")
                    f.write(f"DROP VIEW IF EXISTS `{view_name}`;\n")
                    f.write(f"{create_view};\n\n")
        
        cursor.close()
        connection.close()
        
        print()
        print("====================================")
        print("导出成功！")
        print(f"文件位置: {os.path.abspath(output_file)}")
        print("====================================")
        
    except pymysql.Error as e:
        print()
        print("====================================")
        print(f"数据库错误: {e}")
        print("请检查：")
        print("1. 数据库连接信息是否正确")
        print("2. 数据库是否存在")
        print("3. 用户是否有足够的权限")
        print("====================================")
    except Exception as e:
        print()
        print("====================================")
        print(f"发生错误: {e}")
        print("====================================")

if __name__ == '__main__':
    print("====================================")
    print("超市库存管理系统 - 数据库结构导出工具")
    print("====================================")
    print()
    export_database_structure()
    print()
    input("按回车键退出...")
