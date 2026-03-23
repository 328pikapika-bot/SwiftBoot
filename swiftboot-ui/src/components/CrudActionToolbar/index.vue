<template>
  <div class="crud-action-toolbar">
    <div class="crud-action-toolbar__left">
      <el-button
        v-for="action in visibleLeftActions"
        :key="action.key"
        :type="action.type || 'default'"
        :plain="action.plain"
        :link="action.link"
        :text="action.text"
        :disabled="action.disabled"
        :loading="action.loading"
        @click="emit('action', action.key)"
      >
        <el-icon v-if="action.icon">
          <component :is="action.icon" />
        </el-icon>
        {{ action.label }}
      </el-button>
      <slot name="left" />
    </div>

    <div class="crud-action-toolbar__right">
      <slot name="right">
        <el-tag v-if="summary" type="info" effect="plain">{{ summary }}</el-tag>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useUserStore } from '@/stores/user'

export interface CrudActionItem {
  key: string
  label: string
  type?: '' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
  plain?: boolean
  link?: boolean
  text?: boolean
  disabled?: boolean
  loading?: boolean
  visible?: boolean
  permission?: string | string[]
  icon?: Component
}

const props = defineProps<{
  leftActions?: CrudActionItem[]
  rightActions?: CrudActionItem[]
  summary?: string
}>()

const emit = defineEmits<{
  action: [key: string]
}>()

const userStore = useUserStore()

const hasPermission = (permission?: string | string[]) => {
  if (!permission) {
    return true
  }

  const permissions = userStore.userInfo?.permissions || []
  if (!permissions.length) {
    return true
  }

  const requiredPermissions = Array.isArray(permission) ? permission : [permission]
  return requiredPermissions.every((item) => permissions.includes(item))
}

const filterVisibleActions = (actions: CrudActionItem[] = []) => actions.filter((action) => action.visible !== false && hasPermission(action.permission))

const visibleLeftActions = computed(() => filterVisibleActions(props.leftActions))
</script>

<style scoped>
.crud-action-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.crud-action-toolbar__left,
.crud-action-toolbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
