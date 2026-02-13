# -*- coding: utf-8 -*-
import os
import re
from typing import List, Dict
import xml.etree.ElementTree as ET


class MapperXmlParser:
    """
    MyBatis Mapper XML 解析器
    负责解析 Mapper.xml 文件，提取 SQL 语句和业务逻辑。
    这对于理解深层业务逻辑（如连表查询、条件过滤）至关重要。
    """
    
    def parse_file(self, file_path: str) -> List[Dict]:
        """
        解析单个 Mapper.xml 文件并返回 SQL 代码块列表。
        """
        if not os.path.exists(file_path):
            return []
        
        chunks = []
        
        try:
            # 读取文件内容
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 提取 namespace（通常是对应的 Mapper 接口全限定名）
            namespace_match = re.search(r'namespace\s*=\s*["\']([^"\']+)["\']', content)
            namespace = namespace_match.group(1) if namespace_match else ""
            mapper_name = namespace.split('.')[-1] if namespace else os.path.basename(file_path)
            
            # 解析 XML
            # 移除 DOCTYPE 声明以避免解析错误
            content_clean = re.sub(r'<!DOCTYPE[^>]*>', '', content)
            
            try:
                root = ET.fromstring(content_clean)
            except ET.ParseError:
                # 如果 XML 解析失败，使用正则表达式提取
                return self._parse_with_regex(file_path, content, namespace, mapper_name)
            
            # 提取 SQL 语句块
            sql_tags = ['select', 'insert', 'update', 'delete']
            
            for tag in sql_tags:
                for elem in root.findall(f'.//{tag}'):
                    sql_id = elem.get('id', 'unknown')
                    result_type = elem.get('resultType', elem.get('resultMap', ''))
                    param_type = elem.get('parameterType', '')
                    
                    # 提取完整的 SQL 文本（包括动态 SQL 标签）
                    sql_text = self._extract_sql_text(elem)
                    
                    # 分析 SQL 特征
                    features = self._analyze_sql_features(sql_text)
                    
                    # 构建描述
                    desc = f"Mapper: {mapper_name}\n"
                    desc += f"Method: {sql_id}\n"
                    desc += f"Type: {tag.upper()}\n"
                    if result_type:
                        desc += f"ResultType: {result_type}\n"
                    if param_type:
                        desc += f"ParamType: {param_type}\n"
                    if features:
                        desc += f"Features: {', '.join(features)}\n"
                    desc += f"\nSQL:\n{sql_text}\n"
                    
                    chunk = {
                        "type": "mapper_sql",
                        "name": f"{mapper_name}.{sql_id}",
                        "mapper": mapper_name,
                        "sql_type": tag,
                        "content": desc,
                        "file_path": file_path
                    }
                    chunks.append(chunk)
            
            # 提取 resultMap 定义
            for result_map in root.findall('.//resultMap'):
                map_id = result_map.get('id', 'unknown')
                map_type = result_map.get('type', '')
                
                # 提取字段映射
                mappings = []
                for child in result_map:
                    if child.tag in ['id', 'result']:
                        column = child.get('column', '')
                        prop = child.get('property', '')
                        mappings.append(f"  {column} -> {prop}")
                    elif child.tag == 'association':
                        prop = child.get('property', '')
                        java_type = child.get('javaType', '')
                        mappings.append(f"  [关联] {prop} ({java_type})")
                    elif child.tag == 'collection':
                        prop = child.get('property', '')
                        of_type = child.get('ofType', '')
                        mappings.append(f"  [集合] {prop} ({of_type})")
                
                if mappings:
                    desc = f"Mapper: {mapper_name}\n"
                    desc += f"ResultMap: {map_id}\n"
                    desc += f"Type: {map_type}\n"
                    desc += f"Mappings:\n" + "\n".join(mappings)
                    
                    chunk = {
                        "type": "result_map",
                        "name": f"{mapper_name}.{map_id}",
                        "mapper": mapper_name,
                        "content": desc,
                        "file_path": file_path
                    }
                    chunks.append(chunk)
            
        except Exception as e:
            print(f"[MapperXmlParser] 解析失败: {file_path}, 错误: {e}")
        
        return chunks
    
    def _extract_sql_text(self, elem) -> str:
        """
        提取 XML 元素中的完整 SQL 文本，包括动态 SQL 标签。
        """
        def get_text_recursive(element, depth=0):
            parts = []
            indent = "  " * depth
            
            # 添加元素的文本内容
            if element.text and element.text.strip():
                parts.append(element.text.strip())
            
            # 递归处理子元素
            for child in element:
                tag = child.tag
                
                if tag == 'if':
                    test = child.get('test', '')
                    parts.append(f"\n{indent}<if test=\"{test}\">")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</if>")
                elif tag == 'where':
                    parts.append(f"\n{indent}<where>")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</where>")
                elif tag == 'set':
                    parts.append(f"\n{indent}<set>")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</set>")
                elif tag == 'foreach':
                    collection = child.get('collection', '')
                    item = child.get('item', '')
                    parts.append(f"\n{indent}<foreach collection=\"{collection}\" item=\"{item}\">")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</foreach>")
                elif tag == 'choose':
                    parts.append(f"\n{indent}<choose>")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</choose>")
                elif tag in ['when', 'otherwise']:
                    test = child.get('test', '') if tag == 'when' else ''
                    parts.append(f"\n{indent}<{tag}" + (f' test=\"{test}\">' if test else '>'))
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</{tag}>")
                elif tag == 'include':
                    refid = child.get('refid', '')
                    parts.append(f"\n{indent}<include refid=\"{refid}\"/>")
                elif tag == 'trim':
                    prefix = child.get('prefix', '')
                    suffix = child.get('suffix', '')
                    parts.append(f"\n{indent}<trim prefix=\"{prefix}\" suffix=\"{suffix}\">")
                    parts.append(get_text_recursive(child, depth + 1))
                    parts.append(f"{indent}</trim>")
                else:
                    # 其他标签直接提取文本
                    if child.text and child.text.strip():
                        parts.append(child.text.strip())
                
                # 添加尾部文本
                if child.tail and child.tail.strip():
                    parts.append(child.tail.strip())
            
            return " ".join(parts)
        
        return get_text_recursive(elem)
    
    def _analyze_sql_features(self, sql_text: str) -> List[str]:
        """
        分析 SQL 的特征，帮助理解业务逻辑。
        """
        features = []
        sql_lower = sql_text.lower()
        
        if 'left join' in sql_lower or 'inner join' in sql_lower or 'right join' in sql_lower:
            features.append("连表查询")
        if 'group by' in sql_lower:
            features.append("分组聚合")
        if 'order by' in sql_lower:
            features.append("排序")
        if 'limit' in sql_lower:
            features.append("分页")
        if 'del_flag' in sql_lower:
            features.append("逻辑删除过滤")
        if 'data_scope' in sql_lower or '${params.dataScope}' in sql_text:
            features.append("数据权限控制")
        if '<if' in sql_text or '<where>' in sql_text:
            features.append("动态条件")
        if '<foreach' in sql_text:
            features.append("批量操作")
        if 'union' in sql_lower:
            features.append("联合查询")
        if 'distinct' in sql_lower:
            features.append("去重")
        
        return features
    
    def _parse_with_regex(self, file_path: str, content: str, namespace: str, mapper_name: str) -> List[Dict]:
        """
        使用正则表达式解析（作为 XML 解析的备选方案）。
        """
        chunks = []
        
        # 匹配 select/insert/update/delete 标签
        sql_pattern = re.compile(
            r'<(select|insert|update|delete)\s+id\s*=\s*["\']([^"\']+)["\'][^>]*>(.*?)</\1>',
            re.DOTALL | re.IGNORECASE
        )
        
        for match in sql_pattern.finditer(content):
            sql_type = match.group(1).lower()
            sql_id = match.group(2)
            sql_content = match.group(3).strip()
            
            # 清理 SQL 内容
            sql_clean = re.sub(r'<[^>]+>', ' ', sql_content)  # 移除 XML 标签
            sql_clean = re.sub(r'\s+', ' ', sql_clean).strip()  # 合并空白
            
            features = self._analyze_sql_features(sql_content)
            
            desc = f"Mapper: {mapper_name}\n"
            desc += f"Method: {sql_id}\n"
            desc += f"Type: {sql_type.upper()}\n"
            if features:
                desc += f"Features: {', '.join(features)}\n"
            desc += f"\nSQL:\n{sql_content}\n"
            
            chunk = {
                "type": "mapper_sql",
                "name": f"{mapper_name}.{sql_id}",
                "mapper": mapper_name,
                "sql_type": sql_type,
                "content": desc,
                "file_path": file_path
            }
            chunks.append(chunk)
        
        return chunks


class PythonParser:
    """
    Python 代码解析器
    负责解析 Python 文件，提取函数和类定义。
    """
    
    def parse_file(self, file_path: str) -> List[Dict]:
        """
        解析单个 Python 文件并返回代码块列表。
        """
        if not os.path.exists(file_path):
            return []
            
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        chunks = []
        file_name = os.path.basename(file_path)
        
        # 1. 提取所有函数定义
        # 匹配 def func_name(args): ...
        func_pattern = re.compile(r'(?:^|\n)(\s*)def\s+(\w+)\s*\((.*?)\)(?:\s*->\s*[^:]+)?:\s*(?:\"\"\"(.*?)\"\"\")?', re.DOTALL)
        
        for match in func_pattern.finditer(content):
            indent = match.group(1)
            func_name = match.group(2)
            args = match.group(3)
            docstring = match.group(4).strip() if match.group(4) else ""
            
            # 简单提取函数体 (假设缩进一致，比较粗糙但有效)
            start_pos = match.start()
            # 寻找下一个 def 或 class 或文件结束
            next_match = func_pattern.search(content, match.end())
            end_pos = next_match.start() if next_match else len(content)
            
            func_body = content[start_pos:end_pos].strip()
            
            desc = f"Function: {func_name}\nFile: {file_name}\nArgs: {args}\n"
            if docstring:
                desc += f"Docstring: {docstring}\n"
            desc += f"\nImplementation:\n{func_body}\n"
            
            chunk = {
                "type": "python_function",
                "name": func_name,
                "file": file_name,
                "content": desc,
                "file_path": file_path
            }
            chunks.append(chunk)
            
        # 2. 如果没有提取到函数（可能是配置类或脚本），则把整个文件作为一个块
        if not chunks and content.strip():
             chunk = {
                "type": "python_script",
                "name": file_name,
                "file": file_name,
                "content": f"Script: {file_name}\n\nContent:\n{content}",
                "file_path": file_path
            }
             chunks.append(chunk)
             
        return chunks


class JavaParser:
    """
    Java 代码解析器
    负责解析 Java 文件，提取类结构、方法和注释信息。
    """
    
    def parse_file(self, file_path: str) -> List[Dict]:
        """
        解析单个 Java 文件并返回代码块列表（类/方法）。
        """
        if not os.path.exists(file_path):
            return []
            
        # 强制使用 utf-8 读取，防止中文乱码
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        chunks = []
        
        # 提取包名 (Package)
        package_match = re.search(r'package\s+([\w\.]+);', content)
        package_name = package_match.group(1) if package_match else ""
        
        # 提取导入 (Imports)
        imports = re.findall(r'import\s+([\w\.]+);', content)
        # 只保留关键导入，过滤掉 standard java imports 以节省 tokens
        filtered_imports = [imp for imp in imports if not imp.startswith('java.util.') and not imp.startswith('java.lang.')]
        imports_str = "\n".join([f"import {imp};" for imp in filtered_imports])
        
        # 提取类定义 (Class Definition)
        # 增强正则，匹配 extends 和 implements
        class_pattern = re.compile(r'((?:@[\w\(\)\"\s=]+?\s+)*)public\s+(?:abstract\s+)?(?:class|interface|enum)\s+(\w+)(?:.*?)?\{', re.DOTALL)
        
        # 初始化变量，防止未匹配到类时报错
        class_name = "Unknown"
        annotations = ""
        fields = []
        
        class_match = class_pattern.search(content)
        if class_match:
            annotations = class_match.group(1).strip()
            class_name = class_match.group(2)
            full_class_def = class_match.group(0).strip()[:-1] # 移除末尾的 {
            
            # 提取所有字段 (Fields) - 通用提取
            fields = []
            field_pattern = re.compile(r'((?:@[\w\(\)\"\s=]+?\s+)*)private\s+(?:static\s+)?(?:final\s+)?([\w<>\[\]]+)\s+(\w+)\s*(?:=.*?)?;', re.DOTALL)
            for f_match in field_pattern.finditer(content):
                f_anno = f_match.group(1).strip() if f_match.group(1) else ""
                f_type = f_match.group(2)
                f_name = f_match.group(3)
                
                # 尝试提取 @Schema 或 JavaDoc 注释作为字段描述
                f_desc = ""
                schema_match = re.search(r'@Schema\s*\(\s*description\s*=\s*["\']([^"\']+)["\']', f_anno)
                if schema_match:
                    f_desc = schema_match.group(1)
                
                # 简化注解显示，只标记关键注解
                anno_marker = ""
                if "@Autowired" in f_anno or "@Resource" in f_anno:
                    anno_marker = " [Injected]"
                elif "@TableField" in f_anno:
                     anno_marker = " [DB_Field]"
                
                fields.append(f"- {f_name} ({f_type}){anno_marker}: {f_desc}")
            
            # 提取所有方法签名 (Method Signatures) - 用于类摘要
            method_signatures = []
            # 排除构造函数
            sig_pattern = re.compile(r'public\s+(?:static\s+)?(?:<[\w\s,?]+>\s+)?([\w<>\[\]]+)\s+(\w+)\s*\((.*?)\)', re.DOTALL)
            for s_match in sig_pattern.finditer(content):
                m_ret = s_match.group(1).strip()
                m_name = s_match.group(2).strip()
                m_args = s_match.group(3).strip()
                if m_name != class_name:
                     method_signatures.append(f"+ {m_name}({m_args}) -> {m_ret}")

            # 构建增强版类描述 (Class Summary)
            # 包含：包名、关键导入、类定义、字段列表、方法签名列表
            class_desc = f"Package: {package_name}\n"
            if imports_str:
                class_desc += f"Imports:\n{imports_str}\n"
            class_desc += f"\nDefinition:\n{full_class_def}\n"
            
            if fields:
                class_desc += f"\nFields ({len(fields)}):\n" + "\n".join(fields[:30]) # 限制数量
                if len(fields) > 30:
                    class_desc += f"\n... (+{len(fields)-30} more)"
            
            if method_signatures:
                class_desc += f"\n\nMethods Summary ({len(method_signatures)}):\n" + "\n".join(method_signatures)

            # 提取类定义的代码块（高层概览）
            class_info = {
                "type": "class_definition",
                "name": class_name,
                "package": package_name,
                "content": class_desc,
                "file_path": file_path
            }
            chunks.append(class_info)
        
        # ==============================
        # 针对实体类 (Entity) 的兼容处理
        # ==============================
        # 依然保留 entity_info，因为它的格式专门优化过用于理解数据库结构
        if "TableName" in annotations or "Entity" in annotations:
            # 提取表名
            table_name = "Unknown"
            tb_match = re.search(r'@TableName\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', annotations)
            if tb_match:
                table_name = tb_match.group(1)
            
            # 复用上面提取的 fields，但这里不需要重新提取
            # 创建专门的“数据库表结构”知识块
            entity_content = f"Database Table: {table_name}\nEntity Class: {class_name}\nFields:\n" + "\n".join(fields)
            
            entity_info = {
                "type": "database_schema",
                "name": table_name,
                "class": class_name,
                "package": package_name,
                "content": entity_content,
                "file_path": file_path
            }
            chunks.append(entity_info)
        # ==============================
        
        # 提取方法定义 (Methods)
        # 匹配 public 方法，提取注解、返回值、方法名、参数
        # 排除构造函数（通常没有返回值类型）
        method_pattern = re.compile(r'((?:@[\w\(\")\s=,\{\}\./\-]+?\s+)*)public\s+(?:static\s+)?(?:<[\w\s,?]+>\s+)?([\w<>\[\]]+)\s+(\w+)\s*\((.*?)\)(?:.*?)(?:\{|;)', re.DOTALL)
        
        for m_match in method_pattern.finditer(content):
            m_annotations = m_match.group(1).strip() if m_match.group(1) else ""
            m_return_type = m_match.group(2).strip()
            m_name = m_match.group(3).strip()
            m_params = m_match.group(4).strip()
            m_full_match = m_match.group(0) # 完整匹配到的头部
            
            # 忽略构造函数（通常类名与方法名相同，或无返回类型，但正则已部分处理）
            if m_name == class_name:
                continue
            
            # ==============================
            # 提取方法体 (Bracket Matching)
            # ==============================
            method_body = ""
            start_idx = m_match.end() - 1 # '{' 或 ';' 的位置
            
            if m_full_match.strip().endswith(";"):
                # 抽象方法或接口方法，无方法体
                method_body = "(Abstract Method / Interface Method)"
            elif content[start_idx] == '{':
                # 有方法体，利用栈匹配括号
                bracket_count = 0
                body_end_idx = start_idx
                found_body = False
                
                for i, char in enumerate(content[start_idx:], start=start_idx):
                    if char == '{':
                        bracket_count += 1
                    elif char == '}':
                        bracket_count -= 1
                    
                    if bracket_count == 0:
                        body_end_idx = i + 1
                        found_body = True
                        break
                
                if found_body:
                    method_body = content[start_idx:body_end_idx]
            
            # ==============================
            
            # 提取 Controller 路径信息
            api_path = ""
            http_method = ""
            if "Controller" in class_name:
                # 尝试从方法注解中提取 API 路径 (如 @GetMapping("/list"))
                path_match = re.search(r'@(Get|Post|Put|Delete|Patch|RequestMapping)Mapping\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', m_annotations)
                if path_match:
                    http_method = path_match.group(1).upper()
                    api_path = path_match.group(2)
                    
                    # 结合类级别的 @RequestMapping
                    class_mapping = re.search(r'@RequestMapping\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', annotations)
                    if class_mapping:
                        base_path = class_mapping.group(1)
                        if not base_path.endswith('/') and not api_path.startswith('/'):
                            api_path = f"{base_path}/{api_path}"
                        else:
                            api_path = f"{base_path}{api_path}"
                            
            # 构建方法描述
            # 格式：Header + Body
            method_desc = f"Method: {m_name}\nReturn: {m_return_type}\nParams: {m_params}\n"
            if api_path:
                method_desc += f"API: {http_method} {api_path}\n"
            method_desc += f"Annotations: {m_annotations}\n"
            
            # 尝试提取方法上方的 JavaDoc 注释
            # 简单的向后搜索 */
            method_start_pos = m_match.start()
            pre_text = content[:method_start_pos].strip()
            if pre_text.endswith("*/"):
                comment_start = pre_text.rfind("/**")
                if comment_start != -1:
                    javadoc = pre_text[comment_start:]
                    # 清理 * 号
                    cleaned_doc = re.sub(r'/\*\*|\*/|\*\s?', '', javadoc).strip()
                    method_desc += f"Description: {cleaned_doc}\n"
            
            # 追加具体实现代码
            method_desc += f"\nImplementation:\n{method_body}\n"

            method_info = {
                "type": "method_definition",
                "name": m_name,
                "class": class_name,
                "package": package_name,
                "api_path": api_path,
                "content": method_desc,
                "file_path": file_path
            }
            chunks.append(method_info)
        
        return chunks


class MarkdownParser:
    """
    Markdown 文档解析器
    负责解析 .md 文件，按标题层级进行切片。
    """
    def parse_file(self, file_path: str) -> List[Dict]:
        if not os.path.exists(file_path):
            return []
            
        chunks = []
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            lines = content.split('\n')
            current_chunk = []
            current_header = "Intro"
            file_name = os.path.basename(file_path)
            
            for line in lines:
                if line.strip().startswith('#'):
                    # 保存上一个块
                    if current_chunk:
                        text = "\n".join(current_chunk).strip()
                        if text:
                            chunks.append({
                                "type": "markdown_section",
                                "name": f"{file_name}#{current_header}",
                                "content": f"File: {file_name}\nSection: {current_header}\n\n{text}",
                                "file_path": file_path
                            })
                    
                    # 开始新块
                    current_header = line.strip().lstrip('#').strip()
                    current_chunk = [line]
                else:
                    current_chunk.append(line)
            
            # 保存最后一个块
            if current_chunk:
                text = "\n".join(current_chunk).strip()
                if text:
                    chunks.append({
                        "type": "markdown_section",
                        "name": f"{file_name}#{current_header}",
                        "content": f"File: {file_name}\nSection: {current_header}\n\n{text}",
                        "file_path": file_path
                    })
                    
        except Exception as e:
            print(f"Error parsing Markdown {file_path}: {e}")
            
        return chunks

class TypeScriptParser:
    """
    TypeScript/JavaScript 代码解析器
    """
    def parse_file(self, file_path: str) -> List[Dict]:
        if not os.path.exists(file_path):
            return []
            
        chunks = []
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            file_name = os.path.basename(file_path)
            
            # 简单提取 export const/function/class
            # 1. Exported Functions
            func_pattern = re.compile(r'export\s+(?:async\s+)?function\s+(\w+)\s*\(', re.MULTILINE)
            for match in func_pattern.finditer(content):
                func_name = match.group(1)
                chunks.append({
                    "type": "ts_function",
                    "name": func_name,
                    "content": f"File: {file_name}\nFunction: {func_name}\n\n(See file content for implementation)", # 简化，避免正则匹配括号的复杂性
                    "file_path": file_path
                })
                
            # 2. Exported Interfaces/Types
            type_pattern = re.compile(r'export\s+(?:interface|type)\s+(\w+)', re.MULTILINE)
            for match in type_pattern.finditer(content):
                type_name = match.group(1)
                chunks.append({
                    "type": "ts_type",
                    "name": type_name,
                    "content": f"File: {file_name}\nType: {type_name}",
                    "file_path": file_path
                })

            # 如果没有提取到特定结构，整个文件作为一个块
            if not chunks:
                 chunks.append({
                    "type": "ts_code",
                    "name": file_name,
                    "content": f"File: {file_name}\n\n{content}",
                    "file_path": file_path
                })
                
        except Exception as e:
            print(f"Error parsing TS file {file_path}: {e}")
            
        return chunks

class VueComponentParser:
    """
    Vue 组件解析器
    负责解析 .vue 文件，将其拆分为 Template (UI)、Script (逻辑)、Style (样式) 三个维度的切片。
    """
    
    def parse_file(self, file_path: str) -> List[Dict]:
        """
        解析单个 .vue 文件并返回知识切片列表。
        """
        if not os.path.exists(file_path):
            return []
        
        chunks = []
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            file_name = os.path.basename(file_path)
            component_name = file_name.replace('.vue', '')
            
            # 1. 提取 Template (UI 结构)
            template_match = re.search(r'<template>(.*?)</template>', content, re.DOTALL)
            if template_match:
                template_content = template_match.group(1).strip()
                
                # 提取关键 UI 元素和绑定
                ui_elements = self._extract_ui_elements(template_content)
                
                desc = f"Component: {component_name}\nType: UI Template\n"
                desc += f"Key Elements: {', '.join(ui_elements[:10])}\n"
                desc += f"\nCode:\n{template_content}\n"
                
                chunks.append({
                    "type": "frontend_ui",
                    "name": f"{component_name}.template",
                    "component": component_name,
                    "content": desc,
                    "file_path": file_path
                })
            
            # 2. 提取 Script (业务逻辑)
            script_match = re.search(r'<script.*?>(.*?)</script>', content, re.DOTALL)
            if script_match:
                script_content = script_match.group(1).strip()
                
                # 提取 API 调用、状态变量、方法
                api_calls = self._extract_api_calls(script_content)
                methods = self._extract_methods(script_content)
                state_vars = self._extract_state_vars(script_content)
                
                desc = f"Component: {component_name}\nType: Business Logic\n"
                if api_calls:
                    desc += f"API Calls: {', '.join(api_calls)}\n"
                if methods:
                    desc += f"Methods: {', '.join(methods)}\n"
                if state_vars:
                    desc += f"State: {', '.join(state_vars)}\n"
                desc += f"\nCode:\n{script_content}\n"
                
                chunks.append({
                    "type": "frontend_logic",
                    "name": f"{component_name}.script",
                    "component": component_name,
                    "content": desc,
                    "file_path": file_path
                })
                
        except Exception as e:
            print(f"Error parsing Vue file {file_path}: {e}")
            
        return chunks

    def _extract_ui_elements(self, template: str) -> List[str]:
        """提取 UI 关键元素，如按钮、表单、自定义组件"""
        elements = []
        # 匹配组件名 <MyComponent ...>
        tags = re.findall(r'<([A-Z][a-zA-Z0-9]+)', template)
        elements.extend(tags)
        
        # 匹配关键指令 v-if, v-for, @click
        if 'v-if' in template: elements.append('Condition(v-if)')
        if 'v-for' in template: elements.append('List(v-for)')
        if '@click' in template: elements.append('Event(click)')
        
        return list(set(elements))

    def _extract_api_calls(self, script: str) -> List[str]:
        """提取 API 调用函数名"""
        return re.findall(r'import\s+\{\s*([^}]+)\s*\}\s+from\s+[\'"]@/api/', script)

    def _extract_methods(self, script: str) -> List[str]:
        """提取方法定义"""
        return re.findall(r'const\s+([a-zA-Z0-9_]+)\s*=\s*(?:async)?\s*\(\s*\)\s*=>', script)

    def _extract_state_vars(self, script: str) -> List[str]:
        """提取响应式变量 ref/reactive"""
        return re.findall(r'const\s+([a-zA-Z0-9_]+)\s*=\s*(?:ref|reactive)\(', script)

if __name__ == "__main__":
    # 测试 Java 解析器
    print("=" * 60)
    print("测试 Java 解析器")
    print("=" * 60)
    
    java_parser = JavaParser()
    test_java_file = r"d:\study\SwiftBoot\swiftboot-backend\swiftboot-admin\src\main\java\com\swiftboot\admin\controller\SysAiController.java"
    
    if os.path.exists(test_java_file):
        print(f"正在解析文件: {test_java_file}")
        results = java_parser.parse_file(test_java_file)
        for chunk in results:
            print("-" * 50)
            print(f"Type: {chunk['type']}")
            print(f"Name: {chunk['name']}")
            print(f"Content Preview:\n{chunk['content'][:200]}...")
    else:
        print(f"文件不存在: {test_java_file}")
    
    # 测试 Mapper XML 解析器
    print("\n" + "=" * 60)
    print("测试 Mapper XML 解析器")
    print("=" * 60)
    
    mapper_parser = MapperXmlParser()
    # 查找项目中的 Mapper.xml 文件
    mapper_base = r"d:\study\SwiftBoot\swiftboot-backend"
    
    for root, dirs, files in os.walk(mapper_base):
        for file in files:
            if file.endswith("Mapper.xml"):
                xml_path = os.path.join(root, file)
                print(f"\n正在解析: {xml_path}")
                results = mapper_parser.parse_file(xml_path)
                for chunk in results[:3]:  # 只显示前 3 个结果
                    print("-" * 50)
                    print(f"Type: {chunk['type']}")
                    print(f"Name: {chunk['name']}")
                    preview = chunk['content'][:300] if len(chunk['content']) > 300 else chunk['content']
                    print(f"Content Preview:\n{preview}...")
                if len(results) > 3:
                    print(f"... 共 {len(results)} 个代码块")
                break  # 只测试第一个找到的文件
