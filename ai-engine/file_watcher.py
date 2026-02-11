import time
import os
import json
import threading
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from knowledge_ingest import JavaParser, PythonParser, VueComponentParser
from vector_store import VectorStore

# ==========================================
# 配置
# ==========================================
# 监听的目录（递归监听）
# 使用相对路径：当前脚本所在目录(ai-engine)的上一级目录(SwiftBoot)
WATCH_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
# 忽略的目录（可选）
IGNORE_DIRS = [".git", "target", "build", "node_modules", "dist", ".idea", ".vscode", "__pycache__", "chroma_db"]
# 状态文件路径
STATE_FILE = os.path.join(os.path.dirname(__file__), "file_state.json")

def load_state():
    if os.path.exists(STATE_FILE):
        try:
            with open(STATE_FILE, 'r', encoding='utf-8') as f:
                state = json.load(f)
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 成功加载状态文件，共 {len(state)} 条记录。")
                return state
        except Exception as e:
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 加载状态文件失败: {e}")
            return {}
    return {}

def save_state(state):
    try:
        with open(STATE_FILE, 'w', encoding='utf-8') as f:
            json.dump(state, f, ensure_ascii=False, indent=2)
            # print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 状态已保存。") 
    except Exception as e:
        print(f"保存状态失败: {e}")

class CodeChangeHandler(FileSystemEventHandler):
    """
    文件系统事件处理器
    监听文件变更事件 (Modified, Created, Moved, Deleted)
    并自动触发向量数据库的更新
    """
    def __init__(self, file_state):
        self.java_parser = JavaParser()
        self.py_parser = PythonParser()
        self.vue_parser = VueComponentParser()
        self.db = VectorStore()
        self.last_processed = {}  # 简单的防抖动机制 {path: timestamp}
        self.file_state = file_state
        
        # 批量日志处理相关
        self.update_count = 0
        self.update_lock = threading.Lock()
        self.log_timer = None

    def _schedule_log_sync(self):
        """
        调度日志同步任务 (防抖)
        """
        with self.update_lock:
            if self.log_timer:
                self.log_timer.cancel()
            self.log_timer = threading.Timer(2.0, self._sync_log)
            self.log_timer.start()

    def _sync_log(self):
        """
        执行日志同步
        """
        with self.update_lock:
            count = self.update_count
            if count > 0:
                try:
                    self.db._log_to_backend(f"{count}条RAG 向量索引更新完成", "AI Engine")
                    print(f"[{self._now()}]已触发操作日志同步 (批量更新: {count}条)。")
                    self.update_count = 0
                except Exception as e:
                    print(f"[{self._now()}]操作日志同步失败: {e}")

    def on_modified(self, event):
        self._process_event(event)

    def on_created(self, event):
        self._process_event(event)
    
    def on_moved(self, event):
        # 如果文件移动了，旧路径的记录应该删除，新路径的记录应该添加
        if not event.is_directory and (event.src_path.endswith(".java") or event.src_path.endswith(".py") or event.src_path.endswith(".vue")):
             print(f"\n[{self._now()}][检测到文件移动] {event.src_path} -> {event.dest_path}")
             # 1. 删除旧文件的向量数据
             self.db.delete_by_source(os.path.basename(event.src_path))
             # 更新状态
             norm_src = os.path.normpath(event.src_path).lower()
             if norm_src in self.file_state:
                 del self.file_state[norm_src]
                 save_state(self.file_state)
                 
                 
             # 2. 处理新文件（模拟一个 created 事件）
             class MockEvent:
                 is_directory = False
                 src_path = event.dest_path
             self._process_event(MockEvent())

    def on_deleted(self, event):
        if not event.is_directory and (event.src_path.endswith(".java") or event.src_path.endswith(".py") or event.src_path.endswith(".vue")):
            file_name = os.path.basename(event.src_path)
            print(f"\n[{self._now()}][检测到文件删除] {file_name}")
            # 从向量库中移除该文件的所有切片
            self.db.delete_by_source(file_name)
            # 更新状态
            norm_src = os.path.normpath(event.src_path).lower()
            if norm_src in self.file_state:
                del self.file_state[norm_src]
                save_state(self.file_state)

    def _process_event(self, event):
        """
        统一处理文件变更的核心逻辑
        """
        if event.is_directory:
            return
        
        filename = event.src_path
        if not (filename.endswith(".java") or filename.endswith(".py") or filename.endswith(".vue")):
            return

        # 简单的防抖动：1秒内不重复处理同一个文件
        # 防止编辑器保存时可能触发多次事件
        now = time.time()
        if filename in self.last_processed:
            if now - self.last_processed[filename] < 1:
                return
        self.last_processed[filename] = now

        # 区分前后端
        is_frontend = filename.endswith(".vue") or "swiftboot-ui" in filename
        change_type = "前端" if is_frontend else "后端"
        
        print(f"\n[{self._now()}][检测到{change_type}代码变更] {filename}")
        
        try:
            # 1. 提取文件名作为 Source ID
            source_id = os.path.basename(filename)

            # 2. 清理旧数据 (先删后加，确保数据一致性)
            self.db.delete_by_source(source_id)

            # 3. 重新解析代码文件
            print(f"[{self._now()}]正在重新解析代码...")
            if filename.endswith(".java"):
                chunks = self.java_parser.parse_file(filename)
            elif filename.endswith(".py"):
                chunks = self.py_parser.parse_file(filename)
            elif filename.endswith(".vue"):
                chunks = self.vue_parser.parse_file(filename)
            else:
                chunks = []
            
            if chunks:
                # 补充 source 字段
                for chunk in chunks:
                    chunk['source'] = source_id
                
                # 4. 写入向量数据库
                self.db.add_documents(chunks)
                print(f"[{self._now()}]更新完成: {source_id} (新增 {len(chunks)} 个切片)")
                
                # 5. 记录到后端操作日志 (通过 HTTP 调用)
                try:
                    msg = f"检测到变更: {os.path.basename(filename)}"
                    self.db._log_to_backend(msg, change_type)
                    
                    # 累加更新计数
                    with self.update_lock:
                        self.update_count += len(chunks)
                    # 调度批量同步
                    self._schedule_log_sync()
                    
                except Exception as log_err:
                    print(f"[{self._now()}]日志记录失败: {log_err}")
            else:
                print(f"[{self._now()}]警告: 未提取到有效代码切片")

            # 6. 更新状态文件
            norm_path = os.path.normpath(filename).lower()
            self.file_state[norm_path] = now
            save_state(self.file_state)
            
        except Exception as e:
            print(f"[{self._now()}]处理变更失败: {e}")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

if __name__ == "__main__":
    if not os.path.exists(WATCH_DIR):
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]错误：监听目录不存在: {WATCH_DIR}")
        exit(1)

    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]启动代码监听服务 (Watchdog)...")
    
    # ==========================================
    # 启动时增量代码扫描
    # ==========================================
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]正在执行代码扫描 (增量模式): {WATCH_DIR} ...")
    java_parser = JavaParser()
    py_parser = PythonParser()
    vue_parser = VueComponentParser()
    db = VectorStore()
    
    # 加载上次的文件状态
    file_state = load_state()
    # 如果加载出来的状态 key 不是全小写，做一次规范化
    if file_state:
        file_state = {os.path.normpath(k).lower(): v for k, v in file_state.items()}
        
    new_file_state = {}
    
    count = 0
    skip_count = 0
    
    # 批量更新计数器
    batch_update_count = 0
    
    for root, dirs, files in os.walk(WATCH_DIR):
        # 过滤忽略目录
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        
        for file in files:
            if file.endswith(".java") or file.endswith(".py") or file.endswith(".vue"):
                file_path = os.path.join(root, file)
                # 统一路径格式（Windows下不区分大小写，统一转小写以避免盘符差异）
                norm_path = os.path.normpath(file_path).lower()
                
                try:
                    # 获取当前修改时间
                    mtime = os.path.getmtime(file_path)
                    new_file_state[norm_path] = mtime
                    
                    # 检查是否需要更新
                    # 注意：如果 file_state 为空（首次运行或状态丢失），则不跳过，执行全量同步
                    if norm_path in file_state and abs(file_state[norm_path] - mtime) < 0.001:
                        skip_count += 1
                        # 确保旧状态也被保留到新状态中（虽然上面已经赋值了，这里逻辑是正确的）
                        continue

                    source_id = os.path.basename(file_path)
                    # 先清理旧数据
                    db.delete_by_source(source_id)
                    
                    if file_path.endswith(".java"):
                        chunks = java_parser.parse_file(file_path)
                    elif file_path.endswith(".py"):
                        chunks = py_parser.parse_file(file_path)
                    elif file_path.endswith(".vue"):
                        chunks = vue_parser.parse_file(file_path)
                    else:
                        chunks = []
                        
                    if chunks:
                        for chunk in chunks:
                            chunk['source'] = source_id
                        db.add_documents(chunks)
                        batch_update_count += len(chunks)
                        count += 1
                        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] [{count}] 已同步变更: {source_id} ({len(chunks)} 块)")
                except Exception as e:
                    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]索引文件失败 {file}: {e}")

    # 处理在停止期间被删除的文件
    deleted_count = 0
    # 注意：这里加载出来的 file_state 的 key 应该是已经 norm_path 处理过的（如果是从上次保存读取的）
    # 但为了保险，我们在读取 load_state 后可以做一次规范化，或者假设它是规范的
    for old_path in file_state:
        if old_path not in new_file_state:
            try:
                source_id = os.path.basename(old_path)
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 检测到文件已删除: {old_path}")
                db.delete_by_source(source_id)
                deleted_count += 1
            except Exception as e:
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 删除失效索引失败: {e}")

    # 保存最新状态
    save_state(new_file_state)
                    
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]扫描完成！同步 {count} 个文件，跳过 {skip_count} 个文件，移除 {deleted_count} 个失效文件。")

    # 启动时如果有更新，统一记录一次日志
    if batch_update_count > 0:
        try:
            db._log_to_backend(f"{batch_update_count}条RAG 向量索引更新完成", "AI Engine")
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]已触发启动时批量更新日志同步。")
        except Exception as e:
            print(f"日志同步失败: {e}")
    # ==========================================

    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]正在监控目录: {WATCH_DIR}")
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]当你修改、保存 .java 文件时，系统会自动更新向量数据库。")
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]按 Ctrl+C 停止服务。")

    event_handler = CodeChangeHandler(new_file_state)
    observer = Observer()
    observer.schedule(event_handler, WATCH_DIR, recursive=True)
    observer.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    
    observer.join()
