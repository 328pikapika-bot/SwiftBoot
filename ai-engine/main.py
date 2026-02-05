from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from vector_store import VectorStore
import uvicorn

app = FastAPI(title="SwiftBoot AI Knowledge Engine")

# 初始化向量数据库
db = VectorStore()

class QueryRequest(BaseModel):
    question: str
    n_results: int = 3

@app.post("/retrieve")
async def retrieve_knowledge(request: QueryRequest):
    """
    接收用户问题，从向量数据库检索相关代码片段
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

@app.get("/health")
def health_check():
    return {"status": "ok", "db_path": db.client._system.settings.persist_directory}

if __name__ == "__main__":
    print("启动 AI 知识检索引擎 API...")
    uvicorn.run(app, host="0.0.0.0", port=8001)
