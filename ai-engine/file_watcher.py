import time
import os
import json
import threading
import hashlib
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from knowledge_ingest import JavaParser, PythonParser, VueComponentParser, MarkdownParser, TypeScriptParser
from vector_store import VectorStore

# ==========================================
# 配置
# ==========================================
# 监听的目录（递归监听）
# 使用相对路径：当前脚本所在目录(ai-engine)的上一级目录(SwiftBoot)
WATCH_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

# 白名单配置
ALLOWED_ROOT_DIRS = [
    "swiftboot-backend",
    "swiftboot-ui",
    "devDoc",
    "project-skills",
    "ai-engine",
    "快速启动",
    "release_notes"
]
ALLOWED_ROOT_FILES = ["README.md"]

# 黑名单配置 (子目录/文件)
IGNORE_DIRS = [".git", "target", "build", "node_modules", "dist", ".idea", ".vscode", "__pycache__", "chroma_db", "logs", "javadoc", "classes", "generated-sources", "public"]
IGNORE_FILENAMES = ["auto-imports.d.ts", "components.d.ts", "tsconfig.tsbuildinfo", "vite-env.d.ts", "typed-router.d.ts"]

# 状态文件路径
STATE_FILE = os.path.join(os.path.dirname(__file__), "file_state.json")

def get_file_hash(filepath):
    """计算文件内容的 MD5 哈希"""
    try:
        if not os.path.exists(filepath):
            return None
        with open(filepath, "rb") as f:
            return hashlib.md5(f.read()).hexdigest()
    except Exception:
        return None

def load_state():
    if os.path.exists(STATE_FILE):
        try:
            with open(STATE_FILE, 'r', encoding='utf-8') as f:
                content = f.read().strip()
                if not content:
                    return {}
                state = json.loads(content)
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 成功加载状态文件，共 {len(state)} 条记录。")
                return state
        except Exception as e:
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 状态文件解析异常 (将重新生成): {e}")
            return {}
    else:
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 状态文件不存在，将执行全量扫描。")
        return {}

def save_state(state):
    try:
        with open(STATE_FILE, 'w', encoding='utf-8') as f:
            json.dump(state, f, ensure_ascii=False, indent=2)
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
        self.md_parser = MarkdownParser()
        self.ts_parser = TypeScriptParser()
        self.db = VectorStore()
        self.last_processed = {}  # 简单的防抖动机制 {path: timestamp}
        self.file_hashes = {}     # 内存中缓存文件内容哈希 {path: md5}
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
        if not event.is_directory and self._is_allowed_path(event.src_path):
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
        if not event.is_directory and self._is_allowed_path(event.src_path):
            file_name = os.path.basename(event.src_path)
            print(f"\n[{self._now()}][检测到文件删除] {file_name}")
            # 从向量库中移除该文件的所有切片
            self.db.delete_by_source(file_name)
            # 更新状态
            norm_src = os.path.normpath(event.src_path).lower()
            if norm_src in self.file_state:
                del self.file_state[norm_src]
                save_state(self.file_state)

    def _is_valid_ext(self, filename):
        valid_exts = (".java", ".py", ".md", ".doc", ".vue", ".ts")
        return filename.lower().endswith(valid_exts)

    def _is_allowed_path(self, filename):
        """
        基于白名单和黑名单检查文件是否允许被处理
        """
        rel_path = os.path.relpath(filename, WATCH_DIR)
        path_parts = rel_path.split(os.sep)
        
        # 1. 检查是否在允许的根目录中
        top_dir = path_parts[0]
        
        # 如果是根目录下的文件
        if len(path_parts) == 1:
            return top_dir in ALLOWED_ROOT_FILES
            
        # 如果是目录中的文件
        if top_dir not in ALLOWED_ROOT_DIRS:
            return False
            
        # 2. 检查黑名单 (子目录)
        for part in path_parts:
            if part in IGNORE_DIRS:
                return False
                
        # 3. 检查黑名单 (文件名)
        if os.path.basename(filename).lower() in [f.lower() for f in IGNORE_FILENAMES]:
            return False
            
        # 4. 检查扩展名
        # 排除 .d.ts 定义文件
        if filename.lower().endswith(".d.ts"):
            return False
            
        # 5. 排除临时文件和自动生成文件
        if filename.lower().endswith(".tmp") or "~" in filename:
            return False
            
        return self._is_valid_ext(filename)

    def _process_event(self, event):
        """
        统一处理文件变更的核心逻辑
        """
        if event.is_directory:
            return
        
        filename = event.src_path
        
        # 使用新的白名单逻辑检查
        if not self._is_allowed_path(filename):
            return

        # 简单的防抖动：3秒内不重复处理同一个文件
        now = time.time()
        if filename in self.last_processed:
            if now - self.last_processed[filename] < 3:
                return
        self.last_processed[filename] = now

        # 基于内容哈希的去重
        current_hash = get_file_hash(filename)
        if current_hash:
            if filename in self.file_hashes:
                if self.file_hashes[filename] == current_hash:
                    print(f"[{self._now()}] 检测到文件事件但内容未变，跳过: {filename}")
                    # 更新状态文件中的 mtime，防止下次启动时误判
                    norm_path = os.path.normpath(filename).lower()
                    self.file_state[norm_path] = now
                    save_state(self.file_state)
                    return
            self.file_hashes[filename] = current_hash

        # 区分前后端变更类型
        rel_path = os.path.relpath(filename, WATCH_DIR)
        top_dir = rel_path.split(os.sep)[0]
        
        change_type = "文件" # 默认类型
        
        # 后端规则
        if top_dir in ["swiftboot-backend", "ai-engine"]:
            if filename.lower().endswith((".java", ".py", ".md")):
                change_type = "后端"
        
        # 前端规则
        elif top_dir == "swiftboot-ui":
            if filename.lower().endswith((".md", ".doc", ".vue", ".ts")):
                change_type = "前端"
        
        print(f"\n[{self._now()}][检测到{change_type}变更] {filename}")
        
        try:
            # 1. 提取文件名作为 Source ID
            source_id = os.path.basename(filename)

            # 2. 清理旧数据 (先删后加，确保数据一致性)
            self.db.delete_by_source(source_id)

            # 3. 重新解析代码文件
            print(f"[{self._now()}]正在重新解析代码...")
            chunks = []
            if filename.endswith(".java"):
                chunks = self.java_parser.parse_file(filename)
            elif filename.endswith(".py"):
                chunks = self.py_parser.parse_file(filename)
            elif filename.endswith(".vue"):
                chunks = self.vue_parser.parse_file(filename)
            elif filename.endswith((".md", ".doc")):
                chunks = self.md_parser.parse_file(filename)
            elif filename.endswith((".ts", ".js")):
                chunks = self.ts_parser.parse_file(filename)
            
            if chunks:
                # 补充 source 字段
                for chunk in chunks:
                    chunk['source'] = source_id
                
                # 4. 写入向量数据库
                self.db.add_documents(chunks)
                print(f"[{self._now()}]更新完成: {source_id} (新增 {len(chunks)} 个切片)")
                
                # 5. 记录到后端操作日志 (通过 HTTP 调用)
                try:
                    msg = f"检测到{change_type}变更: {os.path.basename(filename)}"
                    self.db._log_to_backend(msg, "AI Engine")
                    
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
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]正在监控目录: {WATCH_DIR}")
    
    # ==========================================
    # 启动时增量代码扫描
    # ==========================================
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]正在执行代码扫描 (增量模式)...")
    
    # 实例化所有解析器
    java_parser = JavaParser()
    py_parser = PythonParser()
    vue_parser = VueComponentParser()
    md_parser = MarkdownParser()
    ts_parser = TypeScriptParser()
    
    db = VectorStore()
    
    # 加载上次的文件状态
    file_state = load_state()
    is_first_run = len(file_state) == 0
    if file_state:
        file_state = {os.path.normpath(k).lower(): v for k, v in file_state.items()}
        
    new_file_state = {}
    
    count = 0
    skip_count = 0
    batch_update_count = 0
    
    # 使用 os.walk 遍历，但进行剪枝优化
    for root, dirs, files in os.walk(WATCH_DIR):
        # 1. 剪枝：只允许特定的根目录
        if os.path.normpath(root) == os.path.normpath(WATCH_DIR):
            # 只保留白名单中的目录
            dirs[:] = [d for d in dirs if d in ALLOWED_ROOT_DIRS]
        else:
            # 2. 剪枝：排除黑名单中的子目录
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
            
        for file in files:
            file_path = os.path.join(root, file)
            
            # 使用统一的路径检查逻辑
            # 注意：我们需要实例化 CodeChangeHandler 才能调用 _is_allowed_path，或者提取为静态方法
            # 这里简单重写一下逻辑以保持独立性
            
            # 检查扩展名
            valid_exts = (".java", ".py", ".md", ".doc", ".vue", ".ts")
            if not file.lower().endswith(valid_exts):
                continue
                
            # 检查黑名单文件名
            if file.lower() in [f.lower() for f in IGNORE_FILENAMES]:
                continue
                
            # 排除 .d.ts
            if file.lower().endswith(".d.ts"):
                continue
                
            # 检查是否是根目录下的允许文件
            if os.path.normpath(root) == os.path.normpath(WATCH_DIR):
                if file not in ALLOWED_ROOT_FILES:
                    continue
            
            # 统一路径格式
            norm_path = os.path.normpath(file_path).lower()
            
            try:
                mtime = os.path.getmtime(file_path)
                new_file_state[norm_path] = mtime
                
                if norm_path in file_state and abs(file_state[norm_path] - mtime) < 0.1:
                    skip_count += 1
                    continue

                source_id = os.path.basename(file_path)
                db.delete_by_source(source_id)
                
                chunks = []
                if file.endswith(".java"):
                    chunks = java_parser.parse_file(file_path)
                elif file.endswith(".py"):
                    chunks = py_parser.parse_file(file_path)
                elif file.endswith(".vue"):
                    chunks = vue_parser.parse_file(file_path)
                elif file.endswith((".md", ".doc")):
                    chunks = md_parser.parse_file(file_path)
                elif file.endswith((".ts", ".js")):
                    chunks = ts_parser.parse_file(file_path)
                    
                if chunks:
                    for chunk in chunks:
                        chunk['source'] = source_id
                    db.add_documents(chunks)
                    batch_update_count += len(chunks)
                    count += 1
                    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] [{count}] 已同步变更: {source_id} ({len(chunks)} 块)")
            except Exception as e:
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]索引文件失败 {file}: {e}")

    # 处理删除文件
    deleted_count = 0
    for old_path in file_state:
        if old_path not in new_file_state:
            # 额外检查：只有当旧文件路径属于我们现在的关注范围时，才去尝试删除
            # 这样可以避免因为更改了白名单规则而误删之前索引的数据（虽然清理一下也好，但为了稳妥）
            # 这里简单起见，只要不在新状态里，就认为是删除了（或者不再被监控了），直接清理
            try:
                source_id = os.path.basename(old_path)
                # print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 检测到文件已删除或不再监控: {old_path}")
                db.delete_by_source(source_id)
                deleted_count += 1
            except Exception as e:
                print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] 删除失效索引失败: {e}")

    save_state(new_file_state)
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]扫描完成！同步 {count} 个文件，跳过 {skip_count} 个文件，移除 {deleted_count} 个失效文件。")

    if is_first_run:
        try:
            msg = f"初始化切片库完成，同步 {count} 个文件，跳过 {skip_count} 个文件，移除 {deleted_count} 个失效文件。"
            db._log_to_backend(msg, "AI Engine")
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]已触发初始化完成日志同步。")
        except Exception as e:
            print(f"日志同步失败: {e}")
    elif batch_update_count > 0:
        try:
            msg = f"{batch_update_count}条RAG 向量索引更新完成"
            db._log_to_backend(msg, "AI Engine")
            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]已触发启动时批量更新日志同步。")
        except Exception as e:
            print(f"日志同步失败: {e}")
            
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
