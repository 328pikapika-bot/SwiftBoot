from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from vector_store import VectorStore, ChatMemoryStore
import uvicorn

app = FastAPI(title="SwiftBoot AI Knowledge Engine")

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

if __name__ == "__main__":
    print("启动 AI 知识检索引擎 API...")
    uvicorn.run(app, host="0.0.0.0", port=8001)
