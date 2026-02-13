import shutil
import os
import time

# ==========================================
# 配置
# ==========================================
# 向量数据库路径
DB_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")
# 状态文件路径
STATE_FILE = os.path.join(os.path.dirname(__file__), "file_state.json")

def clean_db():
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 开始清理向量数据库...")
    
    # 1. 清理 chroma_db 目录
    if os.path.exists(DB_DIR):
        try:
            shutil.rmtree(DB_DIR)
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 已删除向量数据库目录: {DB_DIR}")
        except Exception as e:
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 删除目录失败: {e}")
            print("提示：请确保没有其他程序（如 file_watcher.py）正在占用该目录。")
    else:
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 目录不存在，无需清理: {DB_DIR}")

    # 2. 清理 file_state.json
    if os.path.exists(STATE_FILE):
        try:
            os.remove(STATE_FILE)
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 已删除状态文件: {STATE_FILE}")
        except Exception as e:
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 删除文件失败: {e}")
    else:
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 状态文件不存在，无需清理: {STATE_FILE}")

    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 清理完成！")

if __name__ == "__main__":
    clean_db()
