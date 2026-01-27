<template>
  <div class="chart-page">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <div class="search-header">
        <div class="title">
          <el-icon :size="24"><PieChart /></el-icon>
          <span>Element Plus 图表组件参考</span>
        </div>
      </div>
      <div class="tips">
        <el-tag type="info" effect="plain">
          <el-icon><InfoFilled /></el-icon>
          这里展示了 Element Plus 中常用的图表组件用法示例
        </el-tag>
      </div>
    </el-card>

    <!-- 图表示例区域 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span>图表组件示例</span>
      </template>

      <div class="chart-examples">
        <!-- 饼图示例 -->
        <div class="chart-example">
          <h4>饼图 (PieChart)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#409eff">
              <PieChart />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;PieChart /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>

        <!-- 柱状图示例 -->
        <div class="chart-example">
          <h4>柱状图 (Histogram)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#67c23a">
              <Histogram />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;Histogram /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>

        <!-- 折线图示例 -->
        <div class="chart-example">
          <h4>折线图 (DataLine)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#e6a23c">
              <DataLine />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;DataLine /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>

        <!-- 数据面板示例 -->
        <div class="chart-example">
          <h4>数据面板 (DataBoard)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#f56c6c">
              <DataBoard />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;DataBoard /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>

        <!-- 趋势图示例 -->
        <div class="chart-example">
          <h4>趋势图 (TrendCharts)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#9c27b0">
              <TrendCharts />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;TrendCharts /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>

        <!-- 数据分析示例 -->
        <div class="chart-example">
          <h4>数据分析 (DataAnalysis)</h4>
          <div class="chart-container">
            <el-icon :size="64" color="#ff9800">
              <DataAnalysis />
            </el-icon>
          </div>
          <div class="code-example">
            <pre><code>&lt;el-icon :size="32"&gt;
  &lt;DataAnalysis /&gt;
&lt;/el-icon&gt;</code></pre>
          </div>
        </div>
      </div>

      <!-- 使用说明 -->
      <div class="usage-guide">
        <h4>使用说明</h4>
        <div class="guide-content">
          <p>1. 首先在图标参考页面查看并复制所需的图表图标名称</p>
          <p>2. 在菜单管理中配置菜单时，将复制的图标名称填入图标字段</p>
          <p>3. 图表图标通常用于数据展示、统计分析等功能的菜单项</p>
          <p>4. 建议根据业务功能选择合适的图表图标，提升用户体验</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { PieChart, Histogram, DataLine, DataBoard, TrendCharts, DataAnalysis, InfoFilled } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.chart-page {
  .search-card {
    margin-bottom: 16px;

    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .tips {
      display: flex;
      gap: 12px;

      .el-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .chart-card {
    .chart-examples {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 24px;
      margin-bottom: 32px;
    }

    .chart-example {
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      transition: all 0.3s ease;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
        transform: translateY(-2px);
      }

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px;
      }

      .chart-container {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 80px;
        margin-bottom: 16px;
        background: #f5f7fa;
        border-radius: 8px;
      }

      .code-example {
        background: #f6f8fa;
        border-radius: 6px;
        padding: 12px;
        text-align: left;

        pre {
          margin: 0;
          font-size: 12px;
          line-height: 1.5;
          color: #24292f;

          code {
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          }
        }
      }
    }

    .usage-guide {
      border-top: 1px solid #e4e7ed;
      padding-top: 24px;

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px;
      }

      .guide-content {
        background: #f8f9fa;
        border-radius: 8px;
        padding: 20px;

        p {
          margin: 0 0 12px;
          color: #606266;
          line-height: 1.6;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}
</style>