import time
import os
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from knowledge_ingest import JavaParser
from vector_store import VectorStore

# ==========================================
# 配置
# ==========================================
# 监听的目录（递归监听）
WATCH_DIR = r"d:\study\SwiftBoot\swiftboot-backend"
# 忽略的目录（可选）
IGNORE_DIRS = [".git", "target", "build"]

class CodeChangeHandler(FileSystemEventHandler):
    def __init__(self):
        self.parser = JavaParser()
        self.db = VectorStore()
        self.last_processed = {}  # 简单的防抖动机制 {path: timestamp}

    def on_modified(self, event):
        self._process_event(event)

    def on_created(self, event):
        self._process_event(event)
    
    def on_moved(self, event):
        # 如果文件移动了，旧路径的记录应该删除，新路径的记录应该添加
        if not event.is_directory and event.src_path.endswith(".java"):
             print(f"\n[检测到文件移动] {event.src_path} -> {event.dest_path}")
             # 删除旧的
             self.db.delete_by_source(os.path.basename(event.src_path))
             # 处理新的（模拟一个 created 事件）
             class MockEvent:
                 is_directory = False
                 src_path = event.dest_path
             self._process_event(MockEvent())

    def on_deleted(self, event):
        if not event.is_directory and event.src_path.endswith(".java"):
            file_name = os.path.basename(event.src_path)
            print(f"\n[检测到文件删除] {file_name}")
            self.db.delete_by_source(file_name)

    def _process_event(self, event):
        if event.is_directory:
            return
        
        filename = event.src_path
        if not filename.endswith(".java"):
            return

        # 简单的防抖动：1秒内不重复处理同一个文件
        now = time.time()
        if filename in self.last_processed:
            if now - self.last_processed[filename] < 1:
                return
        self.last_processed[filename] = now

        print(f"\n[检测到代码变更] {filename}")
        
        try:
            # 1. 提取文件名作为 Source ID
            # 注意：这里我们简单地用文件名作为 source。
            # 如果不同目录下有同名文件，可能会有冲突。
            # 生产环境建议用 相对项目根目录的路径 (e.g. src/main/java/.../SysAiController.java)
            # 但为了和 vector_store.py 保持一致，先用 basename
            source_id = os.path.basename(filename)

            # 2. 清理旧数据
            self.db.delete_by_source(source_id)

            # 3. 重新解析
            print("正在重新解析代码...")
            chunks = self.parser.parse_file(filename)
            
            if chunks:
                # 补充 source 字段
                for chunk in chunks:
                    chunk['source'] = source_id
                
                # 4. 存入数据库
                self.db.add_documents(chunks)
                print(f"更新完成！已同步 {len(chunks)} 个代码块到向量数据库。")
            else:
                print("警告：解析结果为空，未更新数据库。")

        except Exception as e:
            print(f"处理文件变更时出错: {str(e)}")

if __name__ == "__main__":
    if not os.path.exists(WATCH_DIR):
        print(f"错误：监听目录不存在: {WATCH_DIR}")
        exit(1)

    print(f"启动代码监听服务 (Watchdog)...")
    
    # ==========================================
    # 启动时全量扫描
    # ==========================================
    print(f"正在执行全量代码扫描: {WATCH_DIR} ...")
    parser = JavaParser()
    db = VectorStore()
    count = 0
    
    for root, dirs, files in os.walk(WATCH_DIR):
        # 过滤忽略目录
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                try:
                    source_id = os.path.basename(file_path)
                    # 先清理旧数据，确保是最新的
                    db.delete_by_source(source_id)
                    
                    chunks = parser.parse_file(file_path)
                    if chunks:
                        for chunk in chunks:
                            chunk['source'] = source_id
                        db.add_documents(chunks)
                        count += 1
                        print(f"[{count}] 已索引: {source_id} ({len(chunks)} 块)")
                except Exception as e:
                    print(f"索引文件失败 {file}: {e}")
                    
    print(f"全量扫描完成！共索引 {count} 个文件。")
    # ==========================================

    print(f"正在监控目录: {WATCH_DIR}")
    print("当你修改、保存 .java 文件时，系统会自动更新向量数据库。")
    print("按 Ctrl+C 停止服务。")

    event_handler = CodeChangeHandler()
    observer = Observer()
    observer.schedule(event_handler, WATCH_DIR, recursive=True)
    observer.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    
    observer.join()
