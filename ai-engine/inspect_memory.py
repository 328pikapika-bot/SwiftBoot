
import chromadb
import os
import time

# 配置
PERSIST_DIRECTORY = os.path.join(os.path.dirname(__file__), "chroma_db")
MEMORY_COLLECTION_NAME = "swiftboot_chat_memory"
CODEBASE_COLLECTION_NAME = "swiftboot_codebase"

def inspect():
    client = chromadb.PersistentClient(path=PERSIST_DIRECTORY)
    
    print(f"--- 检查记忆库: {MEMORY_COLLECTION_NAME} ---")
    try:
        memory_col = client.get_collection(name=MEMORY_COLLECTION_NAME)
        count = memory_col.count()
        print(f"总记录数: {count}")
        
        if count > 0:
            # 获取最近的 20 条记录
            results = memory_col.get(limit=20, include=['documents', 'metadatas'])
            for i, doc in enumerate(results['documents']):
                meta = results['metadatas'][i]
                role = meta.get('role', 'unknown')
                user_id = meta.get('user_id', 'unknown')
                print(f"[{i+1}] 用户: {user_id} | 角色: {role}")
                print(f"内容: {doc[:100]}...")
                print("-" * 20)
        else:
            print("记忆库为空。")
    except Exception as e:
        print(f"读取记忆库失败: {e}")

    print(f"\n--- 检查代码库: {CODEBASE_COLLECTION_NAME} ---")
    try:
        code_col = client.get_collection(name=CODEBASE_COLLECTION_NAME)
        count = code_col.count()
        print(f"总记录数: {count}")
        
        if count > 0:
            # 获取一些示例
            results = code_col.get(limit=5, include=['documents', 'metadatas'])
            for i, doc in enumerate(results['documents']):
                meta = results['metadatas'][i]
                name = meta.get('name', 'unknown')
                source = meta.get('source', 'unknown')
                print(f"[{i+1}] 文件: {source} | 名称: {name}")
                # print(f"内容摘要: {doc[:50]}...")
        else:
            print("代码库为空。")
    except Exception as e:
        print(f"读取代码库失败: {e}")

if __name__ == "__main__":
    inspect()
