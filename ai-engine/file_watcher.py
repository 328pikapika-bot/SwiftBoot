import time
import os
import json
import hashlib
import signal
import sys
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
    "quick-start",
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

def build_rag_sync_log_title(action, count, aggregate_type=None, initial=False):
    if initial:
        if aggregate_type:
            return f"RAG 向量索引更新完成: {action} {count} 个【{aggregate_type}】知识切片"
        return f"RAG 向量索引更新完成: {action} {count} 个知识切片"
    if aggregate_type:
        return f"RAG 向量索引更新完成: {action} {count} 个【{aggregate_type}】切片"
    return f"RAG 向量索引更新完成: {action} {count} 个切片"

def emit_rag_sync_logs(db, summary, action, initial=False):
    for aggregate_type, count in summary.items():
        if count <= 0:
            continue
        db._log_to_backend(
            build_rag_sync_log_title(action, count, aggregate_type, initial),
            "AI Engine"
        )

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
            source_id = filename.replace("\\", "/")
            norm_path = os.path.normpath(filename).lower()
            action = "更新" if norm_path in self.file_state else "新增"

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
                summary = self.db.add_documents(chunks)
                print(f"[{self._now()}]更新完成: {source_id} ({action} {len(chunks)} 个切片)")
                
                # 5. 仅记录聚合后的索引更新结果，不再写入中间过程日志
                try:
                    emit_rag_sync_logs(self.db, summary, action, initial=False)
                except Exception as log_err:
                    print(f"[{self._now()}]日志记录失败: {log_err}")
            else:
                print(f"[{self._now()}]警告: 未提取到有效代码切片")

            # 6. 更新状态文件
            self.file_state[norm_path] = now
            save_state(self.file_state)
            
        except Exception as e:
            print(f"[{self._now()}]处理变更失败: {e}")

    def _now(self):
        return time.strftime("%Y-%m-%d %H:%M:%S")

def signal_handler(sig, frame):
    print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}] 接收到系统信号 {sig}，准备退出...")
    sys.exit(0)

if __name__ == "__main__":
    try:
        # 注册信号处理器
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)

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
        init_log_summary = {}
        
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
                if not any(file_path.endswith(ext) for ext in [".java", ".py", ".vue", ".md", ".ts", ".js"]):
                    continue
                    
                if file in IGNORE_FILENAMES:
                    continue
                    
                # 检查更新时间
                current_mtime = os.path.getmtime(file_path)
                norm_path = os.path.normpath(file_path).lower()
                new_file_state[norm_path] = current_mtime
                
                # 如果是首次运行，或者文件时间比记录的时间新，则解析
                if is_first_run or norm_path not in file_state or current_mtime > file_state[norm_path]:
                    try:
                        action = "更新" if (not is_first_run and norm_path in file_state) else "新增"
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
                            # 补充 source 字段
                            source_id = file_path.replace("\\", "/")
                            for chunk in chunks:
                                chunk['source'] = source_id
                            
                            # 先删除旧的
                            db.delete_by_source(source_id)
                            # 写入新的
                            summary = db.add_documents(chunks)
                            count += len(chunks)
                            print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]已索引: {file} ({len(chunks)} 切片)")
                            for aggregate_type, chunk_count in summary.items():
                                if chunk_count <= 0:
                                    continue
                                key = (action, aggregate_type)
                                init_log_summary[key] = init_log_summary.get(key, 0) + chunk_count
                    except Exception as e:
                        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]解析失败 {file}: {e}")
                else:
                    skip_count += 1
        
        # 记录初始化聚合日志
        if init_log_summary:
            try:
                for (action, aggregate_type), total in init_log_summary.items():
                    db._log_to_backend(
                        build_rag_sync_log_title(action, total, aggregate_type, initial=True),
                        "AI Engine"
                    )
            except:
                pass
                
        # 更新状态文件 (合并新旧状态，以防某些文件这次没扫描到但确实存在)
        # 注意：这里其实应该用 new_file_state 替换 file_state，以处理文件删除的情况
        # 但为了简单起见，且考虑到 file_state 可能很大，我们这里直接保存 new_file_state
        # 意味着如果文件被删除了，它就不在 new_file_state 中，下次会被视为新文件（如果它又回来了）
        # 或者更严谨的做法是处理 deleted files，这里暂不处理
        save_state(new_file_state)
        
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]初始化扫描完成！新增索引: {count}, 跳过未变更: {skip_count}")

        # 启动 Watchdog 监听
        event_handler = CodeChangeHandler(new_file_state)
        observer = Observer()
        observer.schedule(event_handler, WATCH_DIR, recursive=True)
        observer.start()
        
        print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}]服务已就绪，正在后台监听文件变更...")
        
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            observer.stop()
        observer.join()

    except SystemExit:
        print("\nWatcher 服务正常退出")
    except Exception as e:
        print(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}] Watcher 服务发生异常退出: {e}")
        import traceback
        traceback.print_exc()
        input("按回车键退出...")
