import chromadb
from chromadb.config import Settings
import os
import time
import hashlib
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
            "doc_collection_name": "swiftboot_docs",
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
                        
                    if 'doc-collection-name' in engine_config:
                        default_config["vector_db"]["doc_collection_name"] = engine_config['doc-collection-name']
                        
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
DOC_COLLECTION_NAME = config["vector_db"]["doc_collection_name"]
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
        self.client = chromadb.PersistentClient(
            path=PERSIST_DIRECTORY,
            settings=Settings(anonymized_telemetry=False)
        )
        
        # 获取或创建集合（类似于 SQL 中的 Table）
        # collection: 存储代码
        # doc_collection: 存储文档
        self.collection = self.client.get_or_create_collection(name=COLLECTION_NAME)
        self.doc_collection = self.client.get_or_create_collection(name=DOC_COLLECTION_NAME)
        print(f"[{self._now()}]数据库连接成功！Code Collection: {COLLECTION_NAME}, Doc Collection: {DOC_COLLECTION_NAME}")

    def calculate_similarity(self, text1: str, text2: str) -> float:
        """
        计算两个文本的余弦相似度
        利用 ChromaDB 内置的 embedding function 进行向量化
        """
        try:
            # 获取 embedding function
            # 注意：ChromaDB 的 collection 默认使用 all-MiniLM-L6-v2
            # 我们这里直接利用 collection 的 embedding_function
            embedding_function = self.collection._embedding_function
            
            if not embedding_function:
                # Fallback: 如果没有显式设置，尝试从 chromadb.utils.embedding_functions 导入
                # 但通常 persistent client 会有默认的
                from chromadb.utils import embedding_functions
                embedding_function = embedding_functions.DefaultEmbeddingFunction()
                
            embeddings = embedding_function([text1, text2])
            
            vec1 = embeddings[0]
            vec2 = embeddings[1]
            
            # 计算余弦相似度
            import numpy as np
            
            # 转换为 numpy 数组
            v1 = np.array(vec1)
            v2 = np.array(vec2)
            
            # 计算点积
            dot_product = np.dot(v1, v2)
            
            # 计算模长
            norm_v1 = np.linalg.norm(v1)
            norm_v2 = np.linalg.norm(v2)
            
            if norm_v1 == 0 or norm_v2 == 0:
                return 0.0
                
            similarity = dot_product / (norm_v1 * norm_v2)
            return float(similarity)
            
        except Exception as e:
            print(f"[{self._now()}]计算相似度失败: {e}")
            return 0.0

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
        根据类型分别存入代码集合或文档集合
        :param chunks: 代码块列表，包含 content, name, type, source 等信息
        """
        if not chunks:
            print(f"[{self._now()}]没有数据需要存储。")
            return
            
        print(f"[{self._now()}]正在准备存储 {len(chunks)} 个代码块...")
        
        # 分离代码和文档
        code_chunks = []
        doc_chunks = []
        
        for chunk in chunks:
            is_doc = False
            # 判断逻辑：markdown_section 类型，或者文件名以 .md, .txt 结尾
            if chunk.get('type') == 'markdown_section':
                is_doc = True
            elif chunk.get('file_path', '').lower().endswith(('.md', '.txt')):
                is_doc = True
                
            if is_doc:
                doc_chunks.append(chunk)
            else:
                code_chunks.append(chunk)
        
        self._add_to_collection(self.collection, code_chunks, "Code")
        self._add_to_collection(self.doc_collection, doc_chunks, "Doc")

    def _add_to_collection(self, collection, chunks, label):
        if not chunks:
            return

        ids = []
        documents = []
        metadatas = []
        
        for idx, chunk in enumerate(chunks):
            # 生成唯一ID (UUID v4)
            source = chunk.get('source', 'unknown')
            # 兼容旧字段 file_path (如果 source 为空)
            if source == 'unknown' and 'file_path' in chunk:
                source = chunk['file_path']
                
            unique_key = f"{source}|{chunk['name']}|{chunk['type']}|{idx}"
            ids.append(hashlib.sha1(unique_key.encode('utf-8')).hexdigest())
            # 存入主要文本内容（ChromaDB 会自动将其转为 384 维向量）
            documents.append(chunk['content'])
            # 存入元数据（用于后续的过滤或前端展示）
            metadatas.append({
                'name': chunk['name'], 
                'type': chunk['type'], 
                'source': source
            })
            
        # 批量写入 (Batch upsert)
        batch_size = 100
        total = len(chunks)
        print(f"[{self._now()}]开始写入 {label} 集合 (共 {total} 条)...")
        
        for i in range(0, total, batch_size):
            end = min(i + batch_size, total)
            try:
                collection.upsert(
                    ids=ids[i:end],
                    documents=documents[i:end],
                    metadatas=metadatas[i:end]
                )
                print(f"[{self._now()}][{label}] 已处理 {end}/{total}")
            except Exception as e:
                print(f"[{self._now()}][{label}] 写入失败 (Batch {i}-{end}): {e}")
                self._log_to_backend(f"[{label}] 向量写入失败", str(e), 1)
                
        print(f"[{self._now()}][{label}] 写入完成。")
        self._log_to_backend(f"[{label}] 知识库更新完成", f"新增/更新 {total} 个切片")

    def query(self, question: str, n_results: int = 5):
        """
        检索最相似的代码或文档
        策略：同时检索代码库和文档库，然后合并结果
        """
        print(f"[{self._now()}]正在检索: {question}")
        
        all_results = {
            'ids': [[]],
            'distances': [[]],
            'metadatas': [[]],
            'documents': [[]]
        }
        
        try:
            # 1. 检索代码库
            # 稍微多取一点，以便混合
            code_results = self.collection.query(
                query_texts=[question],
                n_results=n_results
            )
            
            # 2. 检索文档库
            # 文档通常较少，取 n_results 即可
            doc_results = self.doc_collection.query(
                query_texts=[question],
                n_results=n_results
            )
            
            # 3. 合并结果
            # ChromaDB 返回的是列表的列表 [[...]]
            c_ids = code_results['ids'][0] if code_results['ids'] else []
            c_dists = code_results['distances'][0] if code_results['distances'] else []
            c_metas = code_results['metadatas'][0] if code_results['metadatas'] else []
            c_docs = code_results['documents'][0] if code_results['documents'] else []
            
            d_ids = doc_results['ids'][0] if doc_results['ids'] else []
            d_dists = doc_results['distances'][0] if doc_results['distances'] else []
            d_metas = doc_results['metadatas'][0] if doc_results['metadatas'] else []
            d_docs = doc_results['documents'][0] if doc_results['documents'] else []
            
            # 简单的合并策略：按距离排序
            # 构建 (distance, id, meta, doc, type) 元组列表
            combined = []
            for i in range(len(c_ids)):
                combined.append({
                    'distance': c_dists[i],
                    'id': c_ids[i],
                    'metadata': c_metas[i],
                    'document': c_docs[i],
                    'origin': 'code'
                })
            for i in range(len(d_ids)):
                combined.append({
                    'distance': d_dists[i],
                    'id': d_ids[i],
                    'metadata': d_metas[i],
                    'document': d_docs[i],
                    'origin': 'doc'
                })
                
            # 按距离升序排序 (越小越相似)
            combined.sort(key=lambda x: x['distance'])
            
            # 取 Top N
            final_top = combined[:n_results]
            
            # 重构回 ChromaDB 结果格式
            all_results['ids'][0] = [x['id'] for x in final_top]
            all_results['distances'][0] = [x['distance'] for x in final_top]
            all_results['metadatas'][0] = [x['metadata'] for x in final_top]
            all_results['documents'][0] = [x['document'] for x in final_top]
            
            return all_results
            
        except Exception as e:
            print(f"[{self._now()}]检索失败: {e}")
            import traceback
            traceback.print_exc()
            return {'ids': [], 'distances': [], 'metadatas': [], 'documents': []}

    def count(self):
        """
        获取向量库中的文档总数 (Code + Doc)
        """
        try:
            c_count = self.collection.count()
            d_count = self.doc_collection.count()
            return c_count + d_count
        except Exception as e:
            print(f"[{self._now()}]获取数量失败: {e}")
            return 0

    def delete_by_source(self, source_path: str):
        """
        根据源文件路径删除相关的向量数据
        用于在文件更新时，先清理旧版本的数据
        """
        try:
            candidates = {source_path}
            base = os.path.basename(source_path)
            if base:
                candidates.add(base)

            deleted_any = False
            for candidate in candidates:
                try:
                    # 尝试从两个集合中删除
                    self.collection.delete(where={"source": candidate})
                    self.doc_collection.delete(where={"source": candidate})
                    deleted_any = True
                except Exception as e:
                    print(f"[{self._now()}]删除来源失败: {candidate}, 错误: {e}")

            if deleted_any:
                print(f"[{self._now()}]已清理来源: {', '.join(sorted(candidates))}")
        except Exception as e:
            print(f"[{self._now()}]删除来源异常: {e}")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

class ChatMemoryStore:
    """
    对话记忆向量存储管理类
    负责存储和检索用户的历史对话，用于长期记忆
    """
    def __init__(self):
        print(f"[{self._now()}]正在初始化向量数据库，存储路径: {PERSIST_DIRECTORY}")
        self.client = chromadb.PersistentClient(
            path=PERSIST_DIRECTORY,
            settings=Settings(anonymized_telemetry=False)
        )
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
        
        # 为了进行重排，先召回更多候选项 (比如 20 条)
        recall_limit = max(n_results * 3, 20)
        results = self.collection.query(
            query_texts=[query_text],
            n_results=recall_limit,
            where=where
        )
        
        # 内存重排 (Re-ranking): 结合向量距离、引用次数和时间衰减
        if results and results.get('documents') and len(results['documents'][0]) > 0:
            current_time = time.time()
            reranked_items = []
            
            docs = results['documents'][0]
            metas = results['metadatas'][0]
            dists = results['distances'][0] if 'distances' in results else [0] * len(docs)
            ids = results['ids'][0]
            
            for i in range(len(docs)):
                meta = metas[i]
                dist = dists[i]
                
                # 1. 基础分: 距离越小分越高。假设距离在 0~2 之间，转换为 0~1 的相似度得分
                base_score = max(0, 1.0 - (dist / 2.0))
                
                # 2. 引用权重加成
                citation_count = meta.get('citation_count', 0)
                # 引用次数带来的加成，设置上限避免马太效应过强
                citation_bonus = min(citation_count * 0.05, 0.3)
                
                # 3. 时间衰减惩罚
                timestamp = meta.get('timestamp', 0)
                if timestamp > 1000000000000: # 如果是毫秒
                    timestamp = timestamp / 1000.0
                
                # 计算距离当前的天数
                days_ago = max(0, (current_time - timestamp) / (24 * 3600))
                # 时间衰减：每天衰减 0.01，最多衰减 0.2
                time_penalty = min(days_ago * 0.01, 0.2)
                
                # 4. 最终得分
                final_score = base_score + citation_bonus - time_penalty
                
                reranked_items.append({
                    'id': ids[i],
                    'document': docs[i],
                    'metadata': meta,
                    'distance': dist,
                    'final_score': final_score,
                    'base_score': base_score,
                    'citation_bonus': citation_bonus,
                    'time_penalty': time_penalty
                })
            
            # 按最终得分降序排序
            reranked_items.sort(key=lambda x: x['final_score'], reverse=True)
            
            # 截取 Top N
            top_items = reranked_items[:n_results]
            
            # 打印重排日志，方便调试
            print(f"[{self._now()}][记忆重排] Top 3 详情:")
            for i, item in enumerate(top_items[:3]):
                print(f"  {i+1}. 得分:{item['final_score']:.2f} (基础:{item['base_score']:.2f}, 引用:+{item['citation_bonus']:.2f}, 衰减:-{item['time_penalty']:.2f}) | 内容: {item['document'][:20]}...")
            
            # 重新组装为 ChromaDB 的返回格式
            return {
                'ids': [[item['id'] for item in top_items]],
                'documents': [[item['document'] for item in top_items]],
                'metadatas': [[item['metadata'] for item in top_items]],
                'distances': [[item['distance'] for item in top_items]]
            }
            
        return results

    def update_citation(self, user_id: str, memory_id: str = None, text_content: str = None):
        """
        更新记忆的引用次数
        """
        try:
            where = {"user_id": str(user_id)}
            
            # 查找目标记忆
            existing = None
            if memory_id:
                existing = self.collection.get(ids=[memory_id])
            elif text_content:
                # 如果没有 ID，尝试通过内容模糊查找
                res = self.collection.query(query_texts=[text_content], n_results=1, where=where)
                if res and res['ids'] and len(res['ids'][0]) > 0:
                    target_id = res['ids'][0][0]
                    existing = self.collection.get(ids=[target_id])
                    
            if existing and existing['ids']:
                target_id = existing['ids'][0]
                meta = existing['metadatas'][0]
                doc = existing['documents'][0]
                
                # 更新 citation_count
                current_count = meta.get('citation_count', 0)
                meta['citation_count'] = current_count + 1
                
                # 执行更新
                self.collection.update(
                    ids=[target_id],
                    metadatas=[meta],
                    documents=[doc]
                )
                print(f"[{self._now()}][记忆更新] 引用次数 +1 (当前:{meta['citation_count']}): {doc[:20]}...")
                return True
        except Exception as e:
            print(f"[{self._now()}][记忆更新] 失败: {e}")
        return False

    def count(self):
        """
        获取记忆库中的文档总数
        """
        try:
            return self.collection.count()
        except Exception as e:
            print(f"[{self._now()}]获取记忆数量失败: {e}")
            return 0

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
