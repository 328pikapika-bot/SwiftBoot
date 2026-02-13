<template>
  <div class="pagination-container" :class="{ 'hidden': hidden }">
    <el-pagination
      :background="background"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :layout="layout"
      :page-sizes="pageSizes"
      :total="Number(total)"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  total: {
    required: true,
    type: [Number, String] // Allow String to prevent prop validation warning
  },
  page: {
    type: [Number, String],
    default: 1
  },
  limit: {
    type: [Number, String],
    default: 10
  },
  pageSizes: {
    type: Array as () => number[],
    default() {
      return [10, 20, 30, 50]
    }
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  background: {
    type: Boolean,
    default: true
  },
  autoScroll: {
    type: Boolean,
    default: true
  },
  hidden: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

const currentPage = computed({
  get() {
    return Number(props.page)
  },
  set(val) {
    emit('update:page', val)
  }
})

const pageSize = computed({
  get() {
    return Number(props.limit)
  },
  set(val) {
    emit('update:limit', val)
  }
})

function handleSizeChange(val: number) {
  emit('pagination', { page: currentPage.value, limit: val })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}

function handleCurrentChange(val: number) {
  emit('pagination', { page: val, limit: pageSize.value })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}

function scrollTo(left: number, top: number) {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
.pagination-container {
  background: #fff;
  padding: 16px 16px;
  display: flex;
  justify-content: flex-end; /* 默认右对齐 */
}
.pagination-container.hidden {
  display: none;
}

/* 适配暗黑模式 */
.dark .pagination-container {
  background: transparent;
}
</style>
