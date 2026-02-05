import chromadb
import os
from knowledge_ingest import JavaParser
from typing import List, Dict
import uuid

# ==========================================
# 向量数据库配置
# ==========================================
# 这里的路径决定了数据库文件存在哪里。
# 我们设置为当前脚本目录下的 "chroma_db" 文件夹。
# 这就是"本地部署"，不需要安装额外的服务器软件。
PERSIST_DIRECTORY = os.path.join(os.path.dirname(__file__), "chroma_db")
COLLECTION_NAME = "swiftboot_codebase"

class VectorStore:
    def __init__(self):
        print(f"正在初始化向量数据库，存储路径: {PERSIST_DIRECTORY}")
        # 初始化 ChromaDB 客户端
        # PersistentClient 会自动把数据保存到磁盘，类似 SQLite
        self.client = chromadb.PersistentClient(path=PERSIST_DIRECTORY)
        
        # 获取或创建集合（类似于 SQL 中的 Table）
        # ChromaDB 默认使用 all-MiniLM-L6-v2 模型进行文本向量化
        self.collection = self.client.get_or_create_collection(name=COLLECTION_NAME)
        print("数据库连接成功！")

    def add_documents(self, chunks: List[Dict]):
        """
        将代码块存入数据库
        """
        if not chunks:
            print("没有数据需要存储。")
            return
            
        print(f"正在准备存储 {len(chunks)} 个代码块...")
        
        ids = []
        documents = []
        metadatas = []
        
        for chunk in chunks:
            # 生成唯一ID
            ids.append(str(uuid.uuid4()))
            # 存入主要文本内容（用于检索）
            documents.append(chunk['content'])
            # 存入元数据（用于过滤或展示信息）
            metadatas.append({
                'name': chunk['name'], 
                'type': chunk['type'], 
                'source': chunk.get('source', 'unknown')
            })
        
        # 批量写入
        # ChromaDB 会在这里自动调用 Embedding 模型，把 documents 变成向量
        self.collection.add(
            documents=documents,
            metadatas=metadatas,
            ids=ids
        )
        print(f"成功！已将数据持久化保存到: {PERSIST_DIRECTORY}")

    def query(self, query_text: str, n_results: int = 3):
        """
        检索最相似的代码块
        """
        print(f"正在检索问题: [{query_text}]")
        results = self.collection.query(
            query_texts=[query_text],
            n_results=n_results
        )
        return results

    def delete_by_source(self, source_path: str):
        """
        根据源文件路径删除相关的向量数据
        """
        try:
            # 1. 先查询是否存在（为了打印日志，ChromaDB 的 delete 默认如果不匹配也不会报错）
            existing = self.collection.get(where={"source": source_path})
            count = len(existing['ids']) if existing and existing['ids'] else 0
            
            if count > 0:
                print(f"检测到旧数据，正在删除 {count} 条来自 {source_path} 的记录...")
                self.collection.delete(where={"source": source_path})
                print("旧数据删除成功。")
            else:
                print(f"未发现来自 {source_path} 的旧数据，将直接新增。")
        except Exception as e:
            print(f"删除旧数据时出错: {str(e)}")

if __name__ == "__main__":
    # 1. 初始化数据库
    vector_store = VectorStore()
    
    # 2. 解析代码文件
    parser = JavaParser()
    target_file = r"d:\study\SwiftBoot\swiftboot-backend\swiftboot-admin\src\main\java\com\swiftboot\admin\controller\SysAiController.java"
    
    if os.path.exists(target_file):
        print(f"\n--- 第一步：解析文件 ---")
        chunks = parser.parse_file(target_file)
        
        # 补充来源信息
        for chunk in chunks:
            chunk['source'] = "SysAiController.java"
            
        # 3. 存入数据库
        print(f"\n--- 第二步：存入数据库 ---")
        vector_store.add_documents(chunks)
        
        # 4. 测试检索效果
        print(f"\n--- 第三步：测试检索 ---")
        test_query = "流式输出是怎么实现的？"
        results = vector_store.query(test_query)
        
        print("\n=== 检索结果 ===")
        if results['documents']:
            for i, doc in enumerate(results['documents'][0]):
                meta = results['metadatas'][0][i]
                print(f"\n[结果 {i+1}] 来源: {meta['name']} ({meta['type']})")
                print(f"内容预览: {doc[:100].replace(chr(10), ' ')}...")
        else:
            print("未找到相关结果")
            
    else:
        print(f"测试文件不存在: {target_file}")
