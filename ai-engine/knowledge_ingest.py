# -*- coding: utf-8 -*-
import os
import re
from typing import List, Dict

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
        
        # 提取类定义 (Class Definition)
        # 简单的正则匹配类定义，处理注解
        class_pattern = re.compile(r'((?:@[\w\(\)\"\s=]+?\s+)*)public\s+(?:abstract\s+)?(?:class|interface|enum)\s+(\w+)(?:.*?)?\{', re.DOTALL)
        
        class_match = class_pattern.search(content)
        if class_match:
            annotations = class_match.group(1).strip()
            class_name = class_match.group(2)
            
            # 提取类定义的代码块（高层概览）
        class_info = {
            "type": "class_definition",
            "name": class_name,
            "package": package_name,
            "content": f"Package: {package_name}\nClass: {class_name}\nAnnotations: {annotations}\n",
            "file_path": file_path
        }
        chunks.append(class_info)
        
        # ==============================
        # 针对实体类 (Entity) 的增强提取
        # ==============================
        if "TableName" in annotations or "Entity" in annotations:
            # 提取表名
            table_name = "Unknown"
            tb_match = re.search(r'@TableName\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', annotations)
            if tb_match:
                table_name = tb_match.group(1)
            
            # 提取所有字段 (Field)
            fields = []
            field_pattern = re.compile(r'((?:@[\w\(\)\"\s=]+?\s+)*)private\s+(\w+)\s+(\w+);', re.DOTALL)
            for f_match in field_pattern.finditer(content):
                f_anno = f_match.group(1).strip() if f_match.group(1) else ""
                f_type = f_match.group(2)
                f_name = f_match.group(3)
                
                # 尝试提取 @Schema 或 JavaDoc 注释作为字段描述
                f_desc = ""
                schema_match = re.search(r'@Schema\s*\(\s*description\s*=\s*["\']([^"\']+)["\']', f_anno)
                if schema_match:
                    f_desc = schema_match.group(1)
                
                fields.append(f"- {f_name} ({f_type}): {f_desc}")
            
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

if __name__ == "__main__":
    # 测试代码
    parser = JavaParser()
    test_file = r"d:\study\SwiftBoot\swiftboot-backend\swiftboot-admin\src\main\java\com\swiftboot\admin\controller\SysAiController.java"
    
    if os.path.exists(test_file):
        print(f"正在解析文件: {test_file}")
        results = parser.parse_file(test_file)
        for chunk in results:
            print("-" * 50)
            print(f"Type: {chunk['type']}")
            print(f"Name: {chunk['name']}")
            print(f"Content Preview:\n{chunk['content'][:200]}...")
    else:
        print(f"文件不存在: {test_file}")
