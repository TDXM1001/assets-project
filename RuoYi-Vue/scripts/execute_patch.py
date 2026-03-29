import pymysql
import os

# 数据库连接配置 (从 application-druid.yml 提取)
db_config = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'my-assets',
    'charset': 'utf8mb4'
}

sql_file_path = 'sql/asset_patch_20260329_asset_type_foundation.sql'

def execute_sql_file(file_path):
    if not os.path.exists(file_path):
        print(f"Error: SQL file {file_path} not found.")
        return

    try:
        connection = pymysql.connect(**db_config)
        print("Connected to MySQL successfully.")
        
        with connection.cursor() as cursor:
            # 读取 SQL 文件内容并按分号分割 (若依 Patch 通常包含多条语句)
            with open(file_path, 'r', encoding='utf-8') as f:
                sql_content = f.read()
            
            # 基础分割 (这里需处理注释与多行语句，简化处理忽略空行与前导注释)
            statements = [s.strip() for s in sql_content.split(';') if s.strip()]
            
            for statement in statements:
                try:
                    cursor.execute(statement)
                    print(f"Executed success: {statement[:50]}...")
                except Exception as e:
                    print(f"Warning during execution: {e}")
            
            connection.commit()
            print("--- SQL injection completed successfully! ---")

    except Exception as e:
        print(f"Failed to execute SQL via Python: {e}")
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()

if __name__ == "__main__":
    execute_sql_file(sql_file_path)
