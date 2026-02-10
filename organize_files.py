import os
import shutil

# 定义目标目录
base_archive = 'docs/archive'
dirs = {
    'scripts': os.path.join(base_archive, 'scripts'),
    'sql': os.path.join(base_archive, 'sql'),
    'thesis': os.path.join(base_archive, 'thesis'),
    'dev_notes': os.path.join(base_archive, 'dev-notes')
}

# 必须保留在根目录的文件
keep_in_root = [
    'README.md',
    'DEPLOYMENT_GUIDE.md',
    'PRE_DEPLOYMENT_CHECKLIST.md',
    '数据库迁移指南.md',
    '数据库导入验证指南.md',
    '快速使用指南.md',
    'pom.xml',
    '.gitignore'
]

def organize():
    # 确保目录存在
    for d in dirs.values():
        if not os.path.exists(d):
            os.makedirs(d)

    for filename in os.listdir('.'):
        if not os.path.isfile(filename) or filename in keep_in_root:
            continue

        ext = os.path.splitext(filename)[1].lower()
        
        # 1. 脚本类
        if ext in ['.ps1', '.py', '.sh', '.bat', '.vba'] or 'vba' in filename.lower():
            shutil.move(filename, os.path.join(dirs['scripts'], filename))
            print(f'Moved script: {filename}')
            
        # 2. SQL类
        elif ext == '.sql':
            shutil.move(filename, os.path.join(dirs['sql'], filename))
            print(f'Moved sql: {filename}')
            
        # 3. 论文类 (包含 docx 和 明确的论文过程稿)
        elif ext == '.docx' or '论文' in filename or '开题报告' in filename:
            shutil.move(filename, os.path.join(dirs['thesis'], filename))
            print(f'Moved thesis: {filename}')
            
        # 4. 其他 MD/TXT 过程文档
        elif ext in ['.md', '.txt']:
            shutil.move(filename, os.path.join(dirs['dev_notes'], filename))
            print(f'Moved note: {filename}')

if __name__ == '__main__':
    organize()

