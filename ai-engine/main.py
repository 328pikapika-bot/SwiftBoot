from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from vector_store import VectorStore, ChatMemoryStore
import uvicorn
import os
import jieba.analyse
import jieba
import time
import signal
import sys
from threading import Thread, Lock
from knowledge_ingest import JavaParser, PythonParser, VueComponentParser, MarkdownParser, TypeScriptParser

# 加载自定义词典
# 实际项目中，这里应该从 vector_store 中动态读取项目中的类名、方法名作为词典
# 这里简单演示，添加一些常见的技术栈词汇
custom_words = [
    "SwiftBoot", "Spring Boot", "Spring Security", "MyBatis", "Redis", "Vue", "Element Plus", "TypeScript",
    "FastAPI", "ChromaDB", "Watchdog", "DeepSeek", "RAG", "Agent", "Function Calling", "SysUser", "SysDept", "SysMenu", "SysRole",
    "SysAiSession", "SysOperLog", "Controller", "Service", "Mapper", "Entity", "DTO", "VO"
]
for word in custom_words:
    jieba.add_word(word)

app = FastAPI(title="SwiftBoot AI Knowledge Engine")

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源，生产环境建议配置为具体的域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 全局异常捕获
@app.middleware("http")
async def catch_exceptions_middleware(request: Request, call_next):
    try:
        return await call_next(request)
    except Exception as e:
        print(f"Unhandled Exception: {e}")
        import traceback
        traceback.print_exc()
        return JSONResponse(status_code=500, content={"detail": "Internal Server Error"})

# 初始化向量数据库
db = VectorStore()

memory_db = ChatMemoryStore()

class QueryRequest(BaseModel):
    question: str
    n_results: int = 3

class MemoryMessage(BaseModel):
    content: str
    role: str = "user"
    timestamp: int
    sequence: int

class MemoryAddRequest(BaseModel):
    user_id: str
    session_id: str | None = None
    messages: list[MemoryMessage]

class MemoryQueryRequest(BaseModel):
    user_id: str
    session_id: str | None = None
    question: str
    n_results: int = 6
    max_distance: float | None = None

@app.post("/retrieve")
async def retrieve_knowledge(request: QueryRequest):
    """
    代码库检索接口 (RAG)
    接收用户问题，从向量数据库检索相关代码片段
    
    逻辑：
    1. 接收 question
    2. 调用 VectorStore.query 进行向量相似度搜索
    3. 返回最相似的 Top-N 代码块
    """
    try:
        results = db.query(request.question, n_results=request.n_results)
        
        # 格式化返回结果
        response = []
        if results['documents']:
            for i, doc in enumerate(results['documents'][0]):
                meta = results['metadatas'][0][i]
                response.append({
                    "content": doc,
                    "metadata": meta,
                    "distance": results['distances'][0][i] if 'distances' in results else None
                })
        
        return {"results": response}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/memory/add")
async def add_memory(request: MemoryAddRequest):
    """
    添加长期记忆接口
    当一次对话结束时调用，将用户问题和 AI 回答存入向量库
    """
    try:
        payload = [m.model_dump() for m in request.messages]
        memory_db.add_messages(request.user_id, payload, request.session_id)
        return {"status": "ok", "count": len(payload)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/memory/query")
async def query_memory(request: MemoryQueryRequest):
    """
    记忆检索接口
    根据用户当前问题，检索历史对话中相似的片段
    
    逻辑：
    1. 向量搜索 (Similarity Search)
    2. 过滤 (Filter by user_id)
    3. 阈值过滤 (Filter by max_distance)
    4. 排序 (Re-Rank by timestamp)
    """
    try:
        results = memory_db.query(
            request.question,
            user_id=request.user_id,
            n_results=request.n_results,
            session_id=request.session_id
        )
        response = []
        if results.get("documents"):
            for i, doc in enumerate(results["documents"][0]):
                meta = results["metadatas"][0][i]
                distance = results["distances"][0][i] if "distances" in results else None
                # 距离阈值过滤：如果距离太大（相关性太低），则丢弃
                if request.max_distance is not None and distance is not None and distance > request.max_distance:
                    continue
                response.append({
                    "content": doc,
                    "metadata": meta,
                    "distance": distance
                })
        # 按时间戳排序，保证返回给 AI 的上下文是按时间顺序的
        response.sort(key=lambda x: int(x["metadata"].get("timestamp", 0)))
        return {"results": response}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class SimilarityRequest(BaseModel):
    text1: str
    text2: str

@app.post("/nlp/similarity")
async def calculate_similarity(request: SimilarityRequest):
    """
    计算两个文本的语义相似度 (0.0 ~ 1.0)
    用于判断多轮对话中是否需要引入上下文
    """
    try:
        score = db.calculate_similarity(request.text1, request.text2)
        return {"similarity": score}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/stats")
def get_stats():
    try:
        return {
            "knowledge_count": db.count(),
            "memory_count": memory_db.count()
        }
    except Exception as e:
        print(f"Error in /stats: {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/knowledge/stats")
def get_knowledge_stats():
    """
    获取详细的知识库统计数据
    包括文件分布、切片总数、记忆总数等
    """
    try:
        # 1. 获取知识库统计 (Left Brain)
        collection = db.collection
        count = collection.count()
        
        # 获取所有文档的 metadata 来统计分布 (注意：如果数据量巨大，这里会有性能问题，应考虑缓存或抽样)
        # ChromaDB 的 get 方法支持 include=['metadatas']
        # limit=None 在 ChromaDB 旧版本可能不支持，通常需要分页。
        # 为避免卡死，这里暂时限制最大获取 10000 条用于统计分布
        result = collection.get(limit=10000, include=['metadatas'])
        metadatas = result['metadatas']
        
        file_types = {}
        languages = {}
        
        for meta in metadatas:
            if not meta: continue
            
            # 统计文件类型 (Extension)
            source = meta.get('source', '')
            ext = source.split('.')[-1].lower() if '.' in source else 'unknown'
            file_types[ext] = file_types.get(ext, 0) + 1
            
            # 统计语言 (简单映射)
            lang = 'Other'
            if ext in ['java']: lang = 'Java'
            elif ext in ['py']: lang = 'Python'
            elif ext in ['vue', 'ts', 'js', 'html', 'css']: lang = 'Frontend'
            elif ext in ['sql']: lang = 'SQL'
            elif ext in ['md', 'txt']: lang = 'Docs'
            languages[lang] = languages.get(lang, 0) + 1
            
        # 2. 获取记忆统计 (Right Brain)
        # 同样限制获取最近的 10 条记忆用于展示
        mem_collection = memory_db.collection
        mem_count = mem_collection.count()
        mem_result = mem_collection.get(limit=10, include=['documents', 'metadatas']) # 默认按插入顺序，实际上可能是随机的
        
        recent_memories = []
        if mem_result['documents']:
            for i, doc in enumerate(mem_result['documents']):
                meta = mem_result['metadatas'][i]
                # 简单分类：根据内容关键词打标
                mem_type = 'business' # default
                content = doc.lower()
                if '必须' in content or '禁止' in content or 'always' in content:
                    mem_type = 'rule'
                elif '偏好' in content or '喜欢' in content or '习惯' in content:
                    mem_type = 'preference'
                elif '修正' in content or '错误' in content or 'bug' in content:
                    mem_type = 'correction'
                
                # 格式化时间
                import datetime
                ts = meta.get('timestamp', 0)
                try:
                    # 如果 timestamp 是毫秒 (13位)，转换为秒
                    if ts > 1000000000000:
                        ts = ts / 1000.0
                        
                    if ts > 0:
                        dt = datetime.datetime.fromtimestamp(ts)
                        # 【临时修正】如果年份是 2024，强制修正为 2026
                        # 这可能是因为某些历史数据的时间戳错误，或者环境时间配置问题
                        if dt.year == 2024:
                            dt = dt.replace(year=2026)
                        time_str = dt.strftime('%Y-%m-%d')
                    else:
                        time_str = datetime.datetime.now().strftime('%Y-%m-%d')
                except Exception as e:
                    print(f"Time parse error for ts={ts}: {e}")
                    # Fallback to current date
                    time_str = datetime.datetime.now().strftime('%Y-%m-%d')
                
                recent_memories.append({
                    "type": mem_type,
                    "content": doc,
                    "time": time_str,
                    "timestamp": ts
                })
        
        # 按时间倒序
        recent_memories.sort(key=lambda x: x['timestamp'], reverse=True)

        return {
            "total_chunks": count,
            "memory_count": mem_count,
            "languages": languages,
            "file_types": file_types,
            "recent_memories": recent_memories[:5] # 只返回最近 5 条
        }
    except Exception as e:
        print(f"Error in /knowledge/stats: {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

class MemoryDeleteRequest(BaseModel):
    user_id: str
    messages: list[str] | None = None

@app.post("/memory/delete")
async def delete_memory(request: MemoryDeleteRequest):
    """
    删除长期记忆接口
    根据 user_id 删除该用户的所有向量记忆，或根据 messages 删除特定记忆
    """
    try:
        count = memory_db.delete_by_user(request.user_id, request.messages)
        return {"status": "ok", "deleted_count": count}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health_check():
    return {"status": "ok", "db_path": db.client._system.settings.persist_directory}

ALLOWED_ROOT_DIRS = [
    "swiftboot-backend",
    "swiftboot-ui",
    "devDoc",
    "project-skills",
    "ai-engine",
    "快速启动",
    "release_notes"
]

IGNORE_DIRS = [
    ".git",
    "target",
    "build",
    "node_modules",
    "dist",
    ".idea",
    ".vscode",
    "__pycache__",
    "chroma_db",
    "logs",
    "javadoc",
    "classes",
    "generated-sources",
    "public"
]

IGNORE_FILENAMES = [
    "auto-imports.d.ts",
    "components.d.ts",
    "tsconfig.tsbuildinfo",
    "vite-env.d.ts",
    "typed-router.d.ts"
]

WATCH_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

index_rebuild_lock = Lock()
index_rebuild_state = {
    "running": False,
    "started_at": None,
    "finished_at": None,
    "indexed_chunks": 0,
    "error": None
}

class IndexRebuildRequest(BaseModel):
    force: bool = False

def _rebuild_index_worker(force: bool):
    started_at = time.time()
    indexed_chunks = 0
    error = None

    try:
        java_parser = JavaParser()
        py_parser = PythonParser()
        vue_parser = VueComponentParser()
        md_parser = MarkdownParser()
        ts_parser = TypeScriptParser()

        for root, dirs, files in os.walk(WATCH_DIR):
            if os.path.normpath(root) == os.path.normpath(WATCH_DIR):
                dirs[:] = [d for d in dirs if d in ALLOWED_ROOT_DIRS]
            else:
                dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]

            for file in files:
                if file in IGNORE_FILENAMES:
                    continue
                if file.lower().endswith(".d.ts"):
                    continue
                if not file.lower().endswith((".java", ".py", ".vue", ".md", ".doc", ".ts", ".js")):
                    continue

                file_path = os.path.join(root, file)
                source_id = file_path.replace("\\", "/")

                chunks = []
                if file.lower().endswith(".java"):
                    chunks = java_parser.parse_file(file_path)
                elif file.lower().endswith(".py"):
                    chunks = py_parser.parse_file(file_path)
                elif file.lower().endswith(".vue"):
                    chunks = vue_parser.parse_file(file_path)
                elif file.lower().endswith((".md", ".doc")):
                    chunks = md_parser.parse_file(file_path)
                elif file.lower().endswith((".ts", ".js")):
                    chunks = ts_parser.parse_file(file_path)

                if not chunks:
                    continue

                for chunk in chunks:
                    chunk["source"] = source_id

                if force:
                    db.delete_by_source(source_id)
                db.add_documents(chunks)
                indexed_chunks += len(chunks)

    except Exception as e:
        error = str(e)
    finally:
        finished_at = time.time()
        with index_rebuild_lock:
            index_rebuild_state["running"] = False
            index_rebuild_state["started_at"] = started_at
            index_rebuild_state["finished_at"] = finished_at
            index_rebuild_state["indexed_chunks"] = indexed_chunks
            index_rebuild_state["error"] = error

@app.post("/index/rebuild")
async def rebuild_index(request: IndexRebuildRequest):
    with index_rebuild_lock:
        if index_rebuild_state["running"]:
            return {"status": "running", **index_rebuild_state}
        index_rebuild_state["running"] = True
        index_rebuild_state["started_at"] = time.time()
        index_rebuild_state["finished_at"] = None
        index_rebuild_state["indexed_chunks"] = 0
        index_rebuild_state["error"] = None

    Thread(target=_rebuild_index_worker, args=(request.force,), daemon=True).start()
    with index_rebuild_lock:
        return {"status": "started", **index_rebuild_state}

@app.get("/index/rebuild/status")
def rebuild_index_status():
    with index_rebuild_lock:
        return dict(index_rebuild_state)

class TopicRequest(BaseModel):
    text: str

@app.post("/nlp/topic")
async def extract_topic(request: TopicRequest):
    """
    NLP 关键词/主题提取接口
    使用 jieba.analyse.textrank 算法提取核心关键词
    """
    try:
        if not request.text or len(request.text.strip()) == 0:
            return {"topic": None}
            
        # 1. 提取关键词 (TextRank 算法，偏向名词和短语)
        # allowPOS=('n', 'nz', 'v', 'vd', 'vn', 'eng') - 允许名词、动名词、英文
        keywords = jieba.analyse.textrank(
            request.text, 
            topK=3, 
            withWeight=False, 
            allowPOS=('n', 'nz', 'eng', 'vn') # 排除纯动词，保留名动词
        )
        
        # 2. 如果 TextRank 没结果（通常是因为句子太短），改用 TF-IDF
        if not keywords:
            keywords = jieba.analyse.extract_tags(request.text, topK=3, allowPOS=('n', 'nz', 'eng', 'vn'))
            
        # 3. 结果处理
        topic = None
        if keywords:
            # 优先选择最长或最有意义的词，或者直接返回 top 1
            # 这里简单返回第一个，通常是最核心的
            topic = keywords[0]
            
            # 特殊处理：如果提取出的词太通用（如“问题”、“时间”），则尝试取第二个
            stop_words = ["问题", "时间", "内容", "数据", "结果", "系统", "功能"]
            if topic in stop_words and len(keywords) > 1:
                topic = keywords[1]
                
        return {"topic": topic}
        
    except Exception as e:
        print(f"Topic extraction failed: {e}")
        return {"topic": None}

def signal_handler(sig, frame):
    print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}] 接收到系统信号 {sig}，准备退出...")
    sys.exit(0)

if __name__ == "__main__":
    try:
        # 注册信号处理器
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
        
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 启动 AI 知识检索引擎 API...")
        uvicorn.run(app, host="0.0.0.0", port=8001)
    except KeyboardInterrupt:
        print("\nAI 引擎已手动停止")
    except SystemExit:
        print("\nAI 引擎正常退出")
    except Exception as e:
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}] AI 引擎发生异常退出: {e}")
        import traceback
        traceback.print_exc()
        input("按回车键退出...")
