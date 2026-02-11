import chromadb
import os
import time
from knowledge_ingest import JavaParser
from typing import List, Dict
import uuid
import requests
import yaml

# ==========================================
# 配置加载
# ==========================================
# 尝试读取后端的 application-dev.yml
CONFIG_FILE = os.path.abspath(os.path.join(os.path.dirname(__file__), "../swiftboot-backend/swiftboot-admin/src/main/resources/application-dev.yml"))

def load_config():
    """
    加载配置文件
    """
    default_config = {
        "backend": {
            "api_url": "http://localhost:8080/monitor/operlog/inner/add"
        },
        "vector_db": {
            "persist_directory": os.path.join(os.path.dirname(__file__), "chroma_db"),
            "collection_name": "swiftboot_codebase",
            "memory_collection_name": "swiftboot_chat_memory"
        }
    }
     
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                app_config = yaml.safe_load(f)
                
                # 解析 ai.engine 配置
                if 'ai' in app_config and 'engine' in app_config['ai']:
                    engine_config = app_config['ai']['engine']
                    
                    if 'backend-log-api' in engine_config:
                        default_config["backend"]["api_url"] = engine_config['backend-log-api']
                        
                    if 'vector-db-path' in engine_config:
                        # 如果配置的是相对路径，则相对于当前脚本目录
                        path = engine_config['vector-db-path']
                        if path.startswith("./"):
                            default_config["vector_db"]["persist_directory"] = os.path.join(os.path.dirname(__file__), path)
                        else:
                            default_config["vector_db"]["persist_directory"] = path
                            
                    if 'collection-name' in engine_config:
                        default_config["vector_db"]["collection_name"] = engine_config['collection-name']
                        
                    if 'memory-collection-name' in engine_config:
                        default_config["vector_db"]["memory_collection_name"] = engine_config['memory-collection-name']
                        
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 成功加载后端配置文件: {CONFIG_FILE}")
        except Exception as e:
            print(f"加载配置文件失败: {e}，将使用默认配置")
    else:
        print(f"配置文件不存在: {CONFIG_FILE}，将使用默认配置")
        
    return default_config

config = load_config()

# ==========================================
# 向量数据库配置
# ==========================================
PERSIST_DIRECTORY = config["vector_db"]["persist_directory"]
COLLECTION_NAME = config["vector_db"]["collection_name"]
MEMORY_COLLECTION_NAME = config["vector_db"]["memory_collection_name"]
BACKEND_LOG_API = config["backend"]["api_url"]

class VectorStore:
    """
    代码库向量存储管理类
    负责处理项目代码的向量化存储与检索
    """
    def __init__(self):
        print(f"[{self._now()}]正在初始化向量数据库，存储路径: {PERSIST_DIRECTORY}")
        # 初始化 ChromaDB 客户端
        # PersistentClient 会自动把数据保存到磁盘，类似 SQLite
        self.client = chromadb.PersistentClient(path=PERSIST_DIRECTORY)
        
        # 获取或创建集合（类似于 SQL 中的 Table）
        # ChromaDB 默认使用 all-MiniLM-L6-v2 模型进行文本向量化
        self.collection = self.client.get_or_create_collection(name=COLLECTION_NAME)
        print(f"[{self._now()}]数据库连接成功！")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

    def _log_to_backend(self, title: str, oper_name: str, status: int = 0):
        """
        调用后端 API 记录操作日志
        """
        try:
            payload = {
                "title": title,
                "businessType": 0, # 其他
                "method": "VectorStore.update",
                "requestMethod": "POST",
                "operName": oper_name,
                "operUrl": "/inner/rag/sync",
                "operIp": "127.0.0.1",
                "status": status,
                "operTime": time.strftime("%Y-%m-%d %H:%M:%S")
            }
            # 这里需要注意，后端接口可能需要鉴权，或者是一个内部接口
            # 为了简单起见，假设后端提供了一个无需鉴权的内部接口用于系统调用
            # 或者通过添加特定的 header 来通过鉴权
            response = requests.post(BACKEND_LOG_API, json=payload, timeout=2)
            if response.status_code != 200:
                print(f"[{self._now()}]记录日志失败，后端返回: {response.status_code} - {response.text}")
            else:
                # print(f"[{self._now()}]操作日志已同步到后端。")
                pass
        except Exception as e:
            print(f"[{self._now()}]记录日志失败: {e}")

    def add_documents(self, chunks: List[Dict]):
        """
        将解析后的代码块存入向量数据库
        :param chunks: 代码块列表，包含 content, name, type, source 等信息
        """
        if not chunks:
            print(f"[{self._now()}]没有数据需要存储。")
            return
            
        print(f"[{self._now()}]正在准备存储 {len(chunks)} 个代码块...")
        
        ids = []
        documents = []
        metadatas = []
        
        for chunk in chunks:
            # 生成唯一ID (UUID v4)
            ids.append(str(uuid.uuid4()))
            # 存入主要文本内容（ChromaDB 会自动将其转为 384 维向量）
            documents.append(chunk['content'])
            # 存入元数据（用于后续的过滤或前端展示）
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
        print(f"[{self._now()}]成功！已将数据持久化保存到: {PERSIST_DIRECTORY}")

    def query(self, query_text: str, n_results: int = 3):
        """
        检索最相似的代码块
        :param query_text: 用户的自然语言问题
        :param n_results: 返回最相似的 N 个结果
        """
        print(f"[{self._now()}]正在检索问题: [{query_text}]")
        results = self.collection.query(
            query_texts=[query_text],
            n_results=n_results
        )
        return results

    def count(self):
        """
        获取向量库中的文档总数
        """
        return self.collection.count()

    def delete_by_source(self, source_path: str):
        """
        根据源文件路径删除相关的向量数据
        用于在文件更新时，先清理旧版本的数据
        """
        try:
            # 1. 先查询是否存在（为了打印日志，ChromaDB 的 delete 默认如果不匹配也不会报错）
            existing = self.collection.get(where={"source": source_path})
            count = len(existing['ids']) if existing and existing['ids'] else 0
            
            if count > 0:
                print(f"[{self._now()}]检测到旧数据，正在删除 {count} 条来自 {source_path} 的记录...")
                self.collection.delete(where={"source": source_path})
                print(f"[{self._now()}]旧数据删除成功。")
            else:
                print(f"[{self._now()}]未发现来自 {source_path} 的旧数据，将直接新增。")
        except Exception as e:
            print(f"[{self._now()}]删除旧数据时出错: {str(e)}")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

class ChatMemoryStore:
    """
    对话记忆向量存储管理类
    负责存储和检索用户的历史对话，用于长期记忆
    """
    def __init__(self):
        print(f"[{self._now()}]正在初始化向量数据库，存储路径: {PERSIST_DIRECTORY}")
        self.client = chromadb.PersistentClient(path=PERSIST_DIRECTORY)
        self.collection = self.client.get_or_create_collection(name=MEMORY_COLLECTION_NAME)
        print(f"[{self._now()}]对话记忆数据库连接成功！")

    def add_messages(self, user_id: str, messages: List[Dict], session_id: str = None):
        if not messages:
            return
        ids = []
        documents = []
        metadatas = []
        
        # 用于日志展示的摘要
        summary = []
        
        for msg in messages:
            content = msg.get("content")
            if not content:
                continue
            ids.append(str(uuid.uuid4()))
            documents.append(content)
            metadata = {
                "user_id": str(user_id),
                "role": msg.get("role", "user"),
                "timestamp": int(msg.get("timestamp", 0)),
                "sequence": int(msg.get("sequence", 0))
            }
            if session_id:
                metadata["session_id"] = str(session_id)
            metadatas.append(metadata)
            
            # 记录摘要 (截取前20个字符)
            role_icon = "👤" if metadata["role"] == "user" else "🤖"
            summary.append(f"{role_icon} {content[:20]}...")

        if documents:
            self.collection.add(
                documents=documents,
                metadatas=metadatas,
                ids=ids
            )
            print(f"[{self._now()}][记忆存储] 已存入 {len(documents)} 条对话: {' | '.join(summary)}")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

    def query(self, query_text: str, user_id: str, n_results: int = 6, session_id: str = None):
        where = {"user_id": str(user_id)}
        if session_id:
            where["session_id"] = str(session_id)
            
        print(f"[{self._now()}][记忆检索] 用户: {user_id} | 问题: {query_text[:30]}...")
        
        results = self.collection.query(
            query_texts=[query_text],
            n_results=n_results,
            where=where
        )
        return results

    def count(self):
        """
        获取记忆库中的文档总数
        """
        return self.collection.count()

    def delete_by_user(self, user_id: str, messages: List[str] = None):
        """
        删除指定用户的记忆
        :param user_id: 用户ID
        :param messages: 要删除的特定消息内容列表（如果为None则删除该用户所有记忆）
        """
        try:
            where = {"user_id": str(user_id)}
            
            # 如果指定了消息内容，则进行精确查找删除
            if messages:
                print(f"[{self._now()}][记忆删除] 正在查找用户 {user_id} 的 {len(messages)} 条指定记忆...")
                # 获取该用户的所有记忆
                existing = self.collection.get(where=where)
                if not existing or not existing['ids']:
                    print(f"[{self._now()}][记忆删除] 用户 {user_id} 无记忆数据。")
                    return

                ids_to_delete = []
                docs_to_delete = []
                # 遍历查找匹配的内容
                # existing['documents'] 是列表
                for i, doc in enumerate(existing['documents']):
                    if doc in messages:
                        ids_to_delete.append(existing['ids'][i])
                        docs_to_delete.append(doc)
                
                if ids_to_delete:
                    print(f"[{self._now()}][记忆删除] 找到 {len(ids_to_delete)} 条匹配记忆，正在删除...")
                    # 打印具体删除的内容预览
                    for doc in docs_to_delete:
                        preview = doc[:50].replace('\n', ' ') + "..." if len(doc) > 50 else doc.replace('\n', ' ')
                        print(f"  - 删除: {preview}")
                        
                    self.collection.delete(ids=ids_to_delete)
                    print(f"[{self._now()}][记忆删除] 删除成功。")
                    return len(ids_to_delete)
                else:
                    print(f"[{self._now()}][记忆删除] 未找到匹配的记忆内容。")
                    return 0

            # 否则删除该用户所有记忆
            # 1. 先查询是否存在
            existing = self.collection.get(where=where)
            count = len(existing['ids']) if existing and existing['ids'] else 0
            
            if count > 0:
                print(f"[{self._now()}][记忆删除] 正在删除用户 {user_id} 的 {count} 条记忆...")
                self.collection.delete(where=where)
                print(f"[{self._now()}][记忆删除] 删除成功。")
                return count
            else:
                print(f"[{self._now()}][记忆删除] 用户 {user_id} 无记忆数据。")
                return 0
        except Exception as e:
            print(f"[{self._now()}][记忆删除] 删除出错: {str(e)}")
            raise e

if __name__ == "__main__":
    # 1. 初始化数据库
    vector_store = VectorStore()
    
    # 2. 解析代码文件
    parser = JavaParser()
    target_file = r"d:\study\SwiftBoot\swiftboot-backend\swiftboot-admin\src\main\java\com\swiftboot\admin\controller\SysAiController.java"
    
    if os.path.exists(target_file):
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}]--- 第一步：解析文件 ---")
        chunks = parser.parse_file(target_file)
        
        # 补充来源信息
        for chunk in chunks:
            chunk['source'] = "SysAiController.java"
            
        # 3. 存入数据库
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}]--- 第二步：存入数据库 ---")
        vector_store.add_documents(chunks)
        
        # 4. 测试检索效果
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}]--- 第三步：测试检索 ---")
        test_query = "流式输出是怎么实现的？"
        results = vector_store.query(test_query)
        
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}]=== 检索结果 ===")
        if results['documents']:
            for i, doc in enumerate(results['documents'][0]):
                meta = results['metadatas'][0][i]
                print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}][结果 {i+1}] 来源: {meta['name']} ({meta['type']})")
                print(f"内容预览: {doc[:100].replace(chr(10), ' ')}...")
        else:
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]未找到相关结果")
            
    else:
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]测试文件不存在: {target_file}")
