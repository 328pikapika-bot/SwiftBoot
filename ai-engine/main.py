from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from vector_store import VectorStore, ChatMemoryStore
import uvicorn
import jieba.analyse
import jieba

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

@app.get("/stats")
def get_stats():
    try:
        return {
            "knowledge_count": db.count(),
            "memory_count": memory_db.count()
        }
    except Exception as e:
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

if __name__ == "__main__":
    print("启动 AI 知识检索引擎 API...")
    uvicorn.run(app, host="0.0.0.0", port=8001)
